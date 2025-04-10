package ui.chess;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

public class ChessUtility {
    public static boolean isValidSquareFormat(String square) {
        return square.matches("^[a-h][1-8]$");
    }

    public static boolean isValidMoveFormat(String move) {
        return move.matches("^[a-h][1-8][a-h][1-8][QRBN]?$");
    }

    public static ChessMove parseMove(String input) {
        // Parse start and end positions
        int startCol = input.charAt(0) - 'a' + 1;
        int startRow = Character.getNumericValue(input.charAt(1));
        int endCol = input.charAt(2) - 'a' + 1;
        int endRow = Character.getNumericValue(input.charAt(3));

        ChessPosition startPos = new ChessPosition(startRow, startCol);
        ChessPosition endPos = new ChessPosition(endRow, endCol);

        // Since this is not a promotion move, we pass null for promotionPiece
        return new ChessMove(startPos, endPos, null);
    }

    public static ChessPiece.PieceType getPieceType(String piece) {
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

    public static ChessPiece.PieceType getPieceTypeFromSymbol(String symbol) {
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
    
    /**
     * Converts a chess square notation (e.g., "e4") to a ChessPosition object.
     * 
     * @param square The square notation (e.g., "e4")
     * @return A ChessPosition object representing the position
     */
    public static ChessPosition convertSquareToChessPosition(String square) {
        char colChar = square.charAt(0);
        int row = Character.getNumericValue(square.charAt(1));
        int col = colChar - 'a' + 1;
        return new ChessPosition(row, col);
    }
}