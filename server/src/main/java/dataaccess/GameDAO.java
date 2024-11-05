package dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
import model.GameData;
import java.sql.Statement;

public class GameDAO {

  public GameData createGame(String gameName) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      conn.setAutoCommit(false); // Start transaction

      String sql = "INSERT INTO Games (gameName) VALUES (?)";
      try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, gameName);
        stmt.executeUpdate();

        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            int gameId = generatedKeys.getInt(1);
            conn.commit(); // Commit the transaction

            // Return the created game
            return new GameData(gameId, null, null, gameName);
          } else {
            throw new DataAccessException("Creating game failed, no ID obtained.");
          }
        }
      } catch (SQLException e) {
        conn.rollback(); // Roll back on error
        throw new DataAccessException("Error creating game: " + e.getMessage());
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error creating game: " + e.getMessage());
    }
  }


  public GameData getGame(int gameID) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Games WHERE game_id = ?")) {

      stmt.setInt(1, gameID);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          String gameName = rs.getString("gameName");
          String whiteUsername = rs.getString("whiteUsername");
          String blackUsername = rs.getString("blackUsername");
          return new GameData(gameID, whiteUsername, blackUsername, gameName);
        } else {
          return null; // Game not found
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error retrieving game: " + e.getMessage());
    }
  }


  public void updateGame(int gameID, String whiteUsername, String blackUsername) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      conn.setAutoCommit(false); // Start transaction

      String sql = "UPDATE Games SET whiteUsername = ?, blackUsername = ? WHERE game_id = ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, whiteUsername);
        stmt.setString(2, blackUsername);
        stmt.setInt(3, gameID);
        int affectedRows = stmt.executeUpdate();

        if (affectedRows == 0) {
          throw new DataAccessException("Game not found for update.");
        }

        conn.commit(); // Commit transaction
      } catch (SQLException e) {
        conn.rollback(); // Rollback on error
        throw new DataAccessException("Error updating game: " + e.getMessage());
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error updating game: " + e.getMessage());
    }
  }



  // In GameDAO.java - Ensure clearAllGames clears all game data and auth tokens
  public void clearAllGames() throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      conn.setAutoCommit(false); // Start transaction
      try (Statement stmt = conn.createStatement()) {
        stmt.executeUpdate("DELETE FROM Games");
        stmt.executeUpdate("DELETE FROM Users");
        stmt.executeUpdate("DELETE FROM AuthTokens"); // Clear auth tokens
        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        throw new DataAccessException("Error clearing data: " + e.getMessage());
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error clearing data: " + e.getMessage());
    }
  }


  // In GameDAO.java - Update listGames to ensure an empty list is returned if no games are found
  // In GameDAO.java - Update listGames to consistently return an empty list if no games are present
  public List<GameData> listGames() throws DataAccessException {
    List<GameData> games = new ArrayList<>();
    String sql = "SELECT * FROM Games";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        int gameID = rs.getInt("game_id");
        String gameName = rs.getString("gameName");
        String whiteUsername = rs.getString("whiteUsername");
        String blackUsername = rs.getString("blackUsername");

        GameData game = new GameData(gameID, whiteUsername, blackUsername, gameName);
        games.add(game);
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error listing games: " + e.getMessage());
    }
    return games; // Returns empty if no games are found
  }


}
