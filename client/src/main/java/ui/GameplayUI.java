package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.ChessPiece;
import chess.ChessBoard;
import websocket.WebSocketClient;
import websocket.dto.GameStateDTO;
import websocket.GameState;
import ui.chess.ChessBoardRenderer;
import ui.chess.ChessUtility;
import com.google.gson.Gson;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameplayUI {
    private static Set<String> highlightedSquares = new HashSet<>();
    private static ChessGame chessGame = new ChessGame();
    private static volatile GameStateDTO gameStateDTO;
    private static boolean isWhitePlayer = true;
    private static boolean isObserver = false;
    private static boolean isInGame = false;
    private static int currentGameID = -1;
    private static Scanner scanner = new Scanner(System.in);
    private static ServerFacade serverFacade;
    private static WebSocketClient webSocketClient;
    private static AtomicBoolean shouldTransitionToPostLogin = new AtomicBoolean(false);

    public static void init(ServerFacade facade, WebSocketClient client) {
        serverFacade = facade;
        webSocketClient = client;
    }

    public static void gameplayLoop() {
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

    public static void redrawBoard() {
        if (gameStateDTO != null) {
            System.out.println("Redrawing board with current game state.");
            ChessBoardRenderer.drawChessBoard(isWhitePlayer, gameStateDTO, highlightedSquares);
            System.out.println("Board has been redrawn.");
        } else {
            System.out.println("Game state is not available. Please wait for the game to start.");
        }
    }

    private static void makeMove() {
        System.out.print("Enter your move (e.g., e2e4): ");
        String moveInput = scanner.nextLine().trim();

        if (!ChessUtility.isValidMoveFormat(moveInput)) {
            System.out.println("Error: Invalid move format. Please use standard chess notation (e.g., e2e4).");
            return;
        }

        // Parse move input
        ChessMove move = ChessUtility.parseMove(moveInput);
        if (move == null) {
            System.out.println("Error: Failed to parse move. Please try again.");
            return;
        }

        // Send move via WebSocketClient
        try {
            webSocketClient.sendMakeMoveCommand(
                    serverFacade.getAuthToken(),
                    serverFacade.getLastGameID(),
                    move
            );

            System.out.println("Waiting for server response...");

        } catch (Exception e) {
            System.err.println("Error: Failed to send move command. " + e.getMessage());
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

    private static void highlightLegalMoves() {
        System.out.print("Enter the square of the piece to highlight (e.g., e2): ");
        String input = scanner.nextLine().trim().toLowerCase();

        // Validate input format
        if (!ChessUtility.isValidSquareFormat(input)) {
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
            boolean isWhite = ChessBoardRenderer.isWhitePiece(pieceSymbol);

            if ((isWhite && !isWhitePlayer) || (!isWhite && isWhitePlayer)) {
                System.out.println("Error: You can only highlight your own pieces.");
                return;
            }
        }

        // Convert input to ChessPosition
        ChessPosition selectedPosition = ChessUtility.convertSquareToChessPosition(input);
        System.out.println("Selected Position: " + selectedPosition.getRow() + ", " + selectedPosition.getColumn());

        // Determine the piece's color if observer
        ChessGame.TeamColor originalTeamTurn = chessGame.getTeamTurn();
        if (isObserver) {
            String pieceSymbol = gameStateDTO.getBoard().get(input);
            ChessGame.TeamColor pieceColor = ChessBoardRenderer.isWhitePiece(pieceSymbol) ? 
                ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
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

        System.out.println("Legal moves found: " + legalMovesCollection.size());

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
        ChessBoardRenderer.drawChessBoard(isWhitePlayer, gameStateDTO, highlightedSquares);
        System.out.println("Legal moves for " + input + " have been highlighted.");
    }

    private static void clearHighlights() {
        if (highlightedSquares.isEmpty()) {
            System.out.println("No highlights to clear.");
            return;
        }

        highlightedSquares.clear();
        if (gameStateDTO != null) {
            ChessBoardRenderer.drawChessBoard(isWhitePlayer, gameStateDTO, highlightedSquares);
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

    public static void transitionToPostGame() {
        System.out.println("\n=== Game Over ===");
        System.out.println("\nReturning to the main menu.\n");
        // Display post-login menu
        Main.showPostloginMenu();
        isInGame = false;
        currentGameID = -1;
    }

    public static synchronized void updateGameState(GameStateDTO updatedState) {
        gameStateDTO = updatedState;
        
        if (!isObserver) {
            String playerColorStr = gameStateDTO.getPlayerColors().get(serverFacade.getAuthToken());
            if (playerColorStr != null) {
                isWhitePlayer = "WHITE".equalsIgnoreCase(playerColorStr);
            } else {
                System.err.println("Player color not found for authToken: " + serverFacade.getAuthToken());
                isWhitePlayer = true; // Default to white if color not found
            }
        } else {
            isWhitePlayer = true;
        }

        updateChessGameFromGameState(updatedState);
        
        if (highlightedSquares == null) {
            highlightedSquares = new HashSet<>();
        } else {
            highlightedSquares.clear(); // Clear existing highlights when game state updates
        }
        
        ChessBoardRenderer.drawChessBoard(isWhitePlayer, gameStateDTO, highlightedSquares);
    }

    private static void updateChessGameFromGameState(GameStateDTO gameStateDTO) {
        ChessBoard chessBoard = new ChessBoard();
        for (Map.Entry<String, String> entry : gameStateDTO.getBoard().entrySet()) {
            String square = entry.getKey(); // e.g., "e2"
            String pieceSymbol = entry.getValue(); // e.g., "♙"

            ChessPosition position = ChessUtility.convertSquareToChessPosition(square);
            ChessPiece.PieceType pieceType = ChessUtility.getPieceTypeFromSymbol(pieceSymbol);
            ChessGame.TeamColor teamColor = ChessBoardRenderer.isWhitePiece(pieceSymbol) ? 
                ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;

            ChessPiece chessPiece = new ChessPiece(teamColor, pieceType);
            chessBoard.addPiece(position, chessPiece);
        }

        chessGame.setBoard(chessBoard);

        // Set the team turn based on the game state
        int totalMoves = gameStateDTO.getPlayers().size(); // Simplistic assumption
        chessGame.setTeamTurn(totalMoves % 2 == 0 ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK);
    }

    // Using the utility method from ChessUtility class

    // Getters and setters for various fields
    public static void setIsObserver(boolean value) {
        isObserver = value;
    }

    public static void setIsInGame(boolean value) {
        isInGame = value;
    }

    public static void setCurrentGameID(int id) {
        currentGameID = id;
    }

    public static Set<String> getHighlightedSquares() {
        return highlightedSquares;
    }

    public static AtomicBoolean getShouldTransitionToPostLogin() {
        return shouldTransitionToPostLogin;
    }
}