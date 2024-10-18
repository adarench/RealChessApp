package service;

import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import dataAccess.UserDAO;
import dataAccess.DataAccessException;

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
    userDAO.clearAllUsers();
    gameDAO.clearAllGames();
    authDAO.clearAllAuthTokens();
  }



}