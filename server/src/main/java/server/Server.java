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
import dataaccess.DatabaseManager; // Import DatabaseManager for initialization
import spark.Spark;

import static spark.Spark.*;

public class Server {
  public static int run(int port) {
    // Initialize the database and tables
    try {
      DatabaseManager.initializeDatabase();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1); // Exit if database initialization fails
    }

    // Set the port for the Spark server
    port(port); // You can change this port as needed
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
    post("/user", userHandler.register);        // register
    post("/session", userHandler.login);        // login
    delete("/session", userHandler.logout);     // logout

    // Routes for game operations
    post("/game", gameHandler.createGame);      // new game
    get("/game", gameHandler.listGames);        // list games
    put("/game", gameHandler.joinGame);         // join game

    // Route for clearing the database
    delete("/db", databaseHandler.clearDatabase); // clear db

    // Add a shutdown hook to close the connection pool on server shutdown
    Runtime.getRuntime().addShutdownHook(new Thread(DatabaseManager::close));

    // Spark will listen on port
    awaitInitialization();
    return port();
  }

  public void stop() {
    Spark.stop();
    awaitStop();
  }
}
