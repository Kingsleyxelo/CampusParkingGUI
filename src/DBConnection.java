import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static final String USER = "root";
    private static final String PASS = "0735262392@Akx";   // ← your MySQL password
    private static final String DB_NAME = "campus_parking";

    public static Connection getConnection() throws SQLException {
        // First make sure DB and table exist (creates if missing)
        ensureSetup();
        // Now connect directly to campus_parking
        String url = "jdbc:mysql://localhost:3306/" + DB_NAME + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        return DriverManager.getConnection(url, USER, PASS);
    }

    private static void ensureSetup() {
        String baseUrl = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        try (Connection conn = DriverManager.getConnection(baseUrl, USER, PASS);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            stmt.executeUpdate("USE " + DB_NAME);
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS parking_records (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "plate VARCHAR(20) NOT NULL, " +
                    "owner VARCHAR(100) NOT NULL, " +
                    "type VARCHAR(20) NOT NULL, " +
                    "slot VARCHAR(20) NOT NULL, " +
                    "status VARCHAR(20) DEFAULT 'Parked')");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("DB setup failed: " + e.getMessage());
        }
    }
}