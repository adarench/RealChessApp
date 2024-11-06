package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTest {

  private AuthDAO authDAO;
  private GameDAO gameDAO;
  private UserDAO userDAO;
  private DatabaseService databaseService;
  private GameService gameService;
  private UserService userService;

  @BeforeEach
  public void setUp() throws DataAccessException {
    // Initialize DAOs and services
    authDAO = new AuthDAO();
    gameDAO = new GameDAO();
    userDAO = new UserDAO();

    // Initialize service classes
    databaseService = new DatabaseService(userDAO, gameDAO, authDAO);
    gameService = new GameService(gameDAO, authDAO);
    userService = new UserService(userDAO, authDAO);

    // Clear database before each test
    databaseService.clearDatabase();
  }

  @AfterEach
  public void tearDown() throws DataAccessException {
    // Clear database after each test
    databaseService.clearDatabase();
  }

  // DatabaseService Tests
  @Test
  public void testClearDatabasePositive() throws DataAccessException {
    // Populate with some data
    userDAO.createUser(new UserData("user1", "password", "user1@example.com"));
    gameDAO.createGame("Test Game");

    // Clear the database
    databaseService.clearDatabase();

    // Assert that data has been cleared
    assertNull(userDAO.getUser("user1"));
    assertTrue(gameDAO.listGames().isEmpty());
  }

  @Test
  public void testClearDatabaseNegative() {
    // Attempt to clear database without populated data (shouldn't throw an exception)
    assertDoesNotThrow(() -> databaseService.clearDatabase());
  }

  // GameService Tests
  @Test
  public void testCreateGamePositive() throws DataAccessException {
    // Create user and auth token
    UserData user = new UserData("user1", "password", "user1@example.com");
    userDAO.createUser(user);
    String authToken = UUID.randomUUID().toString();
    authDAO.createAuth(new AuthData(authToken, user.username()));

    // Create game with valid auth token
    GameData gameData = gameService.createGame(authToken, "Chess Game");

    // Assert game creation
    assertNotNull(gameData);
    assertEquals("Chess Game", gameData.gameName());
  }

  @Test
  public void testCreateGameNegative() {
    // Attempt to create a game with an invalid token
    String invalidToken = "invalid_token";
    assertThrows(DataAccessException.class, () -> gameService.createGame(invalidToken, "Chess Game"));
  }

  // UserService Tests
  @Test
  public void testRegisterPositive() throws DataAccessException {
    // Register a new user
    UserData user = new UserData("newUser", "password", "newUser@example.com");
    AuthData authData = userService.register(user);

    // Assert that user is registered and auth token created
    assertNotNull(authData);
    assertEquals("newUser", authData.username());
    assertNotNull(authDAO.getAuth(authData.authToken()));
  }

  @Test
  public void testRegisterNegative() throws DataAccessException {
    // Create a user and attempt to register with the same username
    UserData user = new UserData("existingUser", "password", "existingUser@example.com");
    userDAO.createUser(user);

    // Attempt to register a duplicate username
    assertThrows(DataAccessException.class, () -> userService.register(user));
  }

  // Additional tests for `UserService` login and logout
  @Test
  public void testLoginPositive() throws DataAccessException {
    // Register and then login with correct credentials
    UserData user = new UserData("user1", "password", "user1@example.com");
    userService.register(user);

    // Login with correct credentials
    AuthData authData = userService.login("user1", "password");

    // Assert successful login and token generation
    assertNotNull(authData);
    assertEquals("user1", authData.username());
  }

  @Test
  public void testLoginNegative() throws DataAccessException {
    // Attempt login with incorrect credentials
    UserData user = new UserData("user2", "password", "user2@example.com");
    userDAO.createUser(user);

    // Login with incorrect password
    assertThrows(DataAccessException.class, () -> userService.login("user2", "wrongpassword"));
  }

  @Test
  public void testLogoutPositive() throws DataAccessException {
    // Register user, login, and logout with valid auth token
    UserData user = new UserData("user3", "password", "user3@example.com");
    userService.register(user);
    AuthData authData = userService.login("user3", "password");

    // Perform logout
    userService.logout(authData.authToken());

    // Assert token is deleted
    assertNull(authDAO.getAuth(authData.authToken()));
  }

  @Test
  public void testLogoutNegative() {
    // Attempt to logout with invalid token
    assertThrows(DataAccessException.class, () -> userService.logout("invalid_token"));
  }
}
