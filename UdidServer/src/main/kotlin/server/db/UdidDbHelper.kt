package server.db

import tools.helper.SQLiteOpenHelper
import tools.util.IOUtil
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.sql.Statement

class UdidDbHelper(dbPath: String) : SQLiteOpenHelper(dbPath, VERSION) {
    companion object {
        private const val VERSION = 1
    }

    override fun onCreate(statement: Statement) {
      doUpgrade(statement, 0, VERSION)
    }

    override fun onUpgrade(statement: Statement, oldVersion: Int, newVersion: Int) {
        doUpgrade(statement, oldVersion, newVersion)
    }

    private fun doUpgrade(statement: Statement, oldVersion: Int, newVersion: Int) {
        for (i in (oldVersion + 1)..newVersion) {
            val path  = "./config/udid/"
            val inputStream = BufferedInputStream(FileInputStream(path + "version$i.sql"))
            IOUtil.streamToString(inputStream).run {
                split('#').forEach { sql ->
                    statement.execute(sql)
                }
            }
        }
    }

}