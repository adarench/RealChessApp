package handler;

import service.GameService;
import model.GameData;
import dataaccess.DataAccessException;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;
import java.util.Map;
import java.util.HashMap;

public class GameHandler{
  private GameService gameService;
  private final Gson gson = new Gson();

  public GameHandler(GameService gameService) {
    this.gameService = gameService;
  }
  //creating a new game POST

  public Route createGame = (Request req, Response res) -> {
    res.type("application/json");

    try {
      String authToken = req.headers("Authorization");

      // Check if authToken is missing (401 Unauthorized)
      if (authToken == null || authToken.isEmpty()) {
        res.status(401);
        return gson.toJson(new ErrorResponse("Unauthorized: Auth token not provided."));
      }

      CreateGameRequest createGameRequest = gson.fromJson(req.body(), CreateGameRequest.class);
      if (createGameRequest == null || createGameRequest.gameName == null || createGameRequest.gameName.isEmpty()) {
        res.status(400);
        return gson.toJson(new ErrorResponse("Bad Request: Missing game name."));
      }

      GameData gameData = gameService.createGame(authToken, createGameRequest.gameName);
      res.status(200);
      return gson.toJson(gameData);

    } catch (DataAccessException e) {
      if (e.getMessage().contains("Unauthorized")) {
        res.status(401); // Respond with 401 if authentication fails
      } else if (e.getMessage().contains("Forbidden")) {
        res.status(403); // Respond with 403 if access is forbidden
      } else {
        res.status(500); // Internal Server Error for unknown issues
      }
      return gson.toJson(new ErrorResponse(e.getMessage()));
    }
  };


  //list all games GET
  public Route listGames = (Request req, Response res) -> {
    res.type("application/json");

    try {
      //extract the auth token from the headers
      String authToken = req.headers("Authorization");

      // Check if authToken is missing
      if (authToken == null || authToken.isEmpty()) {
        res.status(401); // Unauthorized
        return gson.toJson(new ErrorResponse("Error: Auth token not provided."));
      }
      // List all games
      var games = gameService.listGames(authToken);

      Map<String, Object> response = new HashMap<>();
      response.put("games", games);

      //return success response
      res.status(200);
      return gson.toJson(response);
    } catch (DataAccessException e) {
      res.type("application/json");

      res.status(401);
      return gson.toJson(new ErrorResponse(e.getMessage()));
    }catch (Exception e) {
      res.status(500); // Internal Server Error
      return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
    }
  };

  //join game case PUT
  // In GameHandler.java - Update joinGame to validate the auth token and ensure spot availability
  // In GameHandler.java - Refine joinGame to fit existing structure
  public Route joinGame = (Request req, Response res) -> {
    res.type("application/json");

    try {
      String authToken = req.headers("Authorization");
      if (authToken == null || authToken.isEmpty()) {
        res.status(401);
        return gson.toJson(new ErrorResponse("Unauthorized: Auth token not provided."));
      }

      JoinGameRequest joinGameRequest = gson.fromJson(req.body(), JoinGameRequest.class);
      if (joinGameRequest == null || joinGameRequest.gameID <= 0 || joinGameRequest.playerColor == null || joinGameRequest.playerColor.isEmpty()) {
        res.status(400);
        return gson.toJson(new ErrorResponse("Bad Request: Missing or invalid fields."));
      }

      gameService.joinGame(authToken, joinGameRequest.gameID, joinGameRequest.playerColor);
      res.status(200);
      return gson.toJson(new SuccessResponse(true));

    } catch (DataAccessException e) {
      if (e.getMessage().contains("Unauthorized")) {
        res.status(401);
      } else if (e.getMessage().contains("Forbidden")) {
        res.status(403);
      } else if (e.getMessage().contains("Game not found")) {
        res.status(404);
      } else {
        res.status(500);
      }
      return gson.toJson(new ErrorResponse(e.getMessage()));
    }
  };







  //helper classes
  private static class CreateGameRequest {
    String gameName;
  }

  private static class JoinGameRequest {
    int gameID;
    String playerColor;
  }

  private static class ErrorResponse {
    String message;
    ErrorResponse(String message) {
      if (message.startsWith("Error:")) {
        this.message = message;
      } else {
        this.message = "Error: " + message;
      }
    }
  }

  private static class SuccessResponse {
    boolean success;

    SuccessResponse(boolean success) {
      this.success=success;
    }
  }

}