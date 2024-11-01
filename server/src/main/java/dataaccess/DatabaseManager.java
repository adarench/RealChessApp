package dataaccess;

import java.sql.*;
import java.util.Properties;
import dataaccess.DataAccessException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

public class DatabaseManager {
  private static final String DATABASE_NAME;
  private static final String USER;
  private static final String PASSWORD;
  private static final String CONNECTION_URL;
  private static final String HOST;
  private static final int PORT;

  /*
   * Load the database information for the db.properties file.
   */
  static {
    try {
      try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
        if (propStream == null) {
          throw new Exception("Unable to load db.properties");
        }
        Properties props = new Properties();
        props.load(propStream);
        DATABASE_NAME = props.getProperty("db.name");
        USER = props.getProperty("db.user");
        PASSWORD = props.getProperty("db.password");

        HOST = props.getProperty("db.host");
        PORT = Integer.parseInt(props.getProperty("db.port"));
        CONNECTION_URL = String.format("jdbc:mysql://%s:%d/%s", HOST, PORT, DATABASE_NAME);

      }
    } catch (Exception ex) {
      throw new RuntimeException("unable to process db.properties. " + ex.getMessage());
    }
  }

  /**
   * Creates the database if it does not already exist.
   */
  public static void createDatabase() throws DataAccessException {
    String urlWithoutDatabase = String.format("jdbc:mysql://%s:%d/", HOST, PORT);
    try (Connection conn = DriverManager.getConnection(urlWithoutDatabase, USER, PASSWORD);
         Statement stmt = conn.createStatement()) {

      String statement = "CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME;
      stmt.executeUpdate(statement);

    } catch (SQLException e) {
      throw new DataAccessException("Error creating database: " + e.getMessage());
    }
  }

  /**
   * Create a connection to the database and sets the catalog based upon the
   * properties specified in db.properties. Connections to the database should
   * be short-lived, and you must close the connection when you are done with it.
   * The easiest way to do that is with a try-with-resource block.
   * <br/>
   * <code>
   * try (var conn = DbInfo.getConnection(databaseName)) {
   * // execute SQL statements.
   * }
   * </code>
   */
  static Connection getConnection() throws DataAccessException {
    try {
      var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);

      //conn.setCatalog(DATABASE_NAME);
      System.out.println("Connection successful: " + (conn != null));
      return conn;
    } catch (SQLException e) {
      throw new DataAccessException(e.getMessage());
    }
  }

  public static void createTables() throws DataAccessException {
    String createUsersTable = "CREATE TABLE IF NOT EXISTS Users (" +
            "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
            "username VARCHAR(50) NOT NULL UNIQUE, " +
            "hashed_password VARCHAR(255) NOT NULL, " +
            "email VARCHAR(255) NOT NULL, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")";

    String createGamesTable = "CREATE TABLE IF NOT EXISTS Games (" +
            "game_id INT AUTO_INCREMENT PRIMARY KEY, " +
            "game_name VARCHAR(255) NOT NULL, " +
            "white_username VARCHAR(50), " +
            "black_username VARCHAR(50), " +
            "game_state JSON, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")";
    String createAuthTable = "CREATE TABLE IF NOT EXISTS auth (" +
            "authToken VARCHAR(255) PRIMARY KEY, " +
            "username VARCHAR(255) NOT NULL" +
            ")";

    try (var conn = getConnection()) {
      try (var stmt = conn.createStatement()) {
        stmt.executeUpdate(createUsersTable);
        stmt.executeUpdate(createGamesTable);
        stmt.executeUpdate(createAuthTable);
      }
    } catch (SQLException e) {
      throw new DataAccessException(e.getMessage());
    }
  }

}