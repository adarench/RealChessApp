package server;

import handler.UserHandler;
import handler.GameHandler;
import handler.DatabaseHandler;
import service.UserService;
import service.GameService;
import service.DatabaseService;
import dataaccess.UserDAO;
import dataaccess.GameDAO;
import dataaccess.AuthDAO;
import dataaccess.DatabaseManager;
import spark.Spark;

import static spark.Spark.*;

public class Server {
  public static int run(int port) {
    // Initialize the database and tables
    try {
      DatabaseManager.initializeDatabase();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    port(port);
    staticFiles.location("/web");

    // Initialize DAOs (data access objects)
    UserDAO userDAO = new UserDAO();
    GameDAO gameDAO = new GameDAO();
    AuthDAO authDAO = new AuthDAO();

    // Initialize services
    UserService userService = new UserService(userDAO, authDAO);
    GameService gameService = new GameService(gameDAO, authDAO);
    DatabaseService databaseService = new DatabaseService(userDAO, gameDAO, authDAO);

    // Initialize handlers and pass the services to them
    UserHandler userHandler = new UserHandler(userService);
    GameHandler gameHandler = new GameHandler(gameService);
    DatabaseHandler databaseHandler = new DatabaseHandler(databaseService);

    // Routes
    post("/user", userHandler.register);
    post("/session", userHandler.login);
    delete("/session", userHandler.logout);

    // Routes for game operations
    post("/game", gameHandler.createGame);
    get("/game", gameHandler.listGames);
    put("/game", gameHandler.joinGame);

    delete("/db", databaseHandler.clearDatabase);

    awaitInitialization();
    return port();
  }

  public void stop() {
    Spark.stop();
    awaitStop();
  }
}
