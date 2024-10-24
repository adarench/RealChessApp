package service;

import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

  private UserService userService;
  private AuthDAO authDAO;
  private UserDAO userDAO;

  @BeforeEach
  public void setUp() {
    authDAO = new AuthDAO();
    userDAO = new UserDAO();
    userService = new UserService(userDAO, authDAO);
    try {
      userDAO.clearAllUsers(); // Clear the in-memory user store
    } catch (DataAccessException e) {
      e.printStackTrace(); // Optionally handle the exception or log it
      fail("Failed to clear users in setup: " + e.getMessage()); // Fail the test if necessary
    }
  }


  @Test
  public void testRegisterPositive() throws DataAccessException {
    UserData user = new UserData("uniqueUser", "password", "uniqueUser@example.com");
    AuthData authData = userService.register(user);

    assertNotNull(authData);
    assertEquals("uniqueUser", authData.username());
    assertNotNull(authData.authToken());
  }

  @Test
  public void testRegisterNegative() throws DataAccessException {
    UserData user = new UserData("duplicateUser", "password", "duplicateUser@example.com");
    userService.register(user);

    DataAccessException exception = assertThrows(DataAccessException.class, () -> {
      userService.register(user);
    });

    assertEquals("User already exists.", exception.getMessage());
  }

  @Test
  public void testLoginPositive() throws DataAccessException {
    UserData user = new UserData("testLoginUser", "password", "testLoginUser@example.com");
    userService.register(user);

    AuthData authData = userService.login("testLoginUser", "password");

    assertNotNull(authData);
    assertEquals("testLoginUser", authData.username());
  }

  @Test
  public void testLoginNegative() throws DataAccessException {
    UserData user = new UserData("testLoginUserFail", "password", "testLoginUserFail@example.com");
    userService.register(user);

    DataAccessException exception = assertThrows(DataAccessException.class, () -> {
      userService.login("testLoginUserFail", "wrongPassword");
    });

    assertEquals("Invalid password.", exception.getMessage());
  }

  @Test
  public void testLogoutPositive() throws DataAccessException {
    UserData user = new UserData("testLogoutUser", "password", "testLogoutUser@example.com");
    AuthData authData = userService.register(user);

    userService.logout(authData.authToken());

    DataAccessException exception = assertThrows(DataAccessException.class, () -> {
      authDAO.getAuth(authData.authToken());
    });

    assertEquals("Auth token not found.", exception.getMessage());
  }

  @Test
  public void testLogoutNegative() {
    DataAccessException exception = assertThrows(DataAccessException.class, () -> {
      userService.logout("nonExistentToken");
    });

    assertEquals("Auth token not found.", exception.getMessage());
  }
}
