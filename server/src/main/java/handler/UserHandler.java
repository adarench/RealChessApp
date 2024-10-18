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
}

