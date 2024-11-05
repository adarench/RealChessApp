package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import dataaccess.DataAccessException;

public class DatabaseService{
  private final UserDAO userDAO;
  private final GameDAO gameDAO;
  private final AuthDAO authDAO;

  public DatabaseService(UserDAO userDAO, GameDAO gameDAO, AuthDAO authDAO) {
    this.userDAO = userDAO;
    this.gameDAO = gameDAO;
    this.authDAO = authDAO;
  }
  // clear database records
  public void clearDatabase() throws DataAccessException {
    authDAO.clearAllAuthTokens();
    gameDAO.clearAllGames();
    userDAO.clearAllUsers();
  }



}