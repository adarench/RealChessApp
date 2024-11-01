package service;

import dataaccess.UserDAO;
import dataaccess.GameDAO;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import java.util.Set;
import model.AuthData;

public class DatabaseService {

  private final UserDAO userDAO;
  private final GameDAO gameDAO;
  private final AuthDAO authDAO;

  private static final boolean USE_DATABASE = true; // Toggle for database vs. in-memory storage

  public DatabaseService(UserDAO userDAO, GameDAO gameDAO, AuthDAO authDAO) {
    this.userDAO = userDAO;
    this.gameDAO = gameDAO;
    this.authDAO = authDAO;
  }

  // Clear all data, supporting both in-memory and database storage
  public void clearDatabase() throws DataAccessException {
    if (USE_DATABASE) {
      // Clear database tables by calling existing methods
      userDAO.clearAllUsersFromDatabase();
      gameDAO.clearAllGamesFromDatabase();

      // Retrieve all auth tokens and delete each from the database
      Set<String> authTokens = authDAO.getAllAuthTokens();
      for (String authToken : authTokens) {
        authDAO.deleteAuthFromDatabase(authToken);
      }
    }

    // Always clear in-memory storage to ensure compatibility with tests
    userDAO.clearAllUsers();
    gameDAO.clearAllGames();
    authDAO.clearAllAuthTokens();
  }
}
