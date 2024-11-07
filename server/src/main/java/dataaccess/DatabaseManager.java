package dataaccess;

import java.sql.*;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String DATABASE_NAME;
    private static final String USER;
    private static final String PASSWORD;
    private static final String CONNECTION_URL;

    static {
        try {
            var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties");
            if (propStream == null) throw new Exception("Unable to load db.properties");

            Properties props = new Properties();
            props.load(propStream);

            DATABASE_NAME = props.getProperty("db.name");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");
            var host = props.getProperty("db.host");
            var port = Integer.parseInt(props.getProperty("db.port"));

            CONNECTION_URL = String.format("jdbc:mysql://%s:%d/%s", host, port, DATABASE_NAME);

        } catch (Exception ex) {
            throw new RuntimeException("Unable to configure database connection pool: " + ex.getMessage());
        }
    }

    public static Connection getConnection() throws DataAccessException {
        try {
            var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
            conn.setCatalog(DATABASE_NAME);
            return conn;
        } catch (SQLException e) {
            throw new DataAccessException("Error obtaining database connection: " + e.getMessage());
        }
    }

    public static void initializeDatabase() throws DataAccessException {
        createDatabase();
        createTables();
    }

    public static void createDatabase() throws DataAccessException {
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL.replace(DATABASE_NAME, ""), USER, PASSWORD)) {
            var statement = "CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME;
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public static void createTables() throws DataAccessException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {

                // Create Users Table
                stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Users (
                    username VARCHAR(255) PRIMARY KEY,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255) UNIQUE NOT NULL
                )
            """);

                // Create AuthTokens Table
                stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS AuthTokens (
                    authToken VARCHAR(255) PRIMARY KEY,
                    username VARCHAR(255) NOT NULL,
                    FOREIGN KEY (username) REFERENCES Users(username) ON DELETE CASCADE
                )
            """);

                // Create Games Table
                stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Games (
                    gameID INT AUTO_INCREMENT PRIMARY KEY,
                    gameName VARCHAR(255) NOT NULL,
                    whiteUsername VARCHAR(255),
                    blackUsername VARCHAR(255),
                    FOREIGN KEY (whiteUsername) REFERENCES Users(username) ON DELETE SET NULL,
                    FOREIGN KEY (blackUsername) REFERENCES Users(username) ON DELETE SET NULL
                )
            """);

                conn.commit();

            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating tables: " + e.getMessage());
        }
    }
}