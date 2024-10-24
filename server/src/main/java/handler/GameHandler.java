package handler;

import service.GameService;
import model.GameData;
import dataAccess.DataAccessException;
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
      // Get the auth token from the headers
      String authToken = req.headers("Authorization");

      // Check if authToken is missing
      if (authToken == null || authToken.isEmpty()) {
        res.status(401); // Unauthorized
        return gson.toJson(new ErrorResponse("Error: auth token not found"));
      }

      // Parse request body
      CreateGameRequest createGameRequest = gson.fromJson(req.body(), CreateGameRequest.class);

      // Input validation
      if (createGameRequest == null ||
              createGameRequest.gameName == null || createGameRequest.gameName.isEmpty()) {
        res.status(400); // Bad Request
        return gson.toJson(new ErrorResponse("Error: bad request"));
      }

      // Create new game
      GameData gameData = gameService.createGame(authToken, createGameRequest.gameName);

      // Return success
      res.status(200);
      return gson.toJson(gameData);

    } catch (DataAccessException e) {
      res.status(401); // Unauthorized
      return gson.toJson(new ErrorResponse("Error: invalid auth token"));
    } catch (Exception e) {
      res.status(500); // Internal Server Error
      return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
    }
  };

  //list all games GET
  public Route listGames = (Request req, Response res) -> {
    res.type("application/json");

    try {
      //extract the auth token from the headers
      String authToken = req.headers("Authorization");

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
    }
  };

  //join game case PUT
  public Route joinGame = (Request req, Response res) -> {
    res.type("application/json");

    try {
      // Extract the auth token from the headers
      String authToken = req.headers("Authorization");

      // Check if authToken is missing
      if (authToken == null || authToken.isEmpty()) {
        res.status(401); // Unauthorized
        return gson.toJson(new ErrorResponse("Error: Auth token not found."));
      }

      // Parse request body to get gameID and playerColor
      JoinGameRequest joinGameRequest = gson.fromJson(req.body(), JoinGameRequest.class);

      if (joinGameRequest == null ||
              joinGameRequest.gameID <= 0 ||
              joinGameRequest.playerColor == null || joinGameRequest.playerColor.isEmpty()) {
        res.status(400); // Bad Request
        return gson.toJson(new ErrorResponse("Error: bad request"));
      }

      // Join the game
      gameService.joinGame(authToken, joinGameRequest.gameID, joinGameRequest.playerColor);

      // Return success response
      res.status(200);
      return gson.toJson(new SuccessResponse(true));
    } catch (DataAccessException e) {
      res.type("application/json");

      String errorMessage = e.getMessage();

      // **Set the status code based on the error message**
      if (errorMessage.contains("Auth token not found") || errorMessage.contains("Invalid auth token")) {
        res.status(401); // Unauthorized
      } else if (errorMessage.contains("spot already taken") || errorMessage.contains("Spot already taken")) {
        res.status(403); // Forbidden
      } else {
        res.status(400); // Bad Request
      }
      return gson.toJson(new ErrorResponse("Error: " + errorMessage));
    } catch (Exception e) {
      res.status(500); // Internal Server Error
      return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
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
      this.message ="Error: " + message;
    }
  }

  private static class SuccessResponse {
    boolean success;

    SuccessResponse(boolean success) {
      this.success=success;
    }
  }

}