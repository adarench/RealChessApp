package dataAccess;

import model.AuthData;
import java.util.HashMap;
import java.util.Map;

public class AuthDAO {

  // In-memory auth store (will eventually be replaced by a database)
  private static final Map<String, AuthData> authTokens = new HashMap<>();

  // Method to create a new auth token
  public void createAuth(AuthData auth) throws DataAccessException {
    authTokens.put(auth.authToken(), auth);
  }

  // Method to get an auth token
  public AuthData getAuth(String authToken) throws DataAccessException {
    AuthData auth = authTokens.get(authToken);
    if (auth == null) {
      throw new DataAccessException("Auth token not found.");
    }
    return auth;
  }

  // Method to delete an auth token (for logging out)
  public void deleteAuth(String authToken) throws DataAccessException {
    if (!authTokens.containsKey(authToken)) {
      throw new DataAccessException("Auth token not found.");
    }
    authTokens.remove(authToken);
  }

  // Method to clear all auth tokens
  public void clearAllAuthTokens() throws DataAccessException {
    authTokens.clear();
  }
}
