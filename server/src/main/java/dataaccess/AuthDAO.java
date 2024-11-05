package dataaccess;

// SQL and database imports
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Custom exception class for handling data access exceptions
import dataaccess.DataAccessException;

// Model class for authentication data
import model.AuthData;

// BCrypt library for password hashing and verification
import org.mindrot.jbcrypt.BCrypt;
import java.sql.ResultSet;

//import java.util.HashMap;
//import java.util.Map;

public class AuthDAO {

  public void createAuth(AuthData authData) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      conn.setAutoCommit(false); // Start transaction

      String sql = "INSERT INTO AuthTokens (authToken, username) VALUES (?, ?)";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, authData.authToken());
        stmt.setString(2, authData.username());
        stmt.executeUpdate();
      }

      conn.commit(); // Commit the transaction

    } catch (SQLException e) {
      throw new DataAccessException("Error creating auth token: " + e.getMessage());
    }
  }


  public AuthData getAuth(String authToken) throws DataAccessException {
    String sql = "SELECT * FROM AuthTokens WHERE authToken = ?";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, authToken);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          String username = rs.getString("username");
          return new AuthData(username, authToken); // Found auth token
        } else {
          return null; // Auth token not found
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error retrieving auth token: " + e.getMessage());
    }
  }


  public void deleteAuth(String authToken) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      conn.setAutoCommit(false); // Start transaction

      String sql = "DELETE FROM AuthTokens WHERE authToken = ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, authToken);
        int affectedRows = stmt.executeUpdate();
        if (affectedRows == 0) {
          throw new DataAccessException("Auth token not found.");
        }
      }

      conn.commit(); // Commit the transaction

    } catch (SQLException e) {
      throw new DataAccessException("Error deleting auth token: " + e.getMessage());
    }
  }




  public void clearAllAuthTokens() throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      conn.setAutoCommit(false); // Start transaction

      String sql = "DELETE FROM AuthTokens";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.executeUpdate();
      }

      conn.commit(); // Commit the transaction

    } catch (SQLException e) {
      throw new DataAccessException("Error clearing auth tokens: " + e.getMessage());
    }
  }

}