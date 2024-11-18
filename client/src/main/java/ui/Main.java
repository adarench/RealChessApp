package ui;

import java.util.Scanner;
public class Main {

  private static final ServerFacade SERVER_FACADE= new ServerFacade();

  private static boolean isLoggedIn = false; // Track whether the user is logged in
  private static Scanner scanner = new Scanner(System.in); // Scanner to read user input

  public static void main(String[] args) {


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
    String response = SERVER_FACADE.login(username, password);
    System.out.println(response);

    if (response.contains("Login successful")) {
      isLoggedIn = true; // Update login status
      // Transition to post-login menu (to be implemented later)
      showPostloginMenu();
    }
  }
  private static void logout() {
    System.out.println("Logging out...");
    String response = SERVER_FACADE.logout();
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
    String response = SERVER_FACADE.register(username, password, email);
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
    String response = SERVER_FACADE.listGames();
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

    String response = SERVER_FACADE.createGame(gameName);
    System.out.println(response);
  }
  private static void playGame() {
    System.out.print("Enter the game ID to join: ");
    int gameID;

    try {
      gameID = Integer.parseInt(scanner.nextLine().trim());
    } catch (NumberFormatException e) {
      System.out.println("Error: Invalid game ID.");
      return;
    }

    System.out.print("Enter the color you want to play (white/black): ");
    String playerColor = scanner.nextLine().trim().toLowerCase();

    if (!playerColor.equals("white") && !playerColor.equals("black")) {
      System.out.println("Error: Invalid color. Choose 'white' or 'black'.");
      return;
    }

    String response = SERVER_FACADE.playGame(gameID,playerColor);
    System.out.println(response);

    if (response.contains("Successfully joined")) {
      drawChessBoard(playerColor.equals("white"));
    }else if (response.contains("Game is already full")) {
      System.out.println("Error: Unable to join. The game is already full.");
    }
  }
  private static void observeGame() {
    System.out.print("Enter the game ID to observe: ");
    int gameID;

    try {
      gameID = Integer.parseInt(scanner.nextLine().trim());
    } catch (NumberFormatException e) {
      System.out.println("Error: Invalid game ID.");
      return;
    }

    String response = SERVER_FACADE.observeGame(gameID);
    System.out.println(response);

    drawChessBoard(true);
    drawChessBoard(false);
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
          System.out.println("PlayGame functionality not implemented yet.");
          break;
        case "observegame":
          observeGame();
          System.out.println("ObserveGame functionality not implemented yet.");
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
      for (int i = 0; i < board.length / 2; i++) {
        String[] temp = board[i];
        board[i] = board[board.length - 1 - i];
        board[board.length - 1 - i] = temp;
      }
    }

    // Draw the board with alternating colors
    System.out.println("  a b c d e f g h");
    for (int row = 0; row < 8; row++) {
      System.out.print((whiteAtBottom ? 8 - row : row + 1) + " ");
      for (int col = 0; col < 8; col++) {
        boolean isLightSquare = (row + col) % 2 == 0;
        String squareColor = isLightSquare ? "\u001B[47m" : "\u001B[40m"; // White or Black background
        String reset = "\u001B[0m"; // Reset colors
        System.out.print(squareColor + board[row][col] + " " + reset);
      }
      System.out.println();
    }
  }


}
