package service;

import model.GameData;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.UserData;

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
      throw new DataAccessException("Invalid auth token.");
    }

    // List all games
    return gameDAO.listGames();
  }


  // Join a game
  public void joinGame(String authToken, int gameID, String color) throws DataAccessException {
    AuthData auth = authDAO.getAuth(authToken);
    if (auth == null) {
      throw new DataAccessException("Unauthorized: Invalid auth token.");
    }

    GameData gameData = gameDAO.getGame(gameID);
    if (gameData == null) {
      throw new DataAccessException("Game not found.");
    }

    String username = auth.username();

    if (color.equalsIgnoreCase("white")) {
      if (gameData.whiteUsername() != null && !gameData.whiteUsername().isEmpty()) {
        throw new DataAccessException("Forbidden: White spot already taken.");
      }
      gameDAO.updateGame(gameID, username, gameData.blackUsername());
    } else if (color.equalsIgnoreCase("black")) {
      if (gameData.blackUsername() != null && !gameData.blackUsername().isEmpty()) {
        throw new DataAccessException("Forbidden: Black spot already taken.");
      }
      gameDAO.updateGame(gameID, gameData.whiteUsername(), username);
    } else {
      throw new DataAccessException("Invalid color specified.");
    }
  }


}
