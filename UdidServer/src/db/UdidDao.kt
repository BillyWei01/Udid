package db

import com.horizon.helper.DbHelper

import db.bean.DeviceId
import db.bean.DidLog
import db.bean.InstallLog
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

    fun queryDeviceId(mac: Long, serialNo: Long, androidId: Long): List<DeviceId> {
        val list = ArrayList<DeviceId>()
        if (mac == 0L && serialNo == 0L && androidId == 0L) {
            return list
        }
        var hasCondition = false
        val builder = StringBuilder(1024)
        builder.append("SELECT * FROM t_device_id WHERE ")
        if (mac != 0L) {
            builder.append("mac=").append(mac).append(' ')
            hasCondition = true
        }
        if (serialNo != 0L) {
            if (hasCondition) {
                builder.append("or ")
            }
            builder.append("serial_no=").append(serialNo).append(' ')
            hasCondition = true
        }
        if (androidId != 0L) {
            if (hasCondition) {
                builder.append("or ")
            }
            builder.append("android_id=").append(androidId)
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
        val deviceId = DeviceId()
        deviceId.seq = rs.getLong("seq")
        deviceId.udid = rs.getLong("udid")
        deviceId.mac = rs.getLong("mac")
        deviceId.android_id = rs.getLong("android_id")
        deviceId.serial_no = rs.getLong("serial_no")
        deviceId.physics_info = rs.getLong("physics_info")
        deviceId.dark_physics_info = rs.getLong("dark_physics_info")
        deviceId.create_time = rs.getLong("create_time")
        deviceId.update_time = rs.getLong("update_time")
        return deviceId
    }

    fun insertOrReplaceDeviceId(deviceId: DeviceId) {
        val sql = "INSERT OR REPLACE INTO t_device_id" +
                "(seq, udid, mac, android_id, serial_no, physics_info, dark_physics_info," +
                " create_time, update_time) VALUES (?,?,?,?,?,?,?,?,?)"
        val statement = dbHelper.connection.prepareStatement(sql)
        try {
            statement.setLong(1, deviceId.seq)
            statement.setLong(2, deviceId.udid)
            statement.setLong(3, deviceId.mac)
            statement.setLong(4, deviceId.android_id)
            statement.setLong(5, deviceId.serial_no)
            statement.setLong(6, deviceId.physics_info)
            statement.setLong(7, deviceId.dark_physics_info)
            statement.setLong(8, deviceId.create_time)
            statement.setLong(9, deviceId.update_time)
            statement.executeUpdate()
        } finally {
            DbHelper.closeQuietly(statement)
        }
    }

    fun insertDidLog(didLog: DidLog) {
        if (exist("SELECT 1 FROM t_did_log WHERE md5='" + didLog.md5 + "'")) {
            return
        }
        val sql = "INSERT INTO t_did_log" +
                "(md5, udid, mac, android_id, serial_no, physics_info, dark_physics_info," +
                " create_time) VALUES (?,?,?,?,?,?,?,?)"
        val statement = dbHelper.connection.prepareStatement(sql)
        try {
            statement.setString(1, didLog.md5)
            statement.setLong(2, didLog.udid)
            statement.setString(3, didLog.mac)
            statement.setString(4, didLog.android_id)
            statement.setString(5, didLog.serial_no)
            statement.setLong(6, didLog.physics_info)
            statement.setLong(7, didLog.dark_physics_info)
            statement.setLong(8, didLog.create_time)
            statement.executeUpdate()
        } finally {
            DbHelper.closeQuietly(statement)
        }
    }

    fun insertInstallLog(installLog: InstallLog) {
        if (exist("SELECT 1 FROM t_install_log WHERE install_id='" + installLog.install_id + "'")) {
            return
        }
        val sql = "INSERT INTO t_install_log" +
                "(install_id, udid, create_time)" +
                " VALUES (?,?,?)"
        val statement = dbHelper.connection.prepareStatement(sql)
        try {
            statement.setString(1, installLog.install_id)
            statement.setLong(2, installLog.udid)
            statement.setLong(3, installLog.create_time)
            statement.executeUpdate()
        } finally {
            DbHelper.closeQuietly(statement)
        }
    }

    private fun exist(sql: String): Boolean {
        val statement = dbHelper.connection.createStatement()
        var rs: ResultSet? = null
        try {
            rs = statement.executeQuery(sql)
            return rs.next()
        } finally {
            DbHelper.closeQuietly(rs)
            DbHelper.closeQuietly(statement)
        }
    }
}