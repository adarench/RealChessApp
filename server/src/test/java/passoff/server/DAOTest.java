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
    // Ensure user exists before creating auth token
    UserData user = new UserData("user1", "password", "user1@example.com");
    userDAO.createUser(user);

    AuthData authData = new AuthData(UUID.randomUUID().toString(), "user1");
    authDAO.createAuth(authData);

    // Verify auth token creation
    AuthData retrievedAuth = authDAO.getAuth(authData.authToken());
    assertNotNull(retrievedAuth);
    assertEquals(authData.username(), retrievedAuth.username());
  }

  @Test
  public void testCreateAuthNegative() {
    // Attempt to create a null auth token
    assertThrows(NullPointerException.class, () -> authDAO.createAuth(null));
  }

  @Test
  public void testGetAuthPositive() throws DataAccessException {
    // Ensure user exists before creating auth token
    UserData user = new UserData("user2", "password", "user2@example.com");
    userDAO.createUser(user);

    AuthData authData = new AuthData(UUID.randomUUID().toString(), "user2");
    authDAO.createAuth(authData);

    // Retrieve and verify the existing auth token
    AuthData retrievedAuth = authDAO.getAuth(authData.authToken());
    assertNotNull(retrievedAuth);
    assertEquals("user2", retrievedAuth.username());
  }

  @Test
  public void testGetAuthNegative() throws DataAccessException {
    // Attempt to retrieve a non-existent auth token
    AuthData retrievedAuth = authDAO.getAuth("non_existent_token");
    assertNull(retrievedAuth);
  }

  @Test
  public void testDeleteAuthPositive() throws DataAccessException {
    // Ensure user exists before creating auth token
    UserData user = new UserData("user3", "password", "user3@example.com");
    userDAO.createUser(user);

    AuthData authData = new AuthData(UUID.randomUUID().toString(), "user3");
    authDAO.createAuth(authData);

    // Delete auth token and verify deletion
    authDAO.deleteAuth(authData.authToken());
    assertNull(authDAO.getAuth(authData.authToken()));
  }

  @Test
  public void testDeleteAuthNegative() {
    // Attempt to delete a non-existent auth token
    assertThrows(DataAccessException.class, () -> authDAO.deleteAuth("invalid_token"));
  }

  @Test
  public void testClearAllAuthTokensPositive() throws DataAccessException {
    // Ensure user exists before creating auth token
    UserData user = new UserData("user4", "password", "user4@example.com");
    userDAO.createUser(user);

    authDAO.createAuth(new AuthData(UUID.randomUUID().toString(), "user4"));
    authDAO.clearAllAuthTokens();

    // Assert all auth tokens are cleared
    assertNull(authDAO.getAuth("user4"));
  }

  @Test
  public void testClearAllAuthTokensNegative() {
    // Clear when no auth tokens exist, should not throw exception
    assertDoesNotThrow(() -> authDAO.clearAllAuthTokens());
  }

  // GameDAO Tests
  @Test
  public void testCreateGamePositive() throws DataAccessException {
    GameData gameData = gameDAO.createGame("Test Game");

    // Verify game creation
    assertNotNull(gameData);
    assertEquals("Test Game", gameData.gameName());
  }

  @Test
  public void testCreateGameNegative() {
    // Attempt to create a game with a null name
    assertThrows(DataAccessException.class, () -> gameDAO.createGame(null));
  }

  @Test
  public void testGetGamePositive() throws DataAccessException {
    GameData gameData = gameDAO.createGame("Another Game");

    // Retrieve and verify the created game
    GameData retrievedGame = gameDAO.getGame(gameData.gameID());
    assertNotNull(retrievedGame);
    assertEquals("Another Game", retrievedGame.gameName());
  }

  @Test
  public void testGetGameNegative() throws DataAccessException {
    // Attempt to retrieve a non-existent game
    GameData retrievedGame = gameDAO.getGame(9999); // assuming 9999 is not a valid ID
    assertNull(retrievedGame);
  }

  @Test
  public void testUpdateGamePositive() throws DataAccessException {
    GameData gameData = gameDAO.createGame("Chess Game");
    gameDAO.updateGame(gameData.gameID(), "user1", "user2");

    // Verify game update
    GameData updatedGame = gameDAO.getGame(gameData.gameID());
    assertEquals("user1", updatedGame.whiteUsername());
    assertEquals("user2", updatedGame.blackUsername());
  }

  @Test
  public void testUpdateGameNegative() {
    // Attempt to update a non-existent game
    assertThrows(DataAccessException.class, () -> gameDAO.updateGame(9999, "user1", "user2"));
  }

  @Test
  public void testListGamesPositive() throws DataAccessException {
    gameDAO.createGame("Game 1");
    gameDAO.createGame("Game 2");

    // Verify listing of games
    List<GameData> games = gameDAO.listGames();
    assertEquals(2, games.size());
  }

  @Test
  public void testListGamesNegative() throws DataAccessException {
    // List games when none exist
    List<GameData> games = gameDAO.listGames();
    assertTrue(games.isEmpty());
  }

  @Test
  public void testClearAllGamesPositive() throws DataAccessException {
    gameDAO.createGame("Game to Clear");
    gameDAO.clearAllGames();

    // Assert all games are cleared
    assertTrue(gameDAO.listGames().isEmpty());
  }

  @Test
  public void testClearAllGamesNegative() {
    // Clear games when none exist, should not throw exception
    assertDoesNotThrow(() -> gameDAO.clearAllGames());
  }

  // UserDAO Tests
  @Test
  public void testCreateUserPositive() throws DataAccessException {
    UserData user = new UserData("user1", "password", "user1@example.com");
    userDAO.createUser(user);

    // Verify user creation
    UserData retrievedUser = userDAO.getUser("user1");
    assertNotNull(retrievedUser);
    assertEquals("user1@example.com", retrievedUser.email());
  }

  @Test
  public void testCreateUserNegative() {
    // Attempt to create a null user
    assertThrows(NullPointerException.class, () -> userDAO.createUser(null));
  }

  @Test
  public void testGetUserPositive() throws DataAccessException {
    UserData user = new UserData("user2", "password", "user2@example.com");
    userDAO.createUser(user);

    // Retrieve existing user
    UserData retrievedUser = userDAO.getUser("user2");
    assertNotNull(retrievedUser);
    assertEquals("user2@example.com", retrievedUser.email());
  }

  @Test
  public void testGetUserNegative() throws DataAccessException {
    // Attempt to retrieve a non-existent user
    UserData retrievedUser = userDAO.getUser("non_existent_user");
    assertNull(retrievedUser);
  }

  @Test
  public void testDeleteUserPositive() throws DataAccessException {
    UserData user = new UserData("user3", "password", "user3@example.com");
    userDAO.createUser(user);

    // Delete user and verify deletion
    userDAO.deleteUser("user3");
    assertNull(userDAO.getUser("user3"));
  }

  @Test
  public void testDeleteUserNegative() throws DataAccessException {
    // Attempt to delete a non-existent user and verify user remains non-existent
    userDAO.deleteUser("non_existent_user");
    assertNull(userDAO.getUser("non_existent_user"));
  }


  @Test
  public void testClearAllUsersPositive() throws DataAccessException {
    userDAO.createUser(new UserData("user4", "password", "user4@example.com"));
    userDAO.clearAllUsers();

    // Assert all users are cleared
    assertNull(userDAO.getUser("user4"));
  }

  @Test
  public void testClearAllUsersNegative() {
    // Clear users when none exist, should not throw exception
    assertDoesNotThrow(() -> userDAO.clearAllUsers());
  }
}