package server

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import horizon.task.TaskCenter
import horizon.util.*
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import db.IdGenerator
import db.UdidDao
import db.bean.DeviceId
import server.vo.DeviceIdInfo
import java.io.IOException

class UdidHandler : HttpHandler {
    companion object {
        const val PATH = "/did"

        const val CODE_SUCCESS = 0
        const val CODE_FAIL = 1

        const val OP_QUERY = "query"
        const val OP_UPDATE = "update"

        const val CODE = "code"
        const val MESSAGE = "message"
    }

    @Throws(IOException::class)
    override fun handle(exchange: HttpExchange) {
        val request = IOUtil.streamToString(exchange.requestBody)
        val uri = exchange.requestURI.toString()

        println(DateUtil.now() + " get request, uri:" + uri + " body:" + request)

        val responseHeaders = exchange.responseHeaders
        responseHeaders.set("Content-Type", "text/plain; charset=utf-8")

        if (request.isNullOrEmpty()) {
            responseIllegal(exchange)
            return
        }

        val operation: String
        val info: DeviceIdInfo
        val udid: Long
        try {
            val json = JSON.parseObject(request)
            val deviceIdInfo = json.getJSONObject("device_id_info")
            info = deviceIdInfo.toJavaObject(DeviceIdInfo::class.java)
            operation = json.getString("operation")
            udid = if (OP_UPDATE == operation) HexUtil.hex2Long(json.getString("udid")) else 0L
        } catch (e: Exception) {
            e.printStackTrace()
            responseIllegal(exchange)
            return
        }

        if (info.mac.isNullOrEmpty()
                && info.androidId.isNullOrEmpty()
                && info.serialNo.isNullOrEmpty()) {
            responseIllegal(exchange)
            return
        }

        val mac = HexUtil.hex2Long(StringUtil.removeChar(info.mac, ':'))
        val androidId = HexUtil.hex2Long(info.androidId)
        val serialNo = MHash.hash64(info.serialNo)

        val r = DeviceId()
        r.mac = mac
        r.serial_no = serialNo
        r.android_id = androidId
        r.physics_info = HexUtil.hex2Long(info.physicsInfoHash)
        r.dark_physics_info = HexUtil.hex2Long(info.darkPhysicsInfoHash)

        when (operation) {
            OP_QUERY -> {
                val deviceIdList = UdidDao.queryDeviceId(mac, serialNo, androidId)
                val didFormDb = matchDeviceId(deviceIdList, r)
                if (didFormDb != null) {
                    updateDeviceId(didFormDb, r)
                } else {
                    insertDeviceId(r)
                }
                responseQuery(exchange, r.udid)
            }
            OP_UPDATE -> {
                responseUpdate(exchange)
                UdidDao.queryDeviceId(udid)?.let {
                    updateDeviceId(it, r)
                }
            }
            else -> {
                responseIllegal(exchange)
            }
        }
    }

    private fun insertDeviceId(r: DeviceId) {
        // udid不应该直接取seq, 因为seq是有序的，从而容易被猜出其他的id，以及知道ID生成的数量，
        // 所以这里我们做一个编码。
        // 编码要求 udid 和 seq 一一映射，从而确保udid不重复。
        // 要做到一一映射，就不能使用hash, 而应该使用可逆编码。
        //
        // 当前IdGenerator生成的ID是小于等于48bit,
        // 所以只需对低48bit加密，这样高16bit还是0, 转成十六进制的话只有12字节。
        // 通过LongEncoder.encode48, 会将seq随机地映射到2^48（两百多万亿）的空间。
        // 如果觉得还不够安全，可以用LongEncoder.encode64。
        r.seq = IdGenerator.getUdidSeq()
        r.udid = LongEncoder.encode48(r.seq)
        r.create_time = System.currentTimeMillis()
        r.update_time = r.create_time
        UdidDao.insertOrReplaceDeviceId(r)
    }

    private fun updateDeviceId(d: DeviceId, r: DeviceId): Boolean {
        r.seq = d.seq
        r.udid = d.udid
        r.create_time = d.create_time
        r.update_time = System.currentTimeMillis()
        if (!(d.serial_no == r.serial_no
                        && d.mac == r.mac
                        && d.android_id == r.android_id
                        && d.physics_info == r.physics_info
                        && d.dark_physics_info == r.dark_physics_info)) {
            UdidDao.insertOrReplaceDeviceId(r)
            return true
        }
        return false
    }

    // 相等且不为零方为匹配
    private fun idMatch(a: Long, b: Long): Boolean {
        return a != 0L && a == b
    }

    private fun matchDeviceId(deviceIdList: List<DeviceId>, r: DeviceId): DeviceId? {
        if (deviceIdList.isEmpty()) {
            return null
        }
        var maxPriorityDid: DeviceId? = null
        var priority = 0
        deviceIdList.forEach { did ->
            val s = idMatch(did.serial_no, r.serial_no)
            val a = idMatch(did.android_id, r.android_id)
            val m = idMatch(did.mac, r.mac)
            if (s && m && a) {
                return did
            }

            if (priority == 3) return@forEach
            if ((s && (a || m)) || (a && m)) {
                priority = 3
                maxPriorityDid = did
            }

            if (priority >= 2) return@forEach
            val p = idMatch(did.physics_info, r.physics_info)
                    || idMatch(did.dark_physics_info, r.dark_physics_info)
            if (p && a) {
                priority = 2
                maxPriorityDid = did
            }

            if (priority >= 1) return@forEach
            if (p && m) {
                priority = 1
                maxPriorityDid = did
            }
        }
        return maxPriorityDid
    }

    private fun responseUpdate(exchange: HttpExchange) {
        val json = JSONObject()
        json[CODE] = CODE_SUCCESS
        json[MESSAGE] = "success"
        response(exchange, 200, json)
    }

    private fun responseQuery(exchange: HttpExchange, udid: Long) {
        val json = JSONObject()
        json[CODE] = CODE_SUCCESS
        // Long型udid传给客户端时，可以转化为十六进制来传输，
        // 相对于十进制，客户端不需要解析，字符长度短，可能性高。
        // 当前生成的是48bit的udid，所以可以转成定长12字节的字符串。
        json["udid"] = HexUtil.long48ToHex(udid)
        response(exchange, 200, json)
    }

    private fun responseIllegal(exchange: HttpExchange) {
        val json = JSONObject()
        json[CODE] = CODE_FAIL
        json[MESSAGE] = "非法请求"
        response(exchange, 400, json)
    }

    private fun response(exchange: HttpExchange, statusCode: Int, json: JSONObject) {
        TaskCenter.io.execute {
            try {
                val responseContent = json.toString()
                println(DateUtil.now() + " response: $responseContent")

                exchange.sendResponseHeaders(statusCode, 0)
                val responseBody = exchange.responseBody
                responseBody.write(responseContent.toByteArray())
                responseBody.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
