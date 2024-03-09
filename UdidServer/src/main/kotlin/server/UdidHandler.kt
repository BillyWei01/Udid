package server

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import horizon.task.TaskCenter
import horizon.util.*
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import db.IdGenerator
import db.UdidDao
import db.bean.DeviceId
import server.vo.DeviceIdInfo
import server.vo.DidRequest
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
            val didRequest = JSON.parseObject(request, DidRequest::class.java)
            operation = didRequest.operation
            info = didRequest.deviceIdInfo
            if (info.androidId.isEmpty() && info.widevineId.isEmpty() && info.deviceHash.isEmpty()
            ) {
                responseIllegal(exchange)
                return
            }
            udid = if (OP_UPDATE == operation) HexUtil.hex2Long(didRequest.udid) else 0L
        } catch (e: Exception) {
            e.printStackTrace()
            responseIllegal(exchange)
            return
        }

        val androidId = HexUtil.hex2Long(info.androidId)
        val widevineId = HexUtil.hex2Long(info.widevineId)
        val deviceHash = HexUtil.hex2Long(info.deviceHash)

        val didFromReq = DeviceId(
            androidId = androidId,
            widevineId = widevineId,
            deviceHash = deviceHash,
            localDid = info.localDid
        )

        try {
            when (operation) {
                OP_QUERY -> {
                    val deviceIdList = UdidDao.queryDeviceId(androidId, widevineId, deviceHash)
                    val didFormDb = matchDeviceId(deviceIdList, didFromReq)
                    if (didFormDb != null) {
                        updateDeviceId(didFormDb, didFromReq)
                    } else {
                        insertDeviceId(didFromReq)
                    }
                    responseQuery(exchange, didFromReq.udid)
                }

                OP_UPDATE -> {
                    responseUpdate(exchange)
                    if (udid != 0L) {
                        UdidDao.queryDeviceId(udid)?.let { didFormDb ->
                            updateDeviceId(didFormDb, didFromReq)
                        }
                    }
                }

                else -> {
                    responseIllegal(exchange)
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
            val json = JSONObject()
            json[CODE] = CODE_FAIL
            json[MESSAGE] = "服务器错误"
            response(exchange, 500, json)
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
        r.createTime = System.currentTimeMillis()
        r.updateTime = r.createTime
        UdidDao.insertOrReplaceDeviceId(r)
    }

    private fun updateDeviceId(d: DeviceId, r: DeviceId): Boolean {
        // 当数据看库中的信息和请求的信息不想等时，更新数据库
        if (
            d.androidId != r.androidId ||
            d.widevineId != r.widevineId ||
            d.deviceHash != r.deviceHash
        ) {
            r.seq = d.seq
            r.udid = d.udid
            r.createTime = d.createTime
            r.updateTime = System.currentTimeMillis()
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
            val a = idMatch(did.androidId, r.androidId)
            val w = idMatch(did.widevineId, r.widevineId)
            val d = idMatch(did.deviceHash, r.deviceHash)
            if (w && d && a) {
                return did
            }
            if (priority < 2 && (a && w)) {
                priority = 2
                maxPriorityDid = did
            }
            if (priority < 1 && ((a && d) || (w && d))) {
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
        TaskCenter.executor.execute {
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
