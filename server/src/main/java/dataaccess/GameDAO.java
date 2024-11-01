package dataaccess;

import model.GameData;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GameDAO {

  // In-memory game store (will eventually be replaced by a database)
  private static final Map<Integer, GameData> GAMES= new HashMap<>();
  private int nextGameID = 1;

  // Method to create a new game
  public GameData createGame(String gameName) throws DataAccessException {
    GameData game = new GameData(nextGameID++, null, null, gameName);
    GAMES.put(game.gameID(), game);
    return game;
  }
  // Database-backed method to create a new game
  public GameData createGameInDatabase(String gameName) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "INSERT INTO Games (game_name) VALUES (?)";
      try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, gameName);
        int affectedRows = stmt.executeUpdate();

        if (affectedRows == 0) {
          throw new DataAccessException("Creating user failed, no rows affected.");
        }

        // Retrieve the auto-generated game ID
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            int gameId = generatedKeys.getInt(1);
            return new GameData(gameId, null, null, gameName);
          } else {
            throw new DataAccessException("Failed to retrieve game ID.");
          }
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error creating game in database: " + e.getMessage());
    }
  }
  // Method to get a game by ID
  public GameData getGame(int gameID) throws DataAccessException {
    GameData game = GAMES.get(gameID);
    if (game == null) {
      throw new DataAccessException("Game not found.");
    }
    return game;
  }
  // Database-backed method to get a game by ID
  public GameData getGameFromDatabase(int gameID) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "SELECT * FROM Games WHERE game_id = ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, gameID);
        try (ResultSet rs = stmt.executeQuery()) {
          if (rs.next()) {
            return new GameData(rs.getInt("game_id"), null, null, rs.getString("game_name"));
          } else {
            throw new DataAccessException("Game not found.");
          }
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error retrieving game from database: " + e.getMessage());
    }
  }

  // Method to list all games
  public List<GameData> listGames() throws DataAccessException {
    return new ArrayList<>(GAMES.values());
  }
  // List all games from the database
  public List<GameData> listGamesFromDatabase() throws DataAccessException {
    List<GameData> games = new ArrayList<>();
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "SELECT * FROM Games";
      try (PreparedStatement stmt = conn.prepareStatement(sql);
           ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          games.add(new GameData(rs.getInt("game_id"), null, null, rs.getString("game_name")));
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error listing games from database: " + e.getMessage());
    }
    return games;
  }

  // Method to update a game (for joining a game or updating game state)
  public void updateGame(GameData game) throws DataAccessException {
    if (!GAMES.containsKey(game.gameID())) {
      throw new DataAccessException("Game not found.");
    }
    GAMES.put(game.gameID(), game);
  }

  // Method to clear all games
  public void clearAllGames() throws DataAccessException {
    GAMES.clear();
  }
  // Clear all games from the database
  public void clearAllGamesFromDatabase() throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "DELETE FROM Games";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error clearing games from database: " + e.getMessage());
    }
  }
  public void updateGameInDatabase(GameData game) throws DataAccessException {
    // Implement SQL update for updating a game in the database
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "UPDATE Games SET white_username = ?, black_username = ? game_state = ? WHERE game_id = ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, game.whiteUsername());
        stmt.setString(2, game.blackUsername());
        stmt.setInt(3, game.gameID());
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error updating game in database: " + e.getMessage());
    }
  }
}
