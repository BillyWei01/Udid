package tools.helper

import tools.helper.DbHelper.closeQuietly
import tools.util.IOUtil.makeFileIfNotExist
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

abstract class SQLiteOpenHelper(dbPath: String, version: Int) {
    lateinit var connection: Connection

    init {
        var stmt: Statement? = null
        try {
            makeFileIfNotExist(File(dbPath))
            connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")
            val statement = connection.createStatement()
            stmt = statement
            val rs = statement.executeQuery("PRAGMA user_version")
            rs.next()
            val userVersion = rs.getInt(1)
            rs.close()
            if (userVersion == 0) {
                onCreate(statement)
            } else if (userVersion < version) {
                onUpgrade(statement, userVersion, version)
            } else require(userVersion <= version) {
                ("db can't downgrade, current version:"
                        + userVersion + " target version:" + version)
            }
            statement.execute("PRAGMA user_version=$version")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            closeQuietly(stmt)
        }
    }

    abstract fun onCreate(statement: Statement)
    abstract fun onUpgrade(statement: Statement, oldVersion: Int, newVersion: Int) //private void open

    companion object {
        init {
            try {
                Class.forName("org.sqlite.JDBC")
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
        }
    }
}
