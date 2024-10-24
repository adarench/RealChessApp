package dataAccess;

import model.GameData;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class GameDAO {

  // In-memory game store (will eventually be replaced by a database)
  private static final Map<Integer, GameData> games = new HashMap<>();
  private int nextGameID = 1;

  // Method to create a new game
  public GameData createGame(String gameName) throws DataAccessException {
    GameData game = new GameData(nextGameID++, null, null, gameName);
    games.put(game.gameID(), game);
    return game;
  }

  // Method to get a game by ID
  public GameData getGame(int gameID) throws DataAccessException {
    GameData game = games.get(gameID);
    if (game == null) {
      throw new DataAccessException("Game not found.");
    }
    return game;
  }

  // Method to list all games
  public List<GameData> listGames() throws DataAccessException {
    return new ArrayList<>(games.values());
  }

  // Method to update a game (for joining a game or updating game state)
  public void updateGame(GameData game) throws DataAccessException {
    if (!games.containsKey(game.gameID())) {
      throw new DataAccessException("Game not found.");
    }
    games.put(game.gameID(), game);
  }

  // Method to clear all games
  public void clearAllGames() throws DataAccessException {
    games.clear();
  }
}
