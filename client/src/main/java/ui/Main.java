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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import chess.ChessBoard;
import java.util.Collection;
import java.io.IOException;



import java.util.Map;
public class Main {

  //colors

  public static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_LIGHT_SQUARE = "\u001B[47m"; // Light gray background
  public static final String ANSI_DARK_SQUARE = "\u001B[40m";  // Dark black background
  public static final String ANSI_WHITE_PIECE = "\u001B[37m";  // White pieces
  public static final String ANSI_BLACK_PIECE = "\u001B[30m";  // Black pieces
  public static final String ANSI_HIGHLIGHT_SQUARE = "\u001B[43m"; // Yellow background


  private static Set<String> highlightedSquares = new HashSet<>();
  private static ChessGame chessGame = new ChessGame();

  public static boolean isWhitePlayer = true; // Set this based on the player's role
  private static volatile GameStateDTO gameStateDTO;

  private static ServerFacade serverFacade;
  private static WebSocketClient webSocketClient;



  private static boolean isLoggedIn = false; // Track whether the user is logged in
  public static boolean isInGame = false; // Tracks if the user is currently in a game

  public static boolean isObserver = false;


  private static Scanner scanner = new Scanner(System.in); // Scanner to read user input

  public static int currentGameID = -1; // Track the current game ID

