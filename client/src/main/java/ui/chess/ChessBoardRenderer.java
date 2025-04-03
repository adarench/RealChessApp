package ui.chess;

import chess.ChessGame;
import chess.ChessPosition;
import chess.ChessPiece;
import websocket.dto.GameStateDTO;

import java.util.Map;
import java.util.Set;

public class ChessBoardRenderer {
    // ANSI color constants
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_LIGHT_SQUARE = "\u001B[47m"; // Light gray background
    public static final String ANSI_DARK_SQUARE = "\u001B[40m";  // Dark black background
    public static final String ANSI_WHITE_PIECE = "\u001B[37m";  // White pieces
    public static final String ANSI_BLACK_PIECE = "\u001B[30m";  // Black pieces
    public static final String ANSI_HIGHLIGHT_SQUARE = "\u001B[43m"; // Yellow background

    /**
     * Draws a chess board with the current game state
     */
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
                int col = isWhitePlayer ? chessPosition.getColumn() - 1 : 8 - chessPosition.getColumn();

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
                            getSquareKey(row + 1, 8 - col); // Properly flip columns for black's perspective

                    boolean isLightSquare = (row + col) % 2 == 0;

                    String squareColor;
                    if (highlightedSquares.contains(squareKey)) {
                        squareColor = ANSI_HIGHLIGHT_SQUARE; // Highlight color
                    } else {
                        squareColor = isLightSquare ? ANSI_LIGHT_SQUARE : ANSI_DARK_SQUARE;
                    }

                    // Determine piece color based on square background and piece ownership
                    String pieceColor = "";
                    if (piece != null) {
                        pieceColor = isLightSquare ? ANSI_BLACK_PIECE : ANSI_WHITE_PIECE;
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
                char colLabel = isWhitePlayer ? (char) ('a' + col) : (char) ('a' + (7 - col));
                System.out.print(" " + colLabel + " ");
            }
            System.out.println(); // Move to the next line after column labels
        } catch (Exception e) {
            System.err.println("Exception in drawChessBoard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static ChessPosition convertSquareToChessPosition(String square) {
        char colChar = square.charAt(0);
        int row = Character.getNumericValue(square.charAt(1));
        int col = colChar - 'a' + 1;
        return new ChessPosition(row, col);
    }

    private static String getSquareKey(int row, int col) {
        char column = (char) ('a' + col - 1);
        return "" + column + row;
    }

    public static boolean isWhitePiece(String symbol) {
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
}