package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;
import chess.ChessGame;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor teamColor;
    private PieceType pieceType;
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceType = type;
        this.teamColor = pieceColor;
    }


    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return teamColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return pieceMoves(board, myPosition, null);
    }
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition, ChessGame game) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        switch(pieceType) {
            case QUEEN:
                addRookMoves(board, myPosition, validMoves);
                addBishopMoves(board, myPosition, validMoves);
                break;

            case ROOK:
                addRookMoves(board, myPosition, validMoves);
                break;

            case BISHOP:
                addBishopMoves(board, myPosition, validMoves);
                break;

            case KNIGHT:
                addKnightMoves(board, myPosition, validMoves);
                break;

            case KING:
                addKingMoves(board, myPosition, validMoves, game);
                break;

            case PAWN:
                addPawnMoves(board, myPosition, validMoves);
                break;
        }

        return validMoves;
    }
    //Helper methods
    private void addRookMoves(ChessBoard board, ChessPosition position, Collection<ChessMove> validMoves) {
        // Move up (row decrement)
        addLineMoves(board, position, validMoves, -1, 0);
        // Move down (row increment)
        addLineMoves(board, position, validMoves, 1, 0);
        // Move left (column decrement)
        addLineMoves(board, position, validMoves, 0, -1);
        // Move right (column increment)
        addLineMoves(board, position, validMoves, 0, 1);
    }


    private void addBishopMoves(ChessBoard board, ChessPosition position, Collection<ChessMove> validMoves) {

        // Top-right diagonal
        addLineMoves(board, position, validMoves, -1, 1);

        // Bottom-right diagonal
        addLineMoves(board, position, validMoves, 1, 1);

        // Bottom-left diagonal
        addLineMoves(board, position, validMoves, 1, -1);

        // Top-left diagonal
        addLineMoves(board, position, validMoves, -1, -1);
    }


    private void addKingMoves(ChessBoard board, ChessPosition position, Collection<ChessMove> validMoves, ChessGame game) {
        int[] rowOffsets = {1, 1, 1, 0, 0, -1, -1, -1};
        int[] colOffsets = {1, 0, -1, 1, -1, 1, 0, -1};

        addMovesFromOffsets(board, position, validMoves, rowOffsets, colOffsets);

    }



    private void addKnightMoves(ChessBoard board, ChessPosition position, Collection<ChessMove> validMoves) {
        int[] rowOffsets = {2, 2, -2, -2, 1, 1, -1, -1};
        int[] colOffsets = {1, -1, 1, -1, 2, -2, 2, -2};

        addMovesFromOffsets(board, position, validMoves, rowOffsets, colOffsets);

    }

    private void addMovesFromOffsets(ChessBoard board, ChessPosition position, Collection<ChessMove> validMoves, int[] rowOffsets, int[] colOffsets) {
        for (int i = 0; i < rowOffsets.length; i++) {
            ChessPosition newPosition = new ChessPosition(position.getRow() + rowOffsets[i],
                    position.getColumn() + colOffsets[i]);
            if (isValidPosition(newPosition) &&
                    (board.getPiece(newPosition) == null || board.getPiece(newPosition).getTeamColor() != this.teamColor)) {
                validMoves.add(new ChessMove(position, newPosition, null));
            }
        }
    }
    private void addPawnMoves(ChessBoard board, ChessPosition position, Collection<ChessMove> validMoves) {
        int direction = (teamColor == ChessGame.TeamColor.WHITE) ? 1 : -1;

        // Forward Move
        ChessPosition forward = new ChessPosition(position.getRow() + direction, position.getColumn());
        if (isValidPosition(forward) && board.getPiece(forward) == null) {
            if ((teamColor == ChessGame.TeamColor.WHITE && forward.getRow() == 8) ||
                    (teamColor == ChessGame.TeamColor.BLACK && forward.getRow() == 1)) {
                // Promotion moves: Pawn can become Queen, Rook, Bishop, or Knight
                for (ChessPiece.PieceType promotionType : new ChessPiece.PieceType[]{
                        ChessPiece.PieceType.QUEEN,
                        ChessPiece.PieceType.ROOK,
                        ChessPiece.PieceType.BISHOP,
                        ChessPiece.PieceType.KNIGHT}) {
                    validMoves.add(new ChessMove(position, forward, promotionType));
                }
            } else {
                validMoves.add(new ChessMove(position, forward, null));
            }

            // Double forward move (only if pawn is in starting position)
            if ((teamColor == ChessGame.TeamColor.WHITE && position.getRow() == 2) ||
                    (teamColor == ChessGame.TeamColor.BLACK && position.getRow() == 7)) {
                ChessPosition doubleForward = new ChessPosition(position.getRow() + 2 * direction, position.getColumn());
                if (board.getPiece(doubleForward) == null) {
                    validMoves.add(new ChessMove(position, doubleForward, null));
                }
            }
        }

        // Capture diagonally left
        ChessPosition leftCapture = new ChessPosition(position.getRow() + direction, position.getColumn() - 1);
        if (isValidPosition(leftCapture) && board.getPiece(leftCapture) != null && board.getPiece(leftCapture).getTeamColor() != this.teamColor) {
            if ((teamColor == ChessGame.TeamColor.WHITE && leftCapture.getRow() == 8) ||
                    (teamColor == ChessGame.TeamColor.BLACK && leftCapture.getRow() == 1)) {
                // Promotion moves on capture
                for (ChessPiece.PieceType promotionType : new ChessPiece.PieceType[]{
                        ChessPiece.PieceType.QUEEN,
                        ChessPiece.PieceType.ROOK,
                        ChessPiece.PieceType.BISHOP,
                        ChessPiece.PieceType.KNIGHT}) {
                    validMoves.add(new ChessMove(position, leftCapture, promotionType));
                }
            } else {
                validMoves.add(new ChessMove(position, leftCapture, null));
            }
        }

        // Capture diagonally right
        ChessPosition rightCapture = new ChessPosition(position.getRow() + direction, position.getColumn() + 1);
        if (isValidPosition(rightCapture) && board.getPiece(rightCapture) != null && board.getPiece(rightCapture).getTeamColor() != this.teamColor) {
            if ((teamColor == ChessGame.TeamColor.WHITE && rightCapture.getRow() == 8) ||
                    (teamColor == ChessGame.TeamColor.BLACK && rightCapture.getRow() == 1)) {
                // Promotion moves on capture
                for (ChessPiece.PieceType promotionType : new ChessPiece.PieceType[]{
                        ChessPiece.PieceType.QUEEN,
                        ChessPiece.PieceType.ROOK,
                        ChessPiece.PieceType.BISHOP,
                        ChessPiece.PieceType.KNIGHT}) {
                    validMoves.add(new ChessMove(position, rightCapture, promotionType));
                }
            } else {
                validMoves.add(new ChessMove(position, rightCapture, null));
            }
        }
    }

    private void addLineMoves(ChessBoard board, ChessPosition position, Collection<ChessMove> validMoves, int rowIncrement, int colIncrement) {
        int currentRow = position.getRow() + rowIncrement;
        int currentCol = position.getColumn() + colIncrement;

        while (isValidPosition(new ChessPosition(currentRow, currentCol))) {
            ChessPosition newPosition = new ChessPosition(currentRow, currentCol);
            ChessPiece pieceAtNewPosition = board.getPiece(newPosition);

            if (pieceAtNewPosition == null) {
                validMoves.add(new ChessMove(position, newPosition, null));
            } else {

                if (pieceAtNewPosition.getTeamColor() != this.teamColor) {
                    validMoves.add(new ChessMove(position, newPosition, null));
                }
                break;
            }

            currentRow += rowIncrement;
            currentCol += colIncrement;
        }
    }
    private boolean isValidPosition(ChessPosition position) {
        return position.getRow() >= 1 && position.getRow() <= 8 && position.getColumn() >= 1 && position.getColumn() <= 8;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;}
        if (o == null || getClass() != o.getClass()){
            return false;}

        ChessPiece that = (ChessPiece) o;

        return teamColor == that.teamColor && pieceType == that.pieceType;
    }
    @Override
    public String toString() {
        switch (this.pieceType) {
            case PAWN:
                return this.teamColor == ChessGame.TeamColor.WHITE ? "♙" : "♟";
            case ROOK:
                return this.teamColor == ChessGame.TeamColor.WHITE ? "♖" : "♜";
            case KNIGHT:
                return this.teamColor == ChessGame.TeamColor.WHITE ? "♘" : "♞";
            case BISHOP:
                return this.teamColor == ChessGame.TeamColor.WHITE ? "♗" : "♝";
            case QUEEN:
                return this.teamColor == ChessGame.TeamColor.WHITE ? "♕" : "♛";
            case KING:
                return this.teamColor == ChessGame.TeamColor.WHITE ? "♔" : "♚";
            default:
                return " "; // Empty square
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamColor, pieceType);
    }
}
