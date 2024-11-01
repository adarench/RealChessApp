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
  private static final boolean USE_DATABASE = true; // Toggle to switch between in-memory and database storage

  public UserService(UserDAO userDAO, AuthDAO authDAO) {
    this.userDAO = userDAO;
    this.authDAO = authDAO;
  }

  //registration
  public AuthData register(UserData user) throws DataAccessException{

    // Generate a hashed password if using database storage
    if (USE_DATABASE) {
      String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
      UserData userWithHashedPassword = new UserData(user.username(), hashedPassword, user.email());
      userDAO.createUserInDatabase(userWithHashedPassword);
    } else {
      // In-memory storage (no hashing required)
      userDAO.createUser(user);
    }

    String authToken = UUID.randomUUID().toString();
    AuthData authData = new AuthData(authToken, user.username());
    if (USE_DATABASE) {
      authDAO.createAuthInDatabase(authData);
    } else {
      authDAO.createAuth(authData);
    }

    return authData;
  }
  public AuthData login(String username, String password) throws DataAccessException{
    UserData user = USE_DATABASE ? userDAO.getUserFromDatabase(username) : userDAO.getUser(username);

    // Validate the password
    if (USE_DATABASE) {
      if (!BCrypt.checkpw(password, user.password())) {
        throw new DataAccessException("Invalid password.");
      }
    } else {
      if (!user.password().equals(password)) {
        throw new DataAccessException("Invalid password.");
      }
    }

    // Generate a new auth token
    String authToken = UUID.randomUUID().toString();
    AuthData authData = new AuthData(authToken, user.username());

    if (USE_DATABASE) {
      authDAO.createAuthInDatabase(authData);
    } else {
      authDAO.createAuth(authData);
    }

    return authData;
  }

  public void logout(String authToken) throws DataAccessException{
    if (USE_DATABASE) {
      authDAO.deleteAuthFromDatabase(authToken);
    } else {
      authDAO.deleteAuth(authToken);
    }
  }
}