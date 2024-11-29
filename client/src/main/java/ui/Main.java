package ui;
import java.util.Scanner;
import websocket.WebSocketClient;
import java.net.HttpURLConnection;
import java.net.URL;

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
    serverFacade = new ServerFacade(serverUrl);

    // Initialize WebSocketClient
    webSocketClient = new WebSocketClient();
    try {
      webSocketClient.connect(webSocketUrl);
    } catch (Exception e) {
      System.err.println("Failed to connect to WebSocket server: " + e.getMessage());
    }

    showPreloginMenu();
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
      // Transition to post-login menu (to be implemented later)
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
  }

  private static void listGames() {
    System.out.println("Fetching list of games...");
    String response = serverFacade.listGames();
    if (response.startsWith("Error:")) {
      System.out.println(response); // Display error
    } else {
      System.out.println("Available Games:");
      System.out.println(response); // Display the list of games
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

    String response = serverFacade.playGame(gameName,playerColor);
    System.out.println(response);

    if (response.contains("Successfully joined")) {
      drawChessBoard(playerColor.equals("white"));
    }else if (response.contains("Game is already full")) {
      System.out.println("Error: Unable to join. The game is already full.");
    }
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
        // Send a WebSocket CONNECT command for observing the game
        String observeCommand = String.format(
                "{\"commandType\": \"CONNECT\", \"authToken\": \"validToken\", \"gameID\": \"%s\"}", gameName
        );
        webSocketClient.sendMessage(observeCommand);

        // Draw the chessboard for both perspectives
        drawChessBoard(true);
        drawChessBoard(false);

        System.out.println("Waiting for real-time updates...");

        // Start listening for real-time updates
        while (true) {
          String serverUpdate = webSocketClient.receiveMessage();
          if (serverUpdate != null && serverUpdate.contains("update")) {
            System.out.println("Game update received: " + serverUpdate);
            drawChessBoard(true);
            drawChessBoard(false);
          }
        }
      } catch (Exception e) {
        System.err.println("Error observing game via WebSocket: " + e.getMessage());
      }
    }
  }



  private static void showPostloginMenu() {
    while (true) {
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
  private static void drawChessBoard(boolean whiteAtBottom) {
    // Pieces in starting positions
    String[][] board = {
            {"♜", "♞", "♝", "♛", "♚", "♝", "♞", "♜"},
            {"♟", "♟", "♟", "♟", "♟", "♟", "♟", "♟"},
            {" ", " ", " ", " ", " ", " ", " ", " "},
            {" ", " ", " ", " ", " ", " ", " ", " "},
            {" ", " ", " ", " ", " ", " ", " ", " "},
            {" ", " ", " ", " ", " ", " ", " ", " "},
            {"♙", "♙", "♙", "♙", "♙", "♙", "♙", "♙"},
            {"♖", "♘", "♗", "♕", "♔", "♗", "♘", "♖"}
    };

    // Flip board for black perspective
    if (!whiteAtBottom) {
      for (int row = 0; row < board.length; row++) {
        // Reverse each row to flip horizontally
        for (int col = 0; col < board[row].length / 2; col++) {
          String temp = board[row][col];
          board[row][col] = board[row][board[row].length - 1 - col];
          board[row][board[row].length - 1 - col] = temp;
        }
      }
    }

    // Draw the board
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col++) {
        boolean isLightSquare = (row + col) % 2 == 0;
        String squareColor = isLightSquare ? "\u001B[47m" : "\u001B[40m"; // White or Black background
        String reset = "\u001B[0m"; // Reset colors
        System.out.print(squareColor + board[row][col] + " " + reset);
      }
      // Print row numbers on the right
      System.out.println(" " + (whiteAtBottom ? 8 - row : row + 1));
    }

    // Print column labels
    System.out.print("");
    char[] columns = whiteAtBottom ? new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'}
            : new char[]{'h', 'g', 'f', 'e', 'd', 'c', 'b', 'a'};
    for (char col : columns) {
      System.out.print(col + " ");
    }
    System.out.println();
  }






}
