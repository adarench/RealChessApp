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
import java.util.Collections;

import java.util.Map;
public class Main {

  //colors

  public static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_LIGHT_SQUARE = "\u001B[47m"; // Light gray background
  public static final String ANSI_DARK_SQUARE = "\u001B[40m";  // Dark black background
  public static final String ANSI_WHITE_PIECE = "\u001B[37m";  // White pieces
  public static final String ANSI_BLACK_PIECE = "\u001B[30m";  // Black pieces


  public static boolean isWhitePlayer = true; // Set this based on the player's role
  private static volatile GameStateDTO gameStateDTO;





  private static ServerFacade serverFacade;
  private static WebSocketClient webSocketClient;



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
    webSocketClient = WebSocketClient.getInstance();
    try {
      webSocketClient.connect(webSocketUrl);
    } catch (Exception e) {
      System.err.println("Failed to connect to WebSocket server: " + e.getMessage());
      return; // Exit if WebSocket connection fails
    }
    serverFacade = new ServerFacade(serverUrl);
    //gameStateDTO = getInitialGameState();



    Thread messageProcessingThread = new Thread(() -> {
      while (true) {
        try {
          String message = webSocketClient.receiveMessage();
          if (message != null) {
            System.out.println("Processing message: " + message);
            WebSocketMessageHandler.handleMessage(message);
          } else {
            System.out.println("Received null message from receiveMessage()");
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
  }

  private static GameStateDTO getInitialGameState() {
    // Initialize a new game state
    GameStateDTO initialState = new GameStateDTO();

    // Example: Setting up pieces in their starting positions
    Map<String, String> board = initialState.getBoard();

    // Initialize White pieces
    board.put("a1", "♖"); // Rook
    board.put("b1", "♘"); // Knight
    board.put("c1", "♗"); // Bishop
    board.put("d1", "♕"); // Queen
    board.put("e1", "♔"); // King
    board.put("f1", "♗"); // Bishop
    board.put("g1", "♘"); // Knight
    board.put("h1", "♖"); // Rook
    for (char file = 'a'; file <= 'h'; file++) {
      String position = "" + file + "2";
      board.put(position, "♙"); // Pawns
    }

    // Initialize Black pieces
    board.put("a8", "♜"); // Rook
    board.put("b8", "♞"); // Knight
    board.put("c8", "♝"); // Bishop
    board.put("d8", "♛"); // Queen
    board.put("e8", "♚"); // King
    board.put("f8", "♝"); // Bishop
    board.put("g8", "♞"); // Knight
    board.put("h8", "♜"); // Rook
    for (char file = 'a'; file <= 'h'; file++) {
      String position = "" + file + "7";
      board.put(position, "♟"); // Pawns
    }

    // Set player colors (replace with actual auth tokens)
    Map<String, String> playerColors = initialState.getPlayerColors();
    playerColors.put("your-auth-token", "WHITE");
    playerColors.put("opponent-auth-token", "BLACK");

    // Initialize other game state parameters as needed
    initialState.setGameOver(false);

    return initialState;
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
        String observerAuthToken = serverFacade.getAuthToken();
        if (observerAuthToken == null || observerAuthToken.isEmpty()) {
          System.out.println("Error: Observer is not logged in.");
          return;
        }
        // Send a WebSocket CONNECT command for observing the game
        UserGameCommand connectCommand = new UserGameCommand();
        connectCommand.setCommandType(UserGameCommand.CommandType.CONNECT);
        connectCommand.setAuthToken(observerAuthToken);
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
      System.out.println("\nEnter a command: makemove, resign, leave, redraw, help");
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
        case "redraw":
          if (gameStateDTO != null) {
            drawChessBoard(isWhitePlayer, gameStateDTO);
            System.out.println("Board has been redrawn.");
          } else {
            System.out.println("Game state is not available. Please wait for the game to start.");
          }
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
    System.out.println("Updating gameStateDTO with new state.");
    gameStateDTO = updatedState;

    // Update player color based on the updated game state
    String playerColorStr = gameStateDTO.getPlayerColors().get(serverFacade.getAuthToken());
    if (playerColorStr != null) {
      isWhitePlayer = "WHITE".equalsIgnoreCase(playerColorStr);
      System.out.println("Player color set to: " + playerColorStr);
    } else {
      System.err.println("Player color not found for authToken: " + serverFacade.getAuthToken());
      isWhitePlayer = true; // Default to white if color not found
    }

    System.out.println("gameStateDTO updated: " + new Gson().toJson(gameStateDTO));
  }


  public static boolean isWhitePlayer() {
    if (gameStateDTO == null) {
      System.err.println("GameStateDTO is null. Cannot determine player color.");
      return true; // Default orientation
    }
    try {
      String playerColorStr = gameStateDTO.getPlayerColors().get(serverFacade.getAuthToken());
      if (playerColorStr == null) {
        System.err.println("Player color not found for authToken: " + serverFacade.getAuthToken());
        return true; // Default orientation
      }
      ChessGame.TeamColor playerColor = ChessGame.TeamColor.valueOf(playerColorStr);
      return playerColor == ChessGame.TeamColor.WHITE;
    } catch (Exception e) {
      System.err.println("Exception in isWhitePlayer: " + e.getMessage());
      e.printStackTrace();
      return true; // Default orientation
    }
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
  public static ChessMove parseMove(String input) {
    // Parse start and end positions
    int startCol = input.charAt(0) - 'a' + 1;
    int startRow = Character.getNumericValue(input.charAt(1));
    int endCol = input.charAt(2) - 'a' + 1;
    int endRow = Character.getNumericValue(input.charAt(3));

    // Do not adjust rows

    ChessPosition startPos = new ChessPosition(startRow, startCol);
    ChessPosition endPos = new ChessPosition(endRow, endCol);

    // Since this is not a promotion move, we pass null for promotionPiece
    return new ChessMove(startPos, endPos, null);
  }






  private static ChessPosition parsePosition(String pos) {
    char column = pos.charAt(0); // e.g., 'e'
    int row = Character.getNumericValue(pos.charAt(1)); // e.g., 2

    int colIndex = column - 'a' + 1; // Convert 'a' to 1, 'b' to 2, etc.
    return new ChessPosition(row, colIndex);
  }
  private static String getColoredPiece(String piece) {
    if (piece == null || piece.trim().isEmpty()) {
      return " "; // Empty square
    }

    // Determine if the piece is white or black
    boolean isWhitePiece = Character.isUpperCase(piece.charAt(0));

    String pieceColor = isWhitePiece ? ANSI_WHITE_PIECE : ANSI_BLACK_PIECE;

    // Return the colored piece symbol
    return pieceColor + piece + ANSI_RESET;
  }



  /*public static void drawChessBoard(boolean isWhitePlayer, GameStateDTO gameStateDTO) {
    String[][] boardArray = new String[8][8];

    // Initialize the boardArray with empty spaces
    for (int i = 0; i < 8; i++) {
      Arrays.fill(boardArray[i], " ");
    }

    // Populate the boardArray with pieces from the gameStateDTO's board map
    Map<String, String> boardMap = gameStateDTO.getBoard();
    for (Map.Entry<String, String> entry : boardMap.entrySet()) {
      String position = entry.getKey();
      String piece = entry.getValue();

      int file = position.charAt(0) - 'a';                      // 'a' to 'h' mapped to 0 to 7
      int rank = Character.getNumericValue(position.charAt(1)); // '1' to '8' mapped to 1 to 8

      int arrayRow = 8 - rank; // Adjust rank to array index (0 to 7)
      int arrayCol = file;     // File is already 0 to 7

      boardArray[arrayRow][arrayCol] = piece;
    }

    // Flip the board for black's perspective if needed
    if (!isWhitePlayer) {
      // Reverse the rows
      for (int i = 0; i < 4; i++) {
        String[] temp = boardArray[i];
        boardArray[i] = boardArray[7 - i];
        boardArray[7 - i] = temp;
      }
      // Reverse each row
      for (int i = 0; i < 8; i++) {
        Collections.reverse(Arrays.asList(boardArray[i]));
      }
    }

    // Print the boardArray
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        System.out.print(boardArray[i][j] + " ");
      }
      System.out.println(" " + (8 - i)); // Print rank numbers
    }
    System.out.println("a b c d e f g h");
  }*/

  public static void drawChessBoard(boolean isWhitePlayer, GameStateDTO gameStateDTO) {
    try {
      Map<String, String> boardMap = gameStateDTO.getBoard();

      // Initialize an 8x8 array to represent the board
      String[][] boardArray = new String[8][8];

      // Fill the boardArray with empty strings
      for (int i = 0; i < 8; i++) {
        Arrays.fill(boardArray[i], " ");
      }

      // Place pieces on the boardArray
      for (Map.Entry<String, String> entry : boardMap.entrySet()) {
        String position = entry.getKey();
        String piece = entry.getValue();

        int row = 8 - Character.getNumericValue(position.charAt(1)); // Convert '1'-'8' to 7-0
        int col = position.charAt(0) - 'a'; // Convert 'a'-'h' to 0-7

        boardArray[row][col] = piece;
      }


      // Print the board
      System.out.println();
      for (int row = 0; row < 8; row++) {
        int displayRow = isWhitePlayer ? 8 - row : 8-row;
        System.out.print(displayRow + " "); // Row numbers on the left

        for (int col = 0; col < 8; col++) {

          String piece = boardArray[row][col];
          String squareColor = ((row + col) % 2 == 0) ? ANSI_LIGHT_SQUARE : ANSI_DARK_SQUARE;

          // Ensure full square coloring
          String squareContent = piece.trim().isEmpty() ? "   " : " " + piece + " ";
          System.out.print(squareColor + squareContent + squareColor + ANSI_RESET);
        }

        // Reset background color at the end of the row and print the row number
        System.out.println(ANSI_RESET + " " + displayRow);
      }

      // Print column labels
      System.out.print("  "); // Space before column labels
      for (int col = 0; col < 8; col++) {
        char colLabel = (char) ('a' + (isWhitePlayer ? col : col));
        System.out.print(" " + colLabel + " ");
      }
      System.out.println(); // Move to the next line after column labels
    } catch (Exception e) {
      System.err.println("Exception in drawChessBoard: " + e.getMessage());
      e.printStackTrace();
    }
  }


















}