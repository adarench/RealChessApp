package passoff.server;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import dataaccess.DataAccessException;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DAOTest {

  private AuthDAO authDAO;
  private GameDAO gameDAO;
  private UserDAO userDAO;

  @BeforeEach
  public void setUp() throws DataAccessException {
    authDAO = new AuthDAO();
    gameDAO = new GameDAO();
    userDAO = new UserDAO();

    // Clear all existing data before each test
    authDAO.clearAllAuthTokens();
    gameDAO.clearAllGames();
    userDAO.clearAllUsers();
  }

  // AuthDAO Tests
  @Test
  public void testCreateAuthPositive() throws DataAccessException {
    UserData user = new UserData("user1", "password", "user1@example.com");
    userDAO.createUser(user);

    AuthData authData = new AuthData(UUID.randomUUID().toString(), "user1");
    authDAO.createAuth(authData);

    AuthData retrievedAuth = authDAO.getAuth(authData.authToken());
    assertNotNull(retrievedAuth);
    assertEquals(authData.username(), retrievedAuth.username());
  }

  @Test
  public void testCreateAuthNegative() {
    assertThrows(NullPointerException.class, () -> authDAO.createAuth(null));
  }

  @Test
  public void testGetAuthPositive() throws DataAccessException {
    UserData user = new UserData("user2", "password", "user2@example.com");
    userDAO.createUser(user);

    AuthData authData = new AuthData(UUID.randomUUID().toString(), "user2");
    authDAO.createAuth(authData);

    AuthData retrievedAuth = authDAO.getAuth(authData.authToken());
    assertNotNull(retrievedAuth);
    assertEquals("user2", retrievedAuth.username());
  }

  @Test
  public void testGetAuthNegative() throws DataAccessException {
    AuthData retrievedAuth = authDAO.getAuth("non_existent_token");
    assertNull(retrievedAuth);
  }

  @Test
  public void testDeleteAuthPositive() throws DataAccessException {
    UserData user = new UserData("user3", "password", "user3@example.com");
    userDAO.createUser(user);

    AuthData authData = new AuthData(UUID.randomUUID().toString(), "user3");
    authDAO.createAuth(authData);

    authDAO.deleteAuth(authData.authToken());
    assertNull(authDAO.getAuth(authData.authToken()));
  }

  @Test
  public void testDeleteAuthNegative() {
    assertThrows(DataAccessException.class, () -> authDAO.deleteAuth("invalid_token"));
  }

  @Test
  public void testClearAllAuthTokensPositive() throws DataAccessException {
    UserData user = new UserData("user4", "password", "user4@example.com");
    userDAO.createUser(user);

    authDAO.createAuth(new AuthData(UUID.randomUUID().toString(), "user4"));
    authDAO.clearAllAuthTokens();

    assertNull(authDAO.getAuth("user4"));
  }

  @Test
  public void testClearAllAuthTokensNegative() {
    assertDoesNotThrow(() -> authDAO.clearAllAuthTokens());
  }

  // GameDAO Tests
  @Test
  public void testCreateGamePositive() throws DataAccessException {
    GameData gameData = gameDAO.createGame("Test Game");
    assertNotNull(gameData);
    assertEquals("Test Game", gameData.gameName());
  }

  @Test
  public void testCreateGameNegative() {
    assertThrows(DataAccessException.class, () -> gameDAO.createGame(null));
  }

  @Test
  public void testGetGamePositive() throws DataAccessException {
    GameData gameData = gameDAO.createGame("Another Game");
    GameData retrievedGame = gameDAO.getGame(gameData.gameID());
    assertNotNull(retrievedGame);
    assertEquals("Another Game", retrievedGame.gameName());
  }

  @Test
  public void testGetGameNegative() throws DataAccessException {
    GameData retrievedGame = gameDAO.getGame(9999);
    assertNull(retrievedGame);
  }

  @Test
  public void testUpdateGamePositive() throws DataAccessException {
    UserData user1 = new UserData("user1", "password", "user1@example.com");
    UserData user2 = new UserData("user2", "password", "user2@example.com");
    userDAO.createUser(user1);
    userDAO.createUser(user2);

    GameData gameData = gameDAO.createGame("Game to Update");
    gameDAO.updateGame(gameData.gameID(), "user1", "user2");

    GameData updatedGame = gameDAO.getGame(gameData.gameID());
    assertEquals("user1", updatedGame.whiteUsername());
    assertEquals("user2", updatedGame.blackUsername());
  }

  @Test
  public void testUpdateGameNegative() {
    assertThrows(DataAccessException.class, () -> gameDAO.updateGame(9999, "user1", "user2"));
  }

  @Test
  public void testListGamesPositive() throws DataAccessException {
    gameDAO.createGame("Game 1");
    gameDAO.createGame("Game 2");

    List<GameData> games = gameDAO.listGames();
    assertEquals(2, games.size());
  }

  @Test
  public void testListGamesNegative() throws DataAccessException {
    List<GameData> games = gameDAO.listGames();
    assertTrue(games.isEmpty());
  }

  @Test
  public void testClearAllGamesPositive() throws DataAccessException {
    gameDAO.createGame("Game to Clear");
    gameDAO.clearAllGames();
    assertTrue(gameDAO.listGames().isEmpty());
  }

  @Test
  public void testClearAllGamesNegative() {
    assertDoesNotThrow(() -> gameDAO.clearAllGames());
  }

  // UserDAO Tests
  @Test
  public void testCreateUserPositive() throws DataAccessException {
    UserData user = new UserData("user1", "password", "user1@example.com");
    userDAO.createUser(user);

    UserData retrievedUser = userDAO.getUser("user1");
    assertNotNull(retrievedUser);
    assertEquals("user1@example.com", retrievedUser.email());
  }

  @Test
  public void testCreateUserNegative() {
    assertThrows(NullPointerException.class, () -> userDAO.createUser(null));
  }

  @Test
  public void testGetUserPositive() throws DataAccessException {
    UserData user = new UserData("user2", "password", "user2@example.com");
    userDAO.createUser(user);

    UserData retrievedUser = userDAO.getUser("user2");
    assertNotNull(retrievedUser);
    assertEquals("user2@example.com", retrievedUser.email());
  }

  @Test
  public void testGetUserNegative() throws DataAccessException {
    UserData retrievedUser = userDAO.getUser("non_existent_user");
    assertNull(retrievedUser);
  }

  @Test
  public void testDeleteUserPositive() throws DataAccessException {
    UserData user = new UserData("user3", "password", "user3@example.com");
    userDAO.createUser(user);

    userDAO.deleteUser("user3");
    assertNull(userDAO.getUser("user3"));
  }

  @Test
  public void testDeleteUserNegative() {
    assertDoesNotThrow(() -> userDAO.deleteUser("non_existent_user"));
  }

  @Test
  public void testClearAllUsersPositive() throws DataAccessException {
    userDAO.createUser(new UserData("user4", "password", "user4@example.com"));
    userDAO.clearAllUsers();

    assertNull(userDAO.getUser("user4"));
  }

  @Test
  public void testClearAllUsersNegative() {
    assertDoesNotThrow(() -> userDAO.clearAllUsers());
  }
}