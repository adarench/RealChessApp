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
      String sql = "INSERT INTO Games (gameName, gameState) VALUES (?, ?)";
      try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, gameName);
        stmt.setString(2, "{}");  // initial empty JSON for game state
        stmt.executeUpdate();
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
          return new GameData(rs.getInt(1), null, null, gameName);
        } else {
          throw new DataAccessException("Failed to create game.");
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException(e.getMessage());
    }
  }

  public GameData getGame(int gameID) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "SELECT * FROM Games WHERE gameID = ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, gameID);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
          return new GameData(rs.getInt("gameID"), rs.getString("whiteUsername"), rs.getString("blackUsername"), rs.getString("gameName"));
        } else {
          throw new DataAccessException("Game not found.");
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException(e.getMessage());
    }
  }

  public void updateGame(GameData game) throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "UPDATE Games SET gameState = ?, whiteUsername = ?, blackUsername = ? WHERE gameID = ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(2, game.whiteUsername());
        stmt.setString(3, game.blackUsername());
        stmt.setInt(4, game.gameID());
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException(e.getMessage());
    }
  }

  public void clearAllGames() throws DataAccessException {
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "DELETE FROM Games";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException(e.getMessage());
    }
  }

  public List<GameData> listGames() throws DataAccessException {
    List<GameData> games = new ArrayList<>();

    String sql = "SELECT * FROM Games";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        int gameID = rs.getInt("gameID");
        String gameName = rs.getString("gameName");
        String whiteUsername = rs.getString("whiteUsername");
        String blackUsername = rs.getString("blackUsername");

        GameData game = new GameData(gameID, whiteUsername, blackUsername, gameName);
        games.add(game);
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error listing games: " + e.getMessage());
    }

    return games;
  }
}
