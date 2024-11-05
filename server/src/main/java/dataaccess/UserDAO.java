package dataaccess;

import model.UserData;

// SQL and database imports
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Custom exception class for handling data access exceptions
import dataaccess.DataAccessException;

// Model class for authentication data

// BCrypt library for password hashing and verification
import org.mindrot.jbcrypt.BCrypt;
import java.sql.ResultSet;

//import java.util.HashMap;
//import java.util.Map;

public class UserDAO {


  public void createUser(UserData user) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      conn.setAutoCommit(false); // Start transaction

      String sql = "INSERT INTO Users (username, password, email) VALUES (?, ?, ?)";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, user.username());
        stmt.setString(2, BCrypt.hashpw(user.password(), BCrypt.gensalt()));
        stmt.setString(3, user.email());
        stmt.executeUpdate();
      }

      conn.commit(); // Commit the transaction

    } catch (SQLException e) {
      throw new DataAccessException("Error creating user: " + e.getMessage());
    }
  }



  public UserData getUser(String username) throws DataAccessException {
    String sql = "SELECT * FROM Users WHERE username = ?";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, username);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          String password = rs.getString("password");
          String email = rs.getString("email");
          return new UserData(username, password, email); // Found user
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error retrieving user: " + e.getMessage());
    }
    return null; // User not found
  }




  public void clearAllUsers() throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      conn.setAutoCommit(false); // Start transaction

      String sql = "DELETE FROM Users";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.executeUpdate();
      }

      conn.commit(); // Commit the transaction

    } catch (SQLException e) {
      throw new DataAccessException("Error clearing users: " + e.getMessage());
    }
  }

  public void updateUser(UserData user) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      conn.setAutoCommit(false); // Start transaction

      String sql = "UPDATE Users SET password = ?, email = ? WHERE username = ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, BCrypt.hashpw(user.password(), BCrypt.gensalt()));
        stmt.setString(2, user.email());
        stmt.setString(3, user.username());
        stmt.executeUpdate();
      }

      conn.commit(); // Commit the transaction

    } catch (SQLException e) {
      throw new DataAccessException("Error updating user: " + e.getMessage());
    }
  }

  public void deleteUser(String username) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      conn.setAutoCommit(false); // Start transaction

      String sql = "DELETE FROM Users WHERE username = ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, username);
        stmt.executeUpdate();
      }

      conn.commit(); // Commit the transaction

    } catch (SQLException e) {
      throw new DataAccessException("Error deleting user: " + e.getMessage());
    }
  }



}