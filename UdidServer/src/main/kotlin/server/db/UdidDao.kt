package server.db

import tools.helper.DbHelper

import server.db.bean.DeviceId
import java.sql.ResultSet

object UdidDao {
    private val dbHelper = UdidDbHelper("./data/udid.db")

    fun getMaxSeq(lower : Long, upper : Long):Long{
        val sql = "SELECT seq FROM t_device_id WHERE seq>$lower and seq<=$upper ORDER BY seq DESC LIMIT 1"
        val statement = dbHelper.connection.createStatement()
        var rs: ResultSet? = null
        try {
            rs = statement.executeQuery(sql)
            if (rs.next()) {
                return rs.getLong(1)
            }
        } finally {
            DbHelper.closeQuietly(rs)
            DbHelper.closeQuietly(statement)
        }
        return 0L
    }

    fun queryDeviceId(udid: Long): DeviceId? {
        val sql = "SELECT * FROM t_device_id WHERE udid=$udid"
        val statement = dbHelper.connection.createStatement()
        var rs: ResultSet? = null
        try {
            rs = statement.executeQuery(sql)
            if (rs.next()) {
                return parseDeviceId(rs)
            }
        } finally {
            DbHelper.closeQuietly(rs)
            DbHelper.closeQuietly(statement)
        }
        return null
    }

    fun queryDeviceId(androidId: Long, widevineId: Long, deviceHash: Long, ): List<DeviceId> {
        val list = ArrayList<DeviceId>()
        if (androidId == 0L && widevineId == 0L && deviceHash == 0L) {
            return list
        }
        var hasCondition = false
        val builder = StringBuilder(128)
        builder.append("SELECT * FROM t_device_id WHERE ")
        if (androidId != 0L) {
            builder.append("android_id=").append(androidId).append(' ')
            hasCondition = true
        }
        if (widevineId != 0L) {
            if (hasCondition) {
                builder.append("or ")
            }
            builder.append("widevine_id=").append(widevineId).append(' ')
            hasCondition = true
        }
        if (deviceHash != 0L) {
            if (hasCondition) {
                builder.append("or ")
            }
            builder.append("device_hash=").append(deviceHash)
        }

        val sql = builder.toString()
        val statement = dbHelper.connection.createStatement()
        var rs: ResultSet? = null
        try {
            rs = statement.executeQuery(sql)
            while (rs.next()) {
                list.add(parseDeviceId(rs))
            }
        } finally {
            DbHelper.closeQuietly(rs)
            DbHelper.closeQuietly(statement)
        }
        return list
    }

    private fun parseDeviceId(rs: ResultSet): DeviceId {
        return DeviceId(
            seq = rs.getLong("seq"),
            udid = rs.getLong("udid"),
            androidId = rs.getLong("android_id"),
            widevineId = rs.getLong("widevine_id"),
            deviceHash = rs.getLong("device_hash"),
            localDid = rs.getString("local_did") ?: "",
            createTime = rs.getLong("create_time"),
            updateTime = rs.getLong("update_time")
        )
    }

    fun insertOrReplaceDeviceId(deviceId: DeviceId) {
        val sql = "INSERT OR REPLACE INTO t_device_id" +
                "(seq, udid, android_id, widevine_id, device_hash, local_did," +
                " create_time, update_time) VALUES (?,?,?,?,?,?,?,?)"
        val statement = dbHelper.connection.prepareStatement(sql)
        try {
            statement.setLong(1, deviceId.seq)
            statement.setLong(2, deviceId.udid)
            statement.setLong(3, deviceId.androidId)
            statement.setLong(4, deviceId.widevineId)
            statement.setLong(5, deviceId.deviceHash)
            statement.setString(6, deviceId.localDid)
            statement.setLong(7, deviceId.createTime)
            statement.setLong(8, deviceId.updateTime)
            statement.executeUpdate()
        } finally {
            DbHelper.closeQuietly(statement)
        }
    }
}