package handler;

import service.UserService;
import model.UserData;
import model.AuthData;
import dataaccess.DataAccessException;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;

public class UserHandler{
  private UserService userService;
  private final Gson gson = new Gson();

  public UserHandler(UserService userService){

    this.userService = userService;
  }

  //user registration
  public Route register = (Request req, Response res) -> {
    res.type("application/json");

    try {
      UserData userData = gson.fromJson(req.body(), UserData.class);

      if (userData == null || userData.username() == null || userData.username().isEmpty()
              || userData.password() == null || userData.password().isEmpty()
              || userData.email() == null || userData.email().isEmpty()) {
        res.status(400);
        return gson.toJson(new ErrorResponse("Error: Missing or invalid fields"));
      }

      AuthData authData = userService.register(userData);
      res.status(200);
      return gson.toJson(authData);

    } catch (DataAccessException e) {
      if (e.getMessage().contains("Username already exists")) {
        res.status(403);
        return gson.toJson(new ErrorResponse("Error: Username already exists"));
      }
      res.status(500);
      return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
    }
  };




  // handle user login (/session POST)
  // handle user login (/session POST)
  public Route login = (Request req, Response res) -> {
    res.type("application/json");

    try {
      // Parse the request body
      UserData userData = gson.fromJson(req.body(), UserData.class);

      if (userData == null ||
              userData.username() == null || userData.username().isEmpty() ||
              userData.password() == null || userData.password().isEmpty()) {
        res.status(400);
        return gson.toJson(new ErrorResponse("Error: Missing or invalid fields"));
      }

      // Authenticate the user
      AuthData authData = userService.login(userData.username(), userData.password());

      if (authData == null) {
        res.status(401);
        return gson.toJson(new ErrorResponse("Error: Invalid username or password"));
      }

      res.status(200);
      return gson.toJson(authData);

    } catch (DataAccessException e) {
      if (e.getMessage().contains("User not found") || e.getMessage().contains("Invalid password")) {
        res.status(401); // Unauthorized
        return gson.toJson(new ErrorResponse("Error: Invalid username or password"));
      }
      res.status(500); // Internal server error for other issues
      return gson.toJson(new ErrorResponse("Internal server error"));
    }
  };

  //handle user logout (/session DELETE)
  public Route logout = (Request req, Response res) -> {
    res.type("application/json");

    try {
      //extract the auth token from the headers
      String authToken = req.headers("Authorization");

      //log out the user using the UserService
      userService.logout(authToken);

      //return success response
      res.status(200);
      return gson.toJson(new SuccessResponse(true));
    } catch (DataAccessException e) {
      res.type("application/json");
      res.status(401); // Unauthorized
      return gson.toJson(new ErrorResponse(e.getMessage()));
    }
  };
  //classes for LoginRequest and responses
  private static class LoginRequest {
    String username;
    String password;
  }

  private static class ErrorResponse {
    String message;
    ErrorResponse(String message) {
      this.message = message.startsWith("Error:") ? message : "Error: " + message;
    }
  }

  private static class SuccessResponse {
    boolean success;
    SuccessResponse(boolean success) {
      this.success = success;
    }
  }
}

