package service;

import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import dataAccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.GameService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

  private GameService gameService;
  private AuthDAO authDAO;
  private GameDAO gameDAO;

  @BeforeEach
  void setUp() {
    authDAO = new AuthDAO();
    gameDAO = new GameDAO();
    gameService = new GameService(gameDAO, authDAO);
  }

  // Test case for createGame
  @Test
  void testCreateGamePositive() throws DataAccessException {
    // Create an authenticated user
    AuthData auth = new AuthData("validToken", "user1");
    authDAO.createAuth(auth);

    // Test creating a game
    GameData game = gameService.createGame("validToken", "ChessGame");
    assertNotNull(game);
    assertEquals("ChessGame", game.gameName());
  }

  @Test
  void testCreateGameUnauthorized() {
    // Test creating a game with an invalid auth token
    DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
      gameService.createGame("invalidToken", "ChessGame");
    });
    assertEquals("Auth token not found.", thrown.getMessage());
  }

  // Test case for listGames
  @Test
  void testListGamesPositive() throws DataAccessException {
    // Create an authenticated user
    AuthData auth = new AuthData("validToken", "user1");
    authDAO.createAuth(auth);

    // Create a game
    gameService.createGame("validToken", "ChessGame");

    // Test listing games
    List<GameData> games = gameService.listGames("validToken");
    assertEquals(1, games.size());
    assertEquals("ChessGame", games.get(0).gameName());
  }

  @Test
  void testListGamesUnauthorized() {
    // Test listing games with an invalid auth token
    DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
      gameService.listGames("invalidToken");
    });
    assertEquals("Auth token not found.", thrown.getMessage());
  }

  // Test case for joinGame
  @Test
  void testJoinGamePositive() throws DataAccessException {
    // Create an authenticated user
    AuthData auth = new AuthData("validToken", "user1");
    authDAO.createAuth(auth);

    // Create a game
    GameData game = gameService.createGame("validToken", "ChessGame");

    // Test joining the game
    gameService.joinGame("validToken", game.gameID(), "WHITE");

    // Retrieve the game to check the user was added
    GameData updatedGame = gameDAO.getGame(game.gameID());
    assertEquals("user1", updatedGame.whiteUsername());
  }

  @Test
  void testJoinGameUnauthorized() {
    // Test joining a game with an invalid auth token
    DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
      gameService.joinGame("invalidToken", 1, "WHITE");
    });
    assertEquals("Auth token not found.", thrown.getMessage());
  }
}
