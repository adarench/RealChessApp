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

      if (userData == null ||
              userData.username() == null || userData.username().isEmpty() ||
              userData.password() == null || userData.password().isEmpty() ||
              userData.email() == null || userData.email().isEmpty()) {
        res.status(400); // Bad Request
        return gson.toJson(new ErrorResponse("Error: bad request"));
      }

      // Register the user using the UserService
      AuthData authData = userService.register(userData);

      // Return success response with the auth token
      res.status(200);
      return gson.toJson(authData);

    } catch (DataAccessException e) {
      // Username already taken
      res.status(403); // Forbidden
      return gson.toJson(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      // Other server errors
      res.status(500); // Internal Server Error
      return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
    }
  };






  // handle user login (/session POST)
  public Route login = (Request req, Response res) -> {
    res.type("application/json");

    try {
      //parse request body for login credentials
      LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);

      //log in the user using the UserService
      AuthData authData = userService.login(loginRequest.username, loginRequest.password);

      //return success response with the auth token
      res.status(200);

      return gson.toJson(authData);
    } catch (DataAccessException e) {
      res.type("application/json");
      res.status(401); // Unauthorized
      return gson.toJson(new ErrorResponse(e.getMessage()));
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
      this.message = "Error: " + message;
    }
  }

  private static class SuccessResponse {
    boolean success;
    SuccessResponse(boolean success) {
      this.success = success;
    }
  }
}

