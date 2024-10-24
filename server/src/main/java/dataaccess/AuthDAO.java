package dataaccess;

import model.AuthData;
import java.util.HashMap;
import java.util.Map;

public class AuthDAO {

  // In-memory auth store (will eventually be replaced by a database)
  private static final Map<String, AuthData> AUTH_TOKENS = new HashMap<>();

  // Method to create a new auth token
  public void createAuth(AuthData auth) throws DataAccessException {
    AUTH_TOKENS.put(auth.authToken(), auth);
  }

  // Method to get an auth token
  public AuthData getAuth(String authToken) throws DataAccessException {
    AuthData auth = AUTH_TOKENS.get(authToken);
    if (auth == null) {
      throw new DataAccessException("Auth token not found.");
    }
    return auth;
  }

  // Method to delete an auth token (for logging out)
  public void deleteAuth(String authToken) throws DataAccessException {
    if (!AUTH_TOKENS.containsKey(authToken)) {
      throw new DataAccessException("Auth token not found.");
    }
    AUTH_TOKENS.remove(authToken);
  }

  // Method to clear all auth tokens
  public void clearAllAuthTokens() throws DataAccessException {
    AUTH_TOKENS.clear();
  }
}
