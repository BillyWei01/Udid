package horizon.helper;

import horizon.util.IOUtil;

import java.io.File;
import java.sql.*;

public abstract class SQLiteOpenHelper {
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Connection mConnection;

    public SQLiteOpenHelper(String dbPath, int version) {
        Statement statement = null;
        try {
            IOUtil.makeFileIfNotExist(new File(dbPath));
            mConnection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            if(mConnection == null){
                throw new IllegalStateException("Failure to connect database:" + dbPath);
            }
            statement = mConnection.createStatement();
            ResultSet rs = statement.executeQuery("PRAGMA user_version");
            rs.next();
            int userVersion = rs.getInt(1);
            rs.close();

            if (userVersion == 0) {
                onCreate(statement);
            } else if (userVersion < version) {
                onUpgrade(statement, userVersion, version);
            } else if (userVersion > version) {
                throw new IllegalArgumentException("db can't downgrade, current version:"
                        + userVersion + " target version:" + version);
            }
            statement.execute("PRAGMA user_version=" + version);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            DbHelper.closeQuietly(statement);
        }
    }

    public Connection getConnection(){
        return mConnection;
    }

    public abstract void onCreate(Statement statement);

    public abstract void onUpgrade(Statement statement, int oldVersion, int newVersion);

    //private void open
}
