package horizon.helper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbHelper {
    public static void closeQuietly(Statement statement) {
        if (statement != null ) {
            try {
                if(!statement.isClosed()){
                    statement.close();
                }
            } catch (Exception ignore) {
            }
        }
    }

    public static void closeQuietly(PreparedStatement statement) {
        if (statement != null ) {
            try {
                if(!statement.isClosed()){
                    statement.close();
                }
            } catch (Exception ignore) {
            }
        }
    }

    public static void closeQuietly(ResultSet resultSet) {
        if (resultSet != null ) {
            try {
                if(!resultSet.isClosed()){
                    resultSet.close();
                }
            } catch (Exception ignore) {
            }
        }
    }


}
