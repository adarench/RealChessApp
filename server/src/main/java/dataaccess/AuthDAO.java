package dataaccess;

import model.AuthData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AuthDAO {

  // In-memory auth store (will eventually be replaced by a database)
  private static Map<String, AuthData> AUTH_TOKENS = new HashMap<>();
  private static final boolean USE_DATABASE = true;  // Set to true for tests using the database

  public Set<String> getAllAuthTokens() {
    return AUTH_TOKENS.keySet();
  }

  // In-memory method
  public void createAuth(AuthData auth) throws DataAccessException {
    if (USE_DATABASE) {
      createAuthInDatabase(auth);  // Use database-backed method
    } else {
      AUTH_TOKENS.put(auth.authToken(), auth);  // In-memory storage
    }
  }
  // Database-backed method for creating auth using `username` as identifier
  public void createAuthInDatabase(AuthData auth) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, auth.authToken());
        stmt.setString(2, auth.username());  // Use username as the unique identifier
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error creating auth token in database: " + e.getMessage());
    }
  }

  // Method to get an auth token from the database
  public AuthData getAuthFromDatabase(String authToken) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "SELECT * FROM auth WHERE authToken = ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, authToken);
        try (ResultSet rs = stmt.executeQuery()) {
          if (rs.next()) {
            return new AuthData(rs.getString("authToken"), rs.getString("username"));
          } else {
            throw new DataAccessException("Auth token not found.");
          }
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error retrieving auth token from database: " + e.getMessage());
    }
  }
  // Database-backed method to delete an auth token
  public void deleteAuthFromDatabase(String authToken) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "DELETE FROM auth WHERE authToken = ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, authToken);
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error deleting auth token from database: " + e.getMessage());
    }
  }

  // Method to get an auth token
  public AuthData getAuth(String authToken) throws DataAccessException {
    if (USE_DATABASE) {
      return getAuthFromDatabase(authToken);  // Retrieve from the database
    } else {
      AuthData auth = AUTH_TOKENS.get(authToken);
      if (auth == null) {
        throw new DataAccessException("Auth token not found.");
      }
      return auth;
    }
  }

  // Method to delete an auth token (for logging out)
  public void deleteAuth(String authToken) throws DataAccessException {
    if (!AUTH_TOKENS.containsKey(authToken)) {
      throw new DataAccessException("Auth token not found.");
    }
    AUTH_TOKENS.remove(authToken);
  }

  // Method to clear all auth tokens
  public void clearAllAuthTokens() throws DataAccessException {
    AUTH_TOKENS.clear();
  }
}
