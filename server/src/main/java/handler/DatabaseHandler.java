package handler;

import service.DatabaseService;
import dataAccess.DataAccessException;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;

public class DatabaseHandler{
  private DatabaseService databaseService;
  private Gson gson = new Gson();

  public DatabaseHandler(DatabaseService databaseService){
    this.databaseService = databaseService;
  }

  //clearing database DELETE
  public Route clearDatabase = (Request req, Response res) -> {
    res.type("application/json");

    try {
      databaseService.clearDatabase();

      res.status(200);
      return gson.toJson(new SuccessResponse(true));
    } catch (DataAccessException e) {
      res.type("application/json");

      res.status(500);
      return gson.toJson(new ErrorResponse(e.getMessage()));
    }
  };

private static class ErrorResponse {
  String message;
  ErrorResponse(String message){
    this.message = "Error: " + message;
  }
}

private static class SuccessResponse{
  boolean success;
  SuccessResponse(boolean success){
    this.success = success;
  }
}

}