  public static AtomicBoolean shouldTransitionToPostLogin = new AtomicBoolean(false);

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
            //System.out.println("Processing message: " + message);
            WebSocketMessageHandler.handleMessage(message);
          } else {
            //System.out.println("Received null message from receiveMessage()");
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
      if (shouldTransitionToPostLogin.get()) {
        shouldTransitionToPostLogin.set(false); // Reset the flag
        transitionToPostGame();
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

    String response = serverFacade.playGame(gameName,playerColor);
    System.out.println(response);

    if (response.contains("Successfully joined")) {
      isInGame = true; // Update game state
      isObserver = false;
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
        isObserver = true;
        System.out.println("Observer status: " + isObserver);

        isInGame = true;
        // Fetch the game ID by name
        int gameID = serverFacade.getGameIdByName(gameName);
        if (gameID == -1) {
          System.out.println("Error: Unable to find game ID for the specified game name.");
          isObserver = false; // Reset flag on failure
          isInGame = false;
          return;
        }
        String observerAuthToken = serverFacade.getAuthToken();
        if (observerAuthToken == null || observerAuthToken.isEmpty()) {
          System.out.println("Error: Observer is not logged in.");
          isObserver = false; // Reset flag on failure
          isInGame = false;
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

        gameplayLoop();
      } catch (Exception e) {
        System.err.println("Error observing game via WebSocket: " + e.getMessage());
        isObserver = false; // Reset flag on exception
        isInGame = false;
      }
    }
  }




  private static void showPostloginMenu() {
    while (!isInGame) {
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
    while (isInGame) {
      System.out.println("\nEnter a command: makemove, resign, leave, redraw, highlight, clear, help");
      System.out.print("> ");
      String command = scanner.nextLine().trim().toLowerCase();

      switch (command) {
        case "makemove":
          if (isObserver) {
            System.out.println("Error: Observers cannot make moves.");
          } else {
            makeMove();
          }
          break;
        case "resign":
          if (isObserver) {
            System.out.println("Error: Observers cannot resign.");
          } else {
            resign();
          }
          break;
        case "leave":
          leaveGame();
          return; // Exit gameplay loop
        case "highlight":
          highlightLegalMoves();
          break;
        case "clear":
          clearHighlights();
          break;
        case "help":
          showGameplayHelp();
          break;
        case "redraw":
          redrawBoard();
          break;
        default:
          System.out.println("Invalid command. Type 'help' for a list of commands.");
      }
      if (shouldTransitionToPostLogin.get()) {
        shouldTransitionToPostLogin.set(false); // Reset the flag
        transitionToPostGame();
      }
    }
  }

  private static void redrawBoard() {
    if (gameStateDTO != null) {
      System.out.println("Redrawing board with current game state.");
      drawChessBoard(isWhitePlayer, gameStateDTO, highlightedSquares);
      System.out.println("Board has been redrawn.");
    } else {
      System.out.println("Game state is not available. Please wait for the game to start.");
    }
  }

  private static void leaveGame() {
    if (!isInGame) {
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
    shouldTransitionToPostLogin.set(true); // Set the flag to transition
    isInGame = false; // Update game state
  }
  private static void resign() {
    if (!isInGame) {
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
    isInGame = false;
    currentGameID = -1; // Reset current game ID
  }
  public static synchronized Set<String> getHighlightedSquares() {
    return highlightedSquares;
  }

  public static void transitionToPostGame() {
    System.out.println("\n=== Game Over ===");
    System.out.println("\nReturning to the main menu.\n");
    // Display post-login menu
    showPostloginMenu();
    isInGame = false;
    currentGameID = -1;
  }



  public static synchronized void updateGameState(GameStateDTO updatedState) {
    //System.out.println("Updating gameStateDTO with new state.");
    gameStateDTO = updatedState;
    if(!isObserver){String playerColorStr = gameStateDTO.getPlayerColors().get(serverFacade.getAuthToken());
      if (playerColorStr != null) {
        isWhitePlayer = "WHITE".equalsIgnoreCase(playerColorStr);
        //System.out.println("Player color set to: " + playerColorStr);
      } else {
        System.err.println("Player color not found for authToken: " + serverFacade.getAuthToken());
        isWhitePlayer = true; // Default to white if color not found
      }
    }else{
      isWhitePlayer=true;
    }
    // Update player color based on the updated game state

    updateChessGameFromGameState(updatedState);
    if (highlightedSquares == null) {
      highlightedSquares = new HashSet<>();
    } else {
      highlightedSquares.clear(); // Clear existing highlights when game state updates
    }
    drawChessBoard(isWhitePlayer, gameStateDTO, highlightedSquares);
    //System.out.println("gameStateDTO updated: " + new Gson().toJson(gameStateDTO));
  }


  private static void updateChessGameFromGameState(GameStateDTO gameStateDTO) {
    ChessBoard chessBoard = new ChessBoard();
    for (Map.Entry<String, String> entry : gameStateDTO.getBoard().entrySet()) {
      String square = entry.getKey(); // e.g., "e2"
      String pieceSymbol = entry.getValue(); // e.g., "♙"

      ChessPosition position = convertSquareToChessPosition(square);
      ChessPiece.PieceType pieceType = getPieceTypeFromSymbol(pieceSymbol);
      ChessGame.TeamColor teamColor = isWhitePiece(pieceSymbol) ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;

      ChessPiece chessPiece = new ChessPiece(teamColor, pieceType);
      chessBoard.addPiece(position, chessPiece);
    }

    chessGame.setBoard(chessBoard);

    // You might need to adjust this based on your server's game state
    int totalMoves = gameStateDTO.getPlayers().size(); // Simplistic assumption
    chessGame.setTeamTurn(totalMoves % 2 == 0 ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK);
  }

  private static ChessPiece.PieceType getPieceTypeFromSymbol(String symbol) {
    switch (symbol) {
      case "♔":
      case "♚":
        return ChessPiece.PieceType.KING;
      case "♕":
      case "♛":
        return ChessPiece.PieceType.QUEEN;
      case "♗":
      case "♝":
        return ChessPiece.PieceType.BISHOP;
      case "♘":
      case "♞":
        return ChessPiece.PieceType.KNIGHT;
      case "♖":
      case "♜":
        return ChessPiece.PieceType.ROOK;
      case "♙":
      case "♟":
        return ChessPiece.PieceType.PAWN;
      default:
        throw new IllegalArgumentException("Unknown piece symbol: " + symbol);
    }
  }

  private static boolean isWhitePiece(String symbol) {
    // White pieces are the Unicode symbols for white chess pieces
    switch (symbol) {
      case "♔":
      case "♕":
      case "♗":
      case "♘":
      case "♖":
      case "♙":
        return true;
      case "♚":
      case "♛":
      case "♝":
      case "♞":
      case "♜":
      case "♟":
        return false;
      default:
        throw new IllegalArgumentException("Unknown piece symbol: " + symbol);
    }
  }


  /**
   * Converts a square string (e.g., "e2") to a ChessPosition object.
   *
   * @param square The square string.
   * @return The corresponding ChessPosition.
   */
  private static ChessPosition convertSquareToChessPosition(String square) {
    char colChar = square.charAt(0);
    int row = Character.getNumericValue(square.charAt(1));
    int col = colChar - 'a' + 1;
    return new ChessPosition(row, col);
  }
  private static boolean isValidSquareFormat(String square) {
    return square.matches("^[a-h][1-8]$");
  }


  private static void highlightLegalMoves() {
    System.out.print("Enter the square of the piece to highlight (e.g., e2): ");
    String input = scanner.nextLine().trim().toLowerCase();

    // Validate input format
    if (!isValidSquareFormat(input)) {
      System.out.println("Error: Invalid square format. Please enter a valid square (e.g., e2).");
      return;
    }

    // Check if the selected square has a piece
    if (!gameStateDTO.getBoard().containsKey(input)) {
      System.out.println("Error: No piece found at " + input + ".");
      return;
    }

    // For players, ensure they can only highlight their own pieces
    if (!isObserver) {
      String playerColor = gameStateDTO.getPlayerColors().get(serverFacade.getAuthToken());
      String pieceSymbol = gameStateDTO.getBoard().get(input);
      boolean isWhite = isWhitePiece(pieceSymbol);

      if ((isWhite && !isWhitePlayer) || (!isWhite && isWhitePlayer)) {
        System.out.println("Error: You can only highlight your own pieces.");
        return;
      }
    }

    // Convert input to ChessPosition
    ChessPosition selectedPosition = convertSquareToChessPosition(input);
    System.out.println("Selected Position: " + selectedPosition.getRow() + ", " + selectedPosition.getColumn()); // Debug Statement

    // Determine the piece's color if observer
    ChessGame.TeamColor originalTeamTurn = chessGame.getTeamTurn();
    if (isObserver) {
      String pieceSymbol = gameStateDTO.getBoard().get(input);
      ChessGame.TeamColor pieceColor = isWhitePiece(pieceSymbol) ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
      chessGame.setTeamTurn(pieceColor); // Temporarily set team turn to piece's color
    }

    // Get legal moves from ChessGame
    Collection<ChessMove> legalMovesCollection = chessGame.validMoves(selectedPosition);

    // Restore original team turn if observer
    if (isObserver) {
      chessGame.setTeamTurn(originalTeamTurn);
    }

    if (legalMovesCollection == null || legalMovesCollection.isEmpty()) {
      System.out.println("No legal moves available for the selected piece.");
      return;
    }

    System.out.println("Legal moves found: " + legalMovesCollection.size()); // Debug Statement

    // Convert ChessMove to square keys
    Set<String> legalMoveSquares = new HashSet<>();
    for (ChessMove move : legalMovesCollection) {
      String toSquare = move.getEndPosition().toString(); // e.g., "e4"
      legalMoveSquares.add(toSquare);
    }

    // Update highlightedSquares
    highlightedSquares.clear();
    highlightedSquares.add(input); // Highlight selected square
    highlightedSquares.addAll(legalMoveSquares); // Highlight legal move squares

    // Redraw the board with highlights
    drawChessBoard(isWhitePlayer, gameStateDTO, highlightedSquares);
    System.out.println("Legal moves for " + input + " have been highlighted.");
  }


  private static ChessPiece.PieceType getPieceType(String piece) {
    switch (piece.toUpperCase()) {
      case "♙":
        return ChessPiece.PieceType.PAWN;
      case "♘":
        return ChessPiece.PieceType.KNIGHT;
      case "♗":
        return ChessPiece.PieceType.BISHOP;
      case "♖":
        return ChessPiece.PieceType.ROOK;
      case "♕":
        return ChessPiece.PieceType.QUEEN;
      case "♔":
        return ChessPiece.PieceType.KING;
      // Add cases for black pieces if necessary
      default:
        return null;
    }
  }
  private static void clearHighlights() {
    if (highlightedSquares.isEmpty()) {
      System.out.println("No highlights to clear.");
      return;
    }

    highlightedSquares.clear();
    if (gameStateDTO != null) {
      drawChessBoard(isWhitePlayer, gameStateDTO, highlightedSquares);
      System.out.println("Highlights have been cleared.");
    } else {
      System.out.println("Game state is not available.");
    }
  }
  private static void showGameplayHelp() {
    System.out.println("In-Game Commands:");
    System.out.println("  makemove - Make a chess move (e.g., e2e4)");
    System.out.println("  resign   - Resign from the game");
    System.out.println("  leave    - Leave the game without resigning");
    System.out.println("  highlight - Highlight legal moves for a selected piece");
    System.out.println("  clear     - Clear all highlighted squares");
    System.out.println("  redraw    - Redraw the chessboard");
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

    //System.out.println("Parsed Move: " + move.toString());

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


  public static void drawChessBoard(boolean isWhitePlayer, GameStateDTO gameStateDTO, Set<String> highlightedSquares) {
    try {
      Map<String, String> boardMap = gameStateDTO.getBoard();

      // Initialize an 8x8 array to represent the board
      String[][] boardArray = new String[8][8];

      // Fill the boardArray with piece symbols or empty strings
      for (Map.Entry<String, String> entry : boardMap.entrySet()) {
        String position = entry.getKey(); // e.g., "e2"
        String piece = entry.getValue();  // e.g., "♙"

        ChessPosition chessPosition = convertSquareToChessPosition(position);
        int row = isWhitePlayer ? 8 - chessPosition.getRow() : chessPosition.getRow() - 1;
        int col = chessPosition.getColumn() - 1;

        boardArray[row][col] = piece;
      }

      // Print the board
      System.out.println();
      for (int row = 0; row < 8; row++) {
        // Determine the display row number
        int displayRow = isWhitePlayer ? 8 - row : row + 1;
        System.out.print(displayRow + " "); // Row numbers on the left

        for (int col = 0; col < 8; col++) {
          String piece = boardArray[row][col];
          String squareKey = isWhitePlayer ?
                  getSquareKey(8 - row, col + 1) :
                  getSquareKey(row + 1, col + 1); // Adjust based on player color

          boolean isLightSquare = (row + col) % 2 == 0;

          String squareColor;
          if (highlightedSquares.contains(squareKey)) {
            squareColor = ANSI_HIGHLIGHT_SQUARE; // Highlight color
          } else {
            squareColor = isLightSquare ? ANSI_LIGHT_SQUARE : ANSI_DARK_SQUARE;
          }

          // Determine piece color based on square background and piece ownership
          String pieceColor = ""; if (piece != null) {
            if (isWhitePiece(piece)) {
              pieceColor = isLightSquare ? ANSI_BLACK_PIECE : ANSI_WHITE_PIECE;
            } else {
              pieceColor = isLightSquare ? ANSI_BLACK_PIECE : ANSI_WHITE_PIECE;
            }
          }

          // Prepare square content
          String squareContent = (piece != null) ? " " + piece + " " : "   ";

          // Print the square with appropriate colors
          System.out.print(squareColor + pieceColor + squareContent + ANSI_RESET);
        }

        // Reset background color at the end of the row and print the row number
        System.out.println(" " + displayRow);
      }

      // Print column labels
      System.out.print("  "); // Space before column labels
      for (int col = 0; col < 8; col++) {
        char colLabel = isWhitePlayer ? (char) ('a' + col) : (char) ('h' - col);
        System.out.print(" " + colLabel + " ");
      }
      System.out.println(); // Move to the next line after column labels
    } catch (Exception e) {
      System.err.println("Exception in drawChessBoard: " + e.getMessage());
      e.printStackTrace();
    }
  }



  private static String getSquareKey(int row, int col) {
    char column = (char) ('a' + col - 1);
    return "" + column + row;
  }


}