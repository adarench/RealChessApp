package ui;
import java.util.Scanner;
import websocket.WebSocketClient;
import chess.ChessMove;
import websocket.GameState;
import chess.ChessPosition;
import chess.ChessPiece;
import chess.ChessBoard;
import websocket.WebSocketMessageHandler;

public class Main {

  private static ServerFacade serverFacade;
  private static WebSocketClient webSocketClient;
  private static GameState currentGameState;
  private static boolean isLoggedIn = false; // Track whether the user is logged in
  private static Scanner scanner = new Scanner(System.in); // Scanner to read user input

  private static int currentGameID = -1; // Track the current game ID




  public static void main(String[] args) {
    String serverUrl = "http://localhost:8080";
    String webSocketUrl = "ws://localhost:8080/ws";
    if (serverUrl == null) {
      System.err.println("Failed to discover the server. Ensure it is running.");
      return;
    }

    System.out.println("Connected to server at: " + serverUrl);

    // Initialize WebSocketClient
    webSocketClient = new WebSocketClient();
    try {
      webSocketClient.connect(webSocketUrl);
    } catch (Exception e) {
      System.err.println("Failed to connect to WebSocket server: " + e.getMessage());
      return; // Exit if WebSocket connection fails
    }
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
      isLoggedIn = true; // Update login status
      currentGameID = serverFacade.getLastGameID();
      // Fetch the game state after joining
      currentGameState = serverFacade.getGameState(currentGameID);
      if (currentGameState != null) {
        drawChessBoard(playerColor.equals("white"), currentGameState);
      } else {
        System.out.println("Error: No game state available to draw the board.");
      }

      // Start gameplay loop
      gameplayLoop();


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
        currentGameState = serverFacade.getGameState(currentGameID);

        if (currentGameState != null) {
          drawChessBoard(true, currentGameState); // Assuming observing is always from the white perspective
        } else {
          System.out.println("Error: No game state available to draw the board.");
        }

        System.out.println("Waiting for real-time updates...");

        // Start listening for real-time updates
        while (true) {
          String serverUpdate = webSocketClient.receiveMessage();
          if (serverUpdate != null) {
            System.out.println("Game update received: " + serverUpdate);
            WebSocketMessageHandler.handleMessage(serverUpdate);
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

  private static void gameplayLoop() {
    while (true) {
      System.out.println("\nEnter a command: makemove, resign, leave, help");
      System.out.print("> ");
      String command = scanner.nextLine().trim().toLowerCase();

      switch (command) {
        case "makemove":
          makeMove();
          break;
        case "resign":
          resign();
          break;
        case "leave":
          leaveGame();
          return; // Exit gameplay loop
        case "help":
          showGameplayHelp();
          break;
        default:
          System.out.println("Invalid command. Type 'help' for a list of commands.");
      }
    }
  }

  private static void leaveGame() {
    if (currentGameID == -1) {
      System.out.println("Error: You are not currently in a game.");
      return;
    }

    // Send a WebSocket LEAVE command
    String leaveCommand = String.format(
            "{\"commandType\": \"LEAVE\", \"authToken\": \"%s\", \"gameID\": %d}",
            serverFacade.getAuthToken(), currentGameID
    );
    webSocketClient.sendMessage(leaveCommand);

    System.out.println("You have left the game.");
    currentGameID = -1; // Reset current game ID
  }
  private static void resign() {
    if (currentGameID == -1) {
      System.out.println("Error: You are not currently in a game.");
      return;
    }

    // Send a WebSocket RESIGN command
    String resignCommand = String.format(
            "{\"commandType\": \"RESIGN\", \"authToken\": \"%s\", \"gameID\": %d}",
            serverFacade.getAuthToken(), currentGameID
    );
    webSocketClient.sendMessage(resignCommand);

    System.out.println("You have resigned from the game.");
    currentGameID = -1; // Reset current game ID
  }


  public void updateGameState(GameState updatedState) {
    this.currentGameState = updatedState;
  }
  public boolean isWhitePlayer() {
    // Determine based on player color
    // This method needs to be implemented to reflect the player's assigned color
    return true; // Placeholder
  }
  private static void showGameplayHelp() {
    System.out.println("In-Game Commands:");
    System.out.println("  makemove - Make a chess move (e.g., e2e4)");
    System.out.println("  resign   - Resign from the game");
    System.out.println("  leave    - Leave the game without resigning");
    System.out.println("  help     - Display available in-game commands");
  }

  private static void makeMove() {
    System.out.print("Enter your move (e.g., e2e4): ");
    String moveInput = scanner.nextLine().trim();

    if (!isValidMoveFormat(moveInput)) {
      System.out.println("Error: Invalid move format. Please use standard chess notation (e.g., e2e4).");
      return;
    }

    // Parse move input
    ChessMove move = parseMove(moveInput);
    if (move == null) {
      System.out.println("Error: Failed to parse move. Please try again.");
      return;
    }

    // Send move via WebSocketClient
    try {
      webSocketClient.sendMakeMoveCommand(
              serverFacade.getAuthToken(), // Auth token remains managed by ServerFacade
              serverFacade.getLastGameID(),
              move
      );
      // Fetch the updated game state and redraw the board
              currentGameState = serverFacade.getGameState(currentGameID);
      if (currentGameState != null) {
        drawChessBoard(true, currentGameState);
      } else {
        System.out.println("Error: Failed to update game state after move.");
      }
    } catch (Exception e) {
      System.err.println("Error: Failed to send move command. " + e.getMessage());
    }
  }
  private static boolean isValidMoveFormat(String move) {
    return move.matches("^[a-h][1-8][a-h][1-8][QRBN]?$");
  }
  private static GameState fetchGameStateFromServer(int gameID) {
    // Logic to retrieve the game state (via WebSocket or HTTP)
    return null; // Replace with actual implementation
  }

  private static ChessMove parseMove(String moveInput) {
    try {
      // Extract start and end positions from input
      String start = moveInput.substring(0, 2); // e.g., "e2"
      String end = moveInput.substring(2, 4);   // e.g., "e4"

      // Convert start and end strings into ChessPosition objects
      ChessPosition startPosition = parseChessPosition(start);
      ChessPosition endPosition = parseChessPosition(end);

      // Parse promotion piece if provided (optional)
      ChessPiece.PieceType promotionPiece = null;
      if (moveInput.length() == 5) {
        char promotionChar = moveInput.charAt(4); // e.g., 'q' for queen
        promotionPiece = mapPromotionPiece(promotionChar);
      }

      // Return the ChessMove object
      return new ChessMove(startPosition, endPosition, promotionPiece);
    } catch (Exception e) {
      // Return null for invalid input
      return null;
    }
  }
  private static ChessPosition parseChessPosition(String position) {
    char column = position.charAt(0); // e.g., 'e'
    int row = Character.getNumericValue(position.charAt(1)); // e.g., 2

    int colIndex = column - 'a' + 1; // Convert 'a' to 1, 'b' to 2, etc.
    return new ChessPosition(row, colIndex);
  }

  private static ChessPiece.PieceType mapPromotionPiece(char promotionChar) {
    switch (Character.toLowerCase(promotionChar)) {
      case 'q': return ChessPiece.PieceType.QUEEN;
      case 'r': return ChessPiece.PieceType.ROOK;
      case 'b': return ChessPiece.PieceType.BISHOP;
      case 'n': return ChessPiece.PieceType.KNIGHT;
      default: return null; // Invalid promotion piece
    }
  }


  public static void drawChessBoard(boolean whiteAtBottom, GameState gameState) {
    if (currentGameState == null || currentGameState.getChessGame() == null) {
      System.out.println("Error: No game state available to draw the board.");
      return;
    }

    // Get the current board state
    ChessBoard board = currentGameState.getChessGame().getBoard();

    // Print the board row by row
    for (int row = 8; row >= 1; row--) { // Rows in chess are from 8 (top) to 1 (bottom)
      for (int col = 1; col <= 8; col++) { // Columns are from a (1) to h (8)
        ChessPosition position = new ChessPosition(row, col);
        ChessPiece piece = board.getPiece(position);

        // Display the piece if one exists, or a blank space otherwise
        String squareContent = (piece != null) ? piece.toString() : " ";
        boolean isLightSquare = (row + col) % 2 == 0;

        // Color the squares for display
        String squareColor = isLightSquare ? "\u001B[47m" : "\u001B[40m"; // White or black background
        String reset = "\u001B[0m"; // Reset terminal color
        System.out.print(squareColor + squareContent + " " + reset);
      }
      System.out.println(" " + row); // Add the row number to the right
    }

    // Print column labels at the bottom
    char[] columns = whiteAtBottom
            ? new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'}
            : new char[]{'h', 'g', 'f', 'e', 'd', 'c', 'b', 'a'};
    for (char col : columns) {
      System.out.print(col + " ");
    }
    System.out.println();
  }







}