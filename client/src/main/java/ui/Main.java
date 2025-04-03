package ui;
import java.util.Scanner;
import websocket.WebSocketClient;
import websocket.WebSocketMessageHandler;
import websocket.commands.UserGameCommand;
import com.google.gson.Gson;
import websocket.dto.GameStateDTO;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
  private static ServerFacade serverFacade;
  private static WebSocketClient webSocketClient;
  private static boolean isLoggedIn = false; // Track whether the user is logged in
  private static Scanner scanner = new Scanner(System.in); // Scanner to read user input
  
  public static void main(String[] args) {
    String serverUrl = "http://localhost:8080";
    String webSocketUrl = "ws://localhost:8080/ws";
    if (serverUrl == null) {
      System.err.println("Failed to discover the server. Ensure it is running.");
      return;
    }

    System.out.println("Connected to server at: " + serverUrl);

    // Initialize WebSocketClient
    webSocketClient = WebSocketClient.getInstance();
    try {
      webSocketClient.connect(webSocketUrl);
    } catch (Exception e) {
      System.err.println("Failed to connect to WebSocket server: " + e.getMessage());
      return; // Exit if WebSocket connection fails
    }
    serverFacade = new ServerFacade(serverUrl);

    // Initialize GameplayUI
    GameplayUI.init(serverFacade, webSocketClient);

    Thread messageProcessingThread = new Thread(() -> {
      while (true) {
        try {
          String message = webSocketClient.receiveMessage();
          if (message != null) {
            WebSocketMessageHandler.handleMessage(message);
          }
        } catch (Exception e) {
          System.err.println("Exception in message processing thread: " + e.getMessage());
          e.printStackTrace();
        }
      }
    });
    messageProcessingThread.setDaemon(true);
    messageProcessingThread.start();

    showPreloginMenu();

    while (true) {
      if (GameplayUI.getShouldTransitionToPostLogin().get()) {
        GameplayUI.getShouldTransitionToPostLogin().set(false); // Reset the flag
        GameplayUI.transitionToPostGame();
      }

      // Sleep briefly to reduce CPU usage
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // Handle interruption
      }
    }
  }

  private static void showPreloginMenu() {
    while (true) {
      System.out.println("\n== Chess Client ==");
      System.out.println("Enter a command: help, login, register, quit");
      System.out.print("> ");

      String command = scanner.nextLine().trim().toLowerCase();

      switch (command) {
        case "help":
          showHelp();
          break;
        case "login":
          login();
          break;
        case "register":
          register();
          break;
        case "quit":
          quit();
          return; // Exit the loop and terminate the application
        default:
          System.out.println("Invalid command. Type 'help' for a list of commands.");
      }
    }
  }

  // Display help text
  private static void showHelp() {
    System.out.println("Commands:");
    System.out.println("  help    - Display available commands");
    System.out.println("  login   - Log in to your account");
    System.out.println("  register - Register a new account");
    System.out.println("  quit    - Exit the application");
  }

  /// Implement login functionality
  private static void login() {
    System.out.print("Enter username: ");
    String username = scanner.nextLine().trim();

    System.out.print("Enter password: ");
    String password = scanner.nextLine().trim();

    if (username.isEmpty() || password.isEmpty()) {
      System.out.println("Error: Username and password are required.");
      return;
    }

    // Call ServerFacade to log in
    String response = serverFacade.login(username, password);
    System.out.println(response);

    if (response.contains("Login successful")) {
      isLoggedIn = true; // Update login status
      // Transition to post-login menu
      showPostloginMenu();
    }
  }
  
  private static void logout() {
    System.out.println("Logging out...");
    String response = serverFacade.logout();
    System.out.println(response);

    if (response.contains("Logout successful")) {
      isLoggedIn = false; // Reset login status
      return; // Exit to prelogin menu
    }
  }

  // Implement register functionality
  private static void register() {
    System.out.print("Enter username: ");
    String username = scanner.nextLine().trim();

    System.out.print("Enter password: ");
    String password = scanner.nextLine().trim();

    System.out.print("Enter email: ");
    String email = scanner.nextLine().trim();

    if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
      System.out.println("Error: All fields are required.");
      return;
    }

    // Call ServerFacade to register
    String response = serverFacade.register(username, password, email);
    System.out.println(response);

    if (response.contains("Registration successful")) {
      isLoggedIn = true; // Update login status
      showPostloginMenu();
    }
  }

  // Quit the application
  private static void quit() {
    System.out.println("Goodbye!");
    scanner.close(); // Close the scanner before exiting
    System.exit(0); // Actually exit the application
  }

  private static void listGames() {
    System.out.println("Fetching list of games...");
    String response = serverFacade.listGames();
    if (response.startsWith("Error:")) {
      System.out.println(response); // Display error
      return;
    }

    // Filter out completed games
    String[] games = response.split("\n");
    System.out.println("Available Games:");
    for (String game : games) {
      if (!game.toLowerCase().contains("(finished)")) {
        System.out.println(game);
      }
    }
  }

  private static void createGame() {
    System.out.print("Enter a name for the game: ");
    String gameName = scanner.nextLine().trim();

    if (gameName.isEmpty()) {
      System.out.println("Error: Game name cannot be empty.");
      return;
    }

    String response = serverFacade.createGame(gameName);
    System.out.println(response);
  }
  
  private static void playGame() {
    System.out.print("Enter the game name to join: ");
    String gameName = scanner.nextLine().trim();

    if (gameName.isEmpty()) {
      System.out.println("Error: Game name cannot be empty.");
      return;
    }

    System.out.print("Enter the color you want to play (white/black): ");
    String playerColor = scanner.nextLine().trim().toLowerCase();

    if (!playerColor.equals("white") && !playerColor.equals("black")) {
      System.out.println("Error: Invalid color. Choose 'white' or 'black'.");
      return;
    }

    String response = serverFacade.playGame(gameName, playerColor);
    System.out.println(response);

    if (response.contains("Successfully joined")) {
      GameplayUI.setIsInGame(true);
      GameplayUI.setIsObserver(false);
      GameplayUI.setCurrentGameID(serverFacade.getLastGameID());

      // Send CONNECT command via WebSocket
      sendConnectCommand(serverFacade.getLastGameID());

      // Start gameplay loop
      GameplayUI.gameplayLoop();
    } else if (response.contains("Game is already full")) {
      System.out.println("Error: Unable to join. The game is already full.");
    }
  }
  
  private static void sendConnectCommand(int gameID) {
    // Create a UserGameCommand object for CONNECT
    UserGameCommand connectCommand = new UserGameCommand();
    connectCommand.setCommandType(UserGameCommand.CommandType.CONNECT);
    connectCommand.setAuthToken(serverFacade.getAuthToken());
    connectCommand.setGameID(gameID);

    // Serialize to JSON using Gson
    String connectJson = new Gson().toJson(connectCommand);

    // Send the CONNECT command via WebSocket
    webSocketClient.sendMessage(connectJson);
  }

  private static void observeGame() {
    System.out.print("Enter the game name to observe: ");
    String gameName = scanner.nextLine().trim();

    if (gameName.isEmpty()) {
      System.out.println("Error: Game name cannot be empty.");
      return;
    }

    // Call the observeGame method in ServerFacade
    String response = serverFacade.observeGame(gameName);
    System.out.println(response);
    // Draw the chessboard only if the game was successfully observed
    if (response.startsWith("Observing game:")) {
      try {
        GameplayUI.setIsObserver(true);
        System.out.println("Observer status: " + true);

        GameplayUI.setIsInGame(true);
        // Fetch the game ID by name
        int gameID = serverFacade.getGameIdByName(gameName);
        if (gameID == -1) {
          System.out.println("Error: Unable to find game ID for the specified game name.");
          GameplayUI.setIsObserver(false); // Reset flag on failure
          GameplayUI.setIsInGame(false);
          return;
        }
        String observerAuthToken = serverFacade.getAuthToken();
        if (observerAuthToken == null || observerAuthToken.isEmpty()) {
          System.out.println("Error: Observer is not logged in.");
          GameplayUI.setIsObserver(false); // Reset flag on failure
          GameplayUI.setIsInGame(false);
          return;
        }
        // Send a WebSocket CONNECT command for observing the game
        UserGameCommand connectCommand = new UserGameCommand();
        connectCommand.setCommandType(UserGameCommand.CommandType.CONNECT);
        connectCommand.setAuthToken(observerAuthToken);
        connectCommand.setGameID(gameID);
        String connectJson = new Gson().toJson(connectCommand);
        webSocketClient.sendMessage(connectJson);

        GameplayUI.setCurrentGameID(gameID);

        System.out.println("Waiting for real-time updates...");

        GameplayUI.gameplayLoop();
      } catch (Exception e) {
        System.err.println("Error observing game via WebSocket: " + e.getMessage());
        GameplayUI.setIsObserver(false); // Reset flag on exception
        GameplayUI.setIsInGame(false);
      }
    }
  }

  public static void showPostloginMenu() {
    while (!GameplayUI.getShouldTransitionToPostLogin().get()) {
      System.out.println("\n== Chess Client (Postlogin) ==");
      System.out.println("Enter a command: help, listgames, creategame, playgame, observegame, logout");
      System.out.print("> ");

      String command = scanner.nextLine().trim().toLowerCase();

      switch (command) {
        case "help":
          showPostloginHelp();
          break;
        case "listgames":
          listGames();
          break;
        case "creategame":
          createGame();
          break;
        case "playgame":
          playGame();
          break;
        case "observegame":
          observeGame();
          break;
        case "logout":
          logout();
          return; // Exit to prelogin menu
        default:
          System.out.println("Invalid command. Type 'help' for a list of commands.");
      }
    }
  }

  // Display help text for postlogin commands
  private static void showPostloginHelp() {
    System.out.println("Commands:");
    System.out.println("  help        - Display available commands");
    System.out.println("  listgames   - List all available games");
    System.out.println("  creategame  - Create a new game");
    System.out.println("  playgame    - Join a game to play");
    System.out.println("  observegame - Observe an existing game");
    System.out.println("  logout      - Log out and return to the prelogin menu");
  }
}