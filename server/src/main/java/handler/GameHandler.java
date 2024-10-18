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

}