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
      String sql = "INSERT INTO Users (username, password, email) VALUES (?, ?, ?)";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, user.username());
        stmt.setString(2, BCrypt.hashpw(user.password(), BCrypt.gensalt()));
        stmt.setString(3, user.email());
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException("User already exists.");
    }
  }

  public UserData getUser(String username) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "SELECT * FROM Users WHERE username = ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
          return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
        } else {
          throw new DataAccessException("User not found.");
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException(e.getMessage());
    }
  }

  public void clearAllUsers() throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "DELETE FROM Users";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException(e.getMessage());
    }
  }
}