package service;

import model.AuthData;
import model.UserData;
import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.UserDAO;

import java.util.UUID;

public class UserService{

  private final UserDAO userDAO;
  private final AuthDAO authDAO;

  public UserService(UserDAO userDAO, AuthDAO authDAO) {
    this.userDAO = userDAO;
    this.authDAO = authDAO;
  }

  //registration
  public AuthData register(UserData user) throws DataAccessException{
    if(userDAO.getUser(user.username()) != null){
      throw new DataAccessException("User already exists");
    }
    userDAO.createUser(user);
    String authToken = UUID.randomUUID().toString();
    AuthData authData = new AuthData(authToken, user.username());
    authDAO.createAuth(authData);

    return authData;
  }
  public AuthData login(String username, String password) throws DataAccessException{
    UserData user = userDAO.getUser(username);

    // Validate the password
    if (!user.password().equals(password)) {
      throw new DataAccessException("Invalid password.");
    }

    // Generate a new auth token
    String authToken = UUID.randomUUID().toString();
    AuthData authData = new AuthData(authToken, user.username());
    authDAO.createAuth(authData);

    return authData;
  }

  /*public void logout(String authToken) throws DataAccessException{
    authDAO.deleteAuth(authToken);
  }*/
}