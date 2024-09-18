package chess;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        private ChessGame.TeamColor teamColor;
        private PieceType pieceType;
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
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        switch(pieceType) {
            case QUEEN:
                addRookMoves(board, position, validMoves);
                addBishopMoves(board, position, validMoves);
                break;

            case ROOK:
                addRookMoves(board, position, validMoves);
                break;

            case BISHOP:
                addBishopMoves(board, position, validMoves);
                break;

            case KNIGHT:
                addKnightMoves(board, position, validMoves);
                break;

            case KING:
                addKingMoves(board, position, validMoves);
                break;

            case PAWN:
                addPawnMoves(board, position, validMoves);
                break;
        }

        return validMoves;
    }
    //Helper methods
    private void addRookMoves(ChessBoard board, ChessPosition position, Collection<ChessMove> validMoves){
        addLineMoves(board, position, validMoves, 1, 0);
        addLineMoves(board, position, validMoves, 1, 0);
        addLineMoves(board, position, validMoves, 1, 0);
        addLineMoves(board, position, validMoves, 0, -1);
    }

    private void addBishopMoves(ChessBoard board, ChessPosition position, Collection<ChessMove> validMove){
        addLineMoves(board, position, validMoves, 1, 1);
        addLineMoves(board, position, validMoves, 1, -1);
        addLineMoves(board, position, validMoves, -1, 1);
        addLineMoves(board, position, validMoves, -1, -1);
    }
    private void addKingMoves(ChessBoard board, ChessPosition position, Collection<ChessMove> validMoves) {
        int[] rowOffsets = {1, 1, 1, 0, 0, -1, -1, -1};
        int[] colOffsets = {1, 0, -1, 1, -1, 1, 0, -1};

        for (int i = 0; i < 8; i++) {
            ChessPosition newPosition = new ChessPosition(position.getRow() + rowOffsets[i], position.getColumn() + colOffsets[i]);
            if (isValidPosition(newPosition) && (board.getPiece(newPosition) == null || board.getPiece(newPosition).getTeamColor() != this.teamColor)) {
                validMoves.add(new ChessMove(position, newPosition, null));
            }
        }
    }

    private void addKnightMoves(ChessBoard board, ChessPosition position, Collection<ChessMove> validMoves) {
        int[] rowOffsets = {2, 2, -2, -2, 1, 1, -1, -1};
        int[] colOffsets = {1, -1, 1, -1, 2, -2, 2, -2};

        for (int i = 0; i < 8; i++) {
            ChessPosition newPosition = new ChessPosition(position.getRow() + rowOffsets[i], position.getColumn() + colOffsets[i]);
            if (isValidPosition(newPosition) && (board.getPiece(newPosition) == null || board.getPiece(newPosition).getTeamColor() != this.teamColor)) {
                validMoves.add(new ChessMove(position, newPosition, null));
            }
        }
    }

    private void addPawnMoves(ChessBoard board, ChessPosition position, Collection<ChessMove> validMoves) {
        int direction = (teamColor == ChessGame.TeamColor.WHITE) ? 1 : -1;

        ChessPosition forward = new ChessPosition(position.getRow() + direction, position.getColumn());
        if (isValidPosition(forward) && board.getPiece(forward) == null) {
            validMoves.add(new ChessMove(position, forward, null));

            if ((teamColor == ChessGame.TeamColor.WHITE && position.getRow() == 1) ||
                    (teamColor == ChessGame.TeamColor.BLACK && position.getRow() == 6)) {
                ChessPosition doubleForward = new ChessPosition(position.getRow() + 2 * direction, position.getColumn());
                if (board.getPiece(doubleForward) == null) {
                    validMoves.add(new ChessMove(position, doubleForward, null));
                }
            }
        }


        ChessPosition leftCapture = new ChessPosition(position.getRow() + direction, position.getColumn() - 1);
        ChessPosition rightCapture = new ChessPosition(position.getRow() + direction, position.getColumn() + 1);
        if (isValidPosition(leftCapture) && board.getPiece(leftCapture) != null && board.getPiece(leftCapture).getTeamColor() != this.teamColor) {
            validMoves.add(new ChessMove(position, leftCapture, null));
        }
        if (isValidPosition(rightCapture) && board.getPiece(rightCapture) != null && board.getPiece(rightCapture).getTeamColor() != this.teamColor) {
            validMoves.add(new ChessMove(position, rightCapture, null));
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
        return position.getRow() >= 0 && position.getRow() < 8 && position.getColumn() >= 0 && position.getColumn() < 8;
    }
}