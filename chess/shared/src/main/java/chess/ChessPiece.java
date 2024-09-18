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
}
