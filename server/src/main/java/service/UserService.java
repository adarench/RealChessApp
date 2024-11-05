package service;

import model.AuthData;
import model.UserData;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class UserService{

  private final UserDAO userDAO;
  private final AuthDAO authDAO;

  public UserService(UserDAO userDAO, AuthDAO authDAO) {
    this.userDAO = userDAO;
    this.authDAO = authDAO;
  }

  //registration
  public AuthData register(UserData user) throws DataAccessException {
    // Check for existing username
    UserData existingUser = userDAO.getUser(user.username());
    if (existingUser != null) {
      throw new DataAccessException("Username already exists.");
    }

    // Register the user in the database
    userDAO.createUser(user);

    // Generate and store auth token
    String authToken = UUID.randomUUID().toString();
    AuthData authData = new AuthData(authToken, user.username());
    authDAO.createAuth(authData);

    return authData;
  }

  public AuthData login(String username, String password) throws DataAccessException {
    UserData user = userDAO.getUser(username);
    if (user == null) {
      throw new DataAccessException("User not found.");
    }

    // Verify password
    if (!BCrypt.checkpw(password, user.password())) {
      throw new DataAccessException("Invalid password.");
    }

    // Generate auth token
    String authToken = UUID.randomUUID().toString();
    AuthData authData = new AuthData(authToken, user.username());
    authDAO.createAuth(authData);

    return authData;
  }


  public void logout(String authToken) throws DataAccessException {
    if (authToken == null || authToken.isEmpty()) {
      throw new DataAccessException("Auth token not provided.");
    }

    // Check if the token exists before attempting deletion
    AuthData authData = authDAO.getAuth(authToken);
    if (authData == null) {
      throw new DataAccessException("Auth token not found or already invalidated.");
    }

    // Proceed to delete the token
    authDAO.deleteAuth(authToken);
  }


}