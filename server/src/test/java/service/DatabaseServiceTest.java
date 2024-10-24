package passoff.service;

import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import dataAccess.UserDAO;
import dataAccess.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.DatabaseService;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseServiceTest {

  private DatabaseService databaseService;
  private UserDAO userDAO;
  private GameDAO gameDAO;
  private AuthDAO authDAO;

  @BeforeEach
  public void setUp() {
    userDAO = new UserDAO();
    gameDAO = new GameDAO();
    authDAO = new AuthDAO();


    databaseService = new DatabaseService(userDAO, gameDAO, authDAO);
  }

  @Test
  public void testClearDatabasePositive() {
    try {

      databaseService.clearDatabase();


      assertTrue(true, "Database was cleared successfully without exceptions.");

    } catch (DataAccessException e) {
      fail("DataAccessException should not have been thrown: " + e.getMessage());
    }
  }

  @Test
  public void testClearDatabaseNegative() {

    try {

      userDAO = new UserDAO() {
        @Override
        public void clearAllUsers() throws DataAccessException {
          throw new DataAccessException("Simulated database failure");
        }
      };


      databaseService = new DatabaseService(userDAO, gameDAO, authDAO);


      databaseService.clearDatabase();

      fail("An expected DataAccessException was not thrown.");

    } catch (DataAccessException e) {

      assertEquals("Simulated database failure", e.getMessage(), "Exception message should match.");
    }
  }
}
