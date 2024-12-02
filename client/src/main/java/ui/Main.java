package ui;
import java.util.Scanner;
import websocket.WebSocketClient;
import chess.ChessMove;
import websocket.GameState;
import chess.ChessPosition;
import chess.ChessPiece;
import websocket.WebSocketMessageHandler;
import websocket.commands.UserGameCommand;
import com.google.gson.Gson;
import chess.ChessGame;
import websocket.dto.GameStateDTO;
import java.util.Arrays;

import java.util.Map;
public class Main {

  private static ServerFacade serverFacade;
  private static WebSocketClient webSocketClient;

  private static GameStateDTO currentGameStateDTO;

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

    // Initialize WebSocketClient using Singleton
    webSocketClient = WebSocketClient.getInstance();
    try {
      webSocketClient.connect(webSocketUrl);
    } catch (Exception e) {
      System.err.println("Failed to connect to WebSocket server: " + e.getMessage());
      return; // Exit if WebSocket connection fails
    }
    serverFacade = new ServerFacade(serverUrl);

    // Removed Duplicate Initialization

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

      // Send CONNECT command via WebSocket
      sendConnectCommand(currentGameID);

      // Start gameplay loop
      gameplayLoop();

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
        // Fetch the game ID by name
        int gameID = serverFacade.getGameIdByName(gameName);
        if (gameID == -1) {
          System.out.println("Error: Unable to find game ID for the specified game name.");
          return;
        }

        // Send a WebSocket CONNECT command for observing the game
        UserGameCommand connectCommand = new UserGameCommand();
        connectCommand.setCommandType(UserGameCommand.CommandType.CONNECT);
        connectCommand.setAuthToken(serverFacade.getAuthToken());
        connectCommand.setGameID(gameID);
        String connectJson = new Gson().toJson(connectCommand);
        webSocketClient.sendMessage(connectJson);

        currentGameID = gameID; // Update the currentGameID

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


  public static void updateGameState(GameStateDTO updatedState) {
    currentGameStateDTO = updatedState;
  }
  public static boolean isWhitePlayer() {
    if (currentGameStateDTO == null) {
      System.err.println("GameStateDTO is null. Cannot determine player color.");
      return true; // Default orientation
    }
    // Assuming you have a method to get the player's color
    String playerColorStr = currentGameStateDTO.getPlayerColors().get(serverFacade.getAuthToken());
    ChessGame.TeamColor playerColor = ChessGame.TeamColor.valueOf(playerColorStr);
    return playerColor == ChessGame.TeamColor.WHITE;
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

    System.out.println("Parsed Move: " + move.toString());

    // Send move via WebSocketClient
    try {
      webSocketClient.sendMakeMoveCommand(
              serverFacade.getAuthToken(), // Auth token remains managed by ServerFacade
              serverFacade.getLastGameID(),
              move
      );

      System.out.println("Waiting for server response...");
      String serverUpdate = webSocketClient.receiveMessage();
      if (serverUpdate != null) {
        WebSocketMessageHandler.handleMessage(serverUpdate);
      }

    } catch (Exception e) {
      System.err.println("Error: Failed to send move command. " + e.getMessage());
    }
  }
  private static boolean isValidMoveFormat(String move) {return move.matches("^[a-h][1-8][a-h][1-8][QRBN]?$");}
  private static GameState fetchGameStateFromServer(int gameID) {
    // Logic to retrieve the game state (via WebSocket or HTTP)
    return null; // Replace with actual implementation
  }
  private static ChessPiece.PieceType mapPromotionPiece(char promotionChar) {
    switch (Character.toLowerCase(promotionChar)) {
      case 'q': return ChessPiece.PieceType.QUEEN;
      case 'r': return ChessPiece.PieceType.ROOK;
      case 'b': return ChessPiece.PieceType.BISHOP;
      case 'n': return ChessPiece.PieceType.KNIGHT;
      default: return null; // Invalid promotion piece
    }}
  private static ChessMove parseMove(String moveInput) {
    try {
      // Extract start and end positions from input
      String start = moveInput.substring(0, 2); // e.g., "e2"
      String end = moveInput.substring(2, 4);   // e.g., "e4"

      // Convert start and end strings into ChessPosition objects
      ChessPosition startPosition = parsePosition(start);
      ChessPosition endPosition = parsePosition(end);

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



  private static ChessPosition parsePosition(String pos) {
    char column = pos.charAt(0); // e.g., 'e'
    int row = Character.getNumericValue(pos.charAt(1)); // e.g., 2

    int colIndex = column - 'a' + 1; // Convert 'a' to 1, 'b' to 2, etc.
    return new ChessPosition(row, colIndex);
  }


  public static void drawChessBoard(boolean isWhitePlayer, GameStateDTO gameStateDTO) {
    // Initialize an 8x8 array to represent the board
    String[][] boardArray = new String[8][8];

    // Fill the boardArray with empty spaces
    for (int i = 0; i < 8; i++) {
      Arrays.fill(boardArray[i], " ");
    }

    // Populate the boardArray with pieces from the gameStateDTO's board map
    Map<String, String> boardMap = gameStateDTO.getBoard();

    for (Map.Entry<String, String> entry : boardMap.entrySet()) {
      String position = entry.getKey(); // e.g., "e3"
      String piece = entry.getValue();  // e.g., "♙"

      // Convert position (e.g., "e3") to array indices
      int rank = position.charAt(1) - '0'; // '1' to '8'
      int file = position.charAt(0) - 'a'; // 'a' to 'h' mapped to 0 to 7

      int arrayRow = 8 - rank; // Convert rank to array index (0 to 7)
      int arrayCol = file;     // File is already 0 to 7

      // Place the piece in the boardArray
      boardArray[arrayRow][arrayCol] = piece;
    }

    // Print the boardArray
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        System.out.print(boardArray[i][j] + " ");
      }
      System.out.println(" " + (8 - i)); // Print the rank number
    }
    System.out.println("a b c d e f g h");
  }







}