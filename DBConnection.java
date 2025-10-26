import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Use environment variables on Railway
    private static final String URL = System.getenv("mysql://root:tqWLlmQLEsOnCQgwybAZwieFJGDbnXay@turntable.proxy.rlwy.net:19066/railway");
    private static final String USER = System.getenv("root");
    private static final String PASSWORD = System.getenv("tqWLlmQLEsOnCQgwybAZwieFJGDbnXay");

    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
}


