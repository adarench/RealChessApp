package service;

import model.GameData;
import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.GameDAO;
import model.AuthData;

import java.util.List;

public class GameService {

  private final GameDAO gameDAO;
  private final AuthDAO authDAO;

  public GameService(GameDAO gameDAO, AuthDAO authDAO) {
    this.gameDAO = gameDAO;
    this.authDAO = authDAO;
  }

  // Create a new game
  public GameData createGame(String authToken, String gameName) throws DataAccessException {
    // Ensure the user is authenticated
    AuthData auth = authDAO.getAuth(authToken);

    if (auth == null) {
      throw new DataAccessException("Unauthorized.");
    }

    // Create a new game
    return gameDAO.createGame(gameName);
  }

  // List all games
  public List<GameData> listGames(String authToken) throws DataAccessException {
    // Ensure the user is authenticated
    AuthData auth = authDAO.getAuth(authToken);
    if (auth == null) {
      throw new DataAccessException("Unauthorized.");
    }

    // List all games
    return gameDAO.listGames();
  }

  // Join a game
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
    AuthData auth = authDAO.getAuth(authToken);
    if (auth == null) {
      throw new DataAccessException("Unauthorized.");
    }

    // Retrieve the game
    GameData game = gameDAO.getGame(gameID);

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

    // Update the game
    gameDAO.updateGame(game);
  }
}
