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

  public void createAuth(AuthData auth) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "INSERT INTO AuthTokens (authToken, username) VALUES (?, ?)";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, auth.authToken());
        stmt.setString(2, auth.username());
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException(e.getMessage());
    }
  }

  public AuthData getAuth(String authToken) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "SELECT * FROM AuthTokens WHERE authToken = ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, authToken);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
          return new AuthData(rs.getString("authToken"), rs.getString("username"));
        } else {
          throw new DataAccessException("Auth token not found.");
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException(e.getMessage());
    }
  }

  public void deleteAuth(String authToken) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "DELETE FROM AuthTokens WHERE authToken = ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, authToken);
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException(e.getMessage());
    }
  }

  public void clearAllAuthTokens() throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "DELETE FROM AuthTokens";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException(e.getMessage());
    }
  }
}