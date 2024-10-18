package handler;

import service.GameService;
import model.GameData;
import dataAccess.DataAccessException;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;

public class GameHandler{
  private final GameService gameService;
  private final Gson gson = new Gson();

  public GameHandler(GameService gameService) {
    this.gameService = gameService;
  }
  //creating a new game POST
  public Route createGame = (Request req, Response res) -> {
    try {
      //get auth token
      String authToken = req.headers("Authorization");

      //parse request body
      CreateGameRequest createGameRequest = gson.fromJson(req.body(), CreateGameRequest.class);

      //create new game
      GameData gameData = gameService.createGame(authToken, createGameRequest.gameName);

      //return success
      res.status(200);
      return gson.toJson(gameData);
    } catch (DataAccessException e) {
      res.status(400); // Bad request
      return gson.toJson(new ErrorResponse(e.getMessage()));
    }
  };

  //list all games GET
  public Route listGames = (Request req, Response res) -> {
    try {
      //extract the auth token from the headers
      String authToken = req.headers("Authorization");

      // List all games
      var games = gameService.listGames(authToken);

      //return success response
      res.status(200);
      return gson.toJson(games);
    } catch (DataAccessException e) {
      res.status(401);
      return gson.toJson(new ErrorResponse(e.getMessage()));
    }
  };

  //join game case PUT
  public Route joinGame = (Request req, Response res) -> {
    try {
      // Extract the auth token from the headers
      String authToken = req.headers("Authorization");

      // Parse request body to get gameID and playerColor
      JoinGameRequest joinGameRequest = gson.fromJson(req.body(), JoinGameRequest.class);

      // Join the game
      gameService.joinGame(authToken, joinGameRequest.gameID, joinGameRequest.playerColor);

      // Return success response
      res.status(200);
      return gson.toJson(new SuccessResponse(true));
    } catch (DataAccessException e) {
      res.status(400); // Bad request
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
      this.message = message;
    }
  }

  private static class SuccessResponse {
    boolean success;
    SuccessResponse(boolean success) {
      this.success = success;
    }

}