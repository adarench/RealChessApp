package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class UserDAO {

  // In-memory user store (this will eventually be replaced by a database)
  private static final Map<String, UserData> USERS = new HashMap<>();

  // Method to create a new user
  public void createUser(UserData user) throws DataAccessException {
    if (USERS.containsKey(user.username())) {
      throw new DataAccessException("User already exists.");
    }
    USERS.put(user.username(), user);
  }

  // Method to get a user by username
  public UserData getUser(String username) throws DataAccessException {
    UserData user = USERS.get(username);
    if (user == null) {
      throw new DataAccessException("User not found.");
    }
    return user;
  }

  // Method to clear all users
  public void clearAllUsers() throws DataAccessException {
    USERS.clear();
  }
}
