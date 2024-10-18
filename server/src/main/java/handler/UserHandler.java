package handler;

import service.UserService;
import model.UserData;
import model.AuthData;
import dataAccess.DataAccessException;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;

public class UserHandler{
  private final UserService userService;
  private final Gson gson = new Gson();

  public UserHandler(UserService userService){
    this.userService = userService;
  }

  //user registration
  public Route register = (Request req, Response res) -> {
    try {
      // Parse request body into UserData
      UserData userData = gson.fromJson(req.body(), UserData.class);

      // Register the user using the UserService
      AuthData authData = userService.register(userData);

      // Return success response with the auth token
      res.status(200);
      return gson.toJson(authData);
    } catch (DataAccessException e) {
      res.status(400);
      return gson.toJson(new ErrorResponse(e.getMessage()));
    }
  };

  // Handle user login (/session POST)
  public Route login = (Request req, Response res) -> {
    try {
      // Parse request body for login credentials
      LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);

      // Log in the user using the UserService
      AuthData authData = userService.login(loginRequest.username, loginRequest.password);

      // Return success response with the auth token
      res.status(200);
      return gson.toJson(authData);
    } catch (DataAccessException e) {
      res.status(401); // Unauthorized
      return gson.toJson(new ErrorResponse(e.getMessage()));
    }
  };

  // Handle user logout (/session DELETE)
  public Route logout = (Request req, Response res) -> {
    try {
      // Extract the auth token from the headers
      String authToken = req.headers("Authorization");

      // Log out the user using the UserService
      userService.logout(authToken);

      // Return success response
      res.status(200);
      return gson.toJson(new SuccessResponse(true));
    } catch (DataAccessException e) {
      res.status(401); // Unauthorized
      return gson.toJson(new ErrorResponse(e.getMessage()));
    }
  };
  // Inner classes for LoginRequest and responses
  private static class LoginRequest {
    String username;
    String password;
  }

  private static class ErrorResponse {
    String message;
    ErrorResponse(String message) {
      this.message = message;
    }
  }

  private static class SuccessResponse {
    boolean success;
    SuccessResponse(boolean success) {
      this.success = success;
    }
  }
}

