package dataaccess;

import model.UserData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UserDAO {

  // In-memory user store (this will eventually be replaced by a database)
  private static final Map<String, UserData> USERS = new HashMap<>();

  // Method to create a new user
  public void createUser(UserData user) throws DataAccessException {
    if (USERS.containsKey(user.username())) {
      throw new DataAccessException("User already exists.");
    }
    USERS.put(user.username(), user);
  }
  // Database-backed method to create a new user
  public void createUserInDatabase(UserData user) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, user.username());
        stmt.setString(2, user.password());
        stmt.setString(3, user.email());
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error creating user in database: " + e.getMessage());
    }
  }

  // Database-backed method to get a user by username
  public UserData getUserFromDatabase(String username) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "SELECT * FROM users WHERE username = ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, username);
        try (ResultSet rs = stmt.executeQuery()) {
          if (rs.next()) {
            return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
          } else {
            throw new DataAccessException("User not found.");
          }
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error retrieving user from database: " + e.getMessage());
    }
  }

  // Method to get a user by username
  public UserData getUser(String username) throws DataAccessException {
    UserData user = USERS.get(username);
    if (user == null) {
      throw new DataAccessException("User not found.");
    }
    return user;
  }

  // Method to clear all users
  public void clearAllUsers() throws DataAccessException {
    USERS.clear();
  }
  public void clearAllUsersFromDatabase() throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "DELETE FROM users";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error clearing users from database: " + e.getMessage());
    }
  }
}
