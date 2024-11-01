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
import spark.Spark;
import dataaccess.DatabaseManager;
import dataaccess.DataAccessException;

import static spark.Spark.*;



public class Server {

  public static void main(String[] args) {
    int port = 8080; // Default port

    // Use the command-line argument for the port, if provided
    if (args.length > 0) {
      port = Integer.parseInt(args[0]);
    }

    // Start the server on the specified or default port
    run(port);
  }

  public static int run(int port) {

    try {
      // Initialize the database and tables
      DatabaseManager.createDatabase();
      DatabaseManager.createTables();
    } catch (DataAccessException e) {
      System.err.println("Failed to initialize database: " + e.getMessage());
      e.printStackTrace();
      return -1; // return an error code if database initialization fails
    }
    // Set the port for the Spark server
    port(port); // You can change this port as needed
    staticFiles.location("/web");

    // initialize DAOs (data access objects)
    UserDAO userDAO=new UserDAO();
    GameDAO gameDAO=new GameDAO();
    AuthDAO authDAO=new AuthDAO();

    // initialize services
    UserService userService=new UserService(userDAO, authDAO);
    GameService gameService=new GameService(gameDAO, authDAO);
    DatabaseService databaseService=new DatabaseService(userDAO, gameDAO, authDAO);

    // initialize handlers and pass the services to them
    UserHandler userHandler=new UserHandler(userService);
    GameHandler gameHandler=new GameHandler(gameService);
    DatabaseHandler databaseHandler=new DatabaseHandler(databaseService);

    // routes
    post("/user", userHandler.register);        // register
    post("/session", userHandler.login);        // login
    delete("/session", userHandler.logout);     // logout

    // Routes for game operations
    post("/game", gameHandler.createGame);      // new game
    get("/game", gameHandler.listGames);        // list games
    put("/game", gameHandler.joinGame);         // join game

    // Route for clearing the database
    delete("/db", databaseHandler.clearDatabase); // clear db

    // spark will listen on port
    awaitInitialization();
    return port();
  }



  public void stop() {
    Spark.stop();
    awaitStop();
  }
}
