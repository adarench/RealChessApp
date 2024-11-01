package service;

import model.GameData;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;

import java.util.List;

public class GameService {

  private final GameDAO gameDAO;
  private final AuthDAO authDAO;
  private static final boolean USE_DATABASE = true; // Toggle for database vs in-memory storage

  public GameService(GameDAO gameDAO, AuthDAO authDAO) {
    this.gameDAO = gameDAO;
    this.authDAO = authDAO;
  }

  // Create a new game with database support
  public GameData createGame(String authToken, String gameName) throws DataAccessException {
    // Ensure the user is authenticated
    AuthData auth = USE_DATABASE ? authDAO.getAuthFromDatabase(authToken) : authDAO.getAuth(authToken);
    if (auth == null) {
      throw new DataAccessException("Unauthorized.");
    }

    // Create a new game in either in-memory or database storage
    return USE_DATABASE ? gameDAO.createGameInDatabase(gameName) : gameDAO.createGame(gameName);
  }

  // List all games with database support
  public List<GameData> listGames(String authToken) throws DataAccessException {
    // Ensure the user is authenticated
    AuthData auth = USE_DATABASE ? authDAO.getAuthFromDatabase(authToken) : authDAO.getAuth(authToken);
    if (auth == null) {
      throw new DataAccessException("Unauthorized.");
    }

    // List games from either in-memory or database storage
    return USE_DATABASE ? gameDAO.listGamesFromDatabase() : gameDAO.listGames();
  }

  // Join a game with database support
  public void joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {
    if (playerColor == null || playerColor.isEmpty()) {
      throw new DataAccessException("Player color is required.");
    }
    if (authToken == null || authToken.isEmpty()) {
      throw new DataAccessException("Auth token is required.");
    }
    if (gameID <= 0) {
      throw new DataAccessException("Valid gameID is required.");
    }

    // Ensure the user is authenticated
    AuthData auth = USE_DATABASE ? authDAO.getAuthFromDatabase(authToken) : authDAO.getAuth(authToken);
    if (auth == null) {
      throw new DataAccessException("Unauthorized.");
    }

    // Retrieve the game
    GameData game = USE_DATABASE ? gameDAO.getGameFromDatabase(gameID) : gameDAO.getGame(gameID);

    // Add the player to the game (either as white or black)
    if (playerColor.equalsIgnoreCase("WHITE")) {
      if (game.whiteUsername() != null) {
        throw new DataAccessException("White player spot already taken.");
      }
      game = new GameData(game.gameID(), auth.username(), game.blackUsername(), game.gameName());
    } else if (playerColor.equalsIgnoreCase("BLACK")) {
      if (game.blackUsername() != null) {
        throw new DataAccessException("Black player spot already taken.");
      }
      game = new GameData(game.gameID(), game.whiteUsername(), auth.username(), game.gameName());
    } else {
      throw new DataAccessException("Invalid player color.");
    }

    // Update the game in either in-memory or database storage
    if (USE_DATABASE) {
      gameDAO.updateGameInDatabase(game);
    } else {
      gameDAO.updateGame(game);
    }
  }
}
