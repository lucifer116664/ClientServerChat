package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionManager {
    private static final String URL = PropertiesUtil.getProperty("db.url");
    private static final String USERNAME = PropertiesUtil.getProperty("db.username");
    private static final String PASSWORD = PropertiesUtil.getProperty("db.password");

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
