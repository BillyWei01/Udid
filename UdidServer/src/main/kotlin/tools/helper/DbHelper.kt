package tools.helper

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement

object DbHelper {
    @JvmStatic
    fun closeQuietly(statement: Statement?) {
        if (statement != null) {
            try {
                if (!statement.isClosed) {
                    statement.close()
                }
            } catch (ignore: Exception) {
            }
        }
    }

    fun closeQuietly(statement: PreparedStatement?) {
        if (statement != null) {
            try {
                if (!statement.isClosed) {
                    statement.close()
                }
            } catch (ignore: Exception) {
            }
        }
    }

    fun closeQuietly(resultSet: ResultSet?) {
        if (resultSet != null) {
            try {
                if (!resultSet.isClosed) {
                    resultSet.close()
                }
            } catch (ignore: Exception) {
            }
        }
    }
}
