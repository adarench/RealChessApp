package chess;

import java.util.Collection;
import java.util.ArrayList;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessPosition lastDoubleMovePawn = null;
    private boolean isCheckingCheck = false;
    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteKingsideRookMoved = false;
    private boolean whiteQueensideRookMoved = false;
    private boolean blackKingsideRookMoved = false;
    private boolean blackQueensideRookMoved = false;
    private ChessBoard board;
    private TeamColor currentTurn;
    public ChessGame() {
        this.board = new ChessBoard();
        this.currentTurn = TeamColor.WHITE;
        this.board.resetBoard();
    }

    public boolean hasKingMoved(TeamColor teamColor) {
        return (teamColor == TeamColor.WHITE) ? whiteKingMoved : blackKingMoved;
    }
    public boolean hasRookMoved(TeamColor teamColor, boolean isKingside) {
        if (teamColor == TeamColor.WHITE) {
            return isKingside ? whiteKingsideRookMoved : whiteQueensideRookMoved;
        } else {
            return isKingside ? blackKingsideRookMoved : blackQueensideRookMoved;
        }
    }
    /*public void updateMoveFlags(ChessPiece piece, ChessPosition startPosition) {
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (piece.getTeamColor() == TeamColor.WHITE) {
                whiteKingMoved = true;
            } else {
                blackKingMoved = true;
            }
        } else if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            if (piece.getTeamColor() == TeamColor.WHITE) {
                if (startPosition.getColumn() == 1) {
                    whiteQueensideRookMoved = true;
                } else if (startPosition.getColumn() == 8) {
                    whiteKingsideRookMoved = true;
                }
            } else {
                if (startPosition.getColumn() == 1) {
                    blackQueensideRookMoved = true;
                } else if (startPosition.getColumn() == 8) {
                    blackKingsideRookMoved = true;
                }
            }
        }
    }*/

    private boolean isSquareUnderAttack(ChessBoard board, ChessPosition position, ChessGame.TeamColor teamColor) {
    ChessGame.TeamColor opponentColor = (teamColor == ChessGame.TeamColor.WHITE) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
    if(position == null){
        return false;
    }
    for (int row = 1; row <= 8; row++) {
        for (int col = 1; col <= 8; col++) {
            ChessPosition attackerPosition = new ChessPosition(row, col);
            ChessPiece attacker = board.getPiece(attackerPosition);

            if (attacker != null && attacker.getTeamColor() == opponentColor) {
                if (attacker.getPieceType() == ChessPiece.PieceType.PAWN) {
                    // Check pawn attack positions
                    int direction = (opponentColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
                    int attackRow = attackerPosition.getRow() + direction;

                    if (attackRow == position.getRow()) {
                        if (attackerPosition.getColumn() - 1 == position.getColumn() || attackerPosition.getColumn() + 1 == position.getColumn()) {
                            return true; // Position is under attack by pawn
                        }
                    }
                } else {
                    Collection<ChessMove> attackerMoves = attacker.pieceMoves(board, attackerPosition);
                    for (ChessMove attackerMove : attackerMoves) {
                        if (attackerMove.getEndPosition().equals(position)) {
                            return true; // Position is under attack
                        }
                    }
                }
            }
        }
    }
    return false;
}

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    /*public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null){
            return null;
        }
        return piece.pieceMoves(board, startPosition, this);
    }*/

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> legalMoves = new ArrayList<>();

        for (ChessMove move : possibleMoves) {
            // Create a copy of the board
            ChessBoard tempBoard = board.deepCopyBoard();

            // Simulate the move on the tempBoard
            tempBoard.addPiece(move.getEndPosition(), tempBoard.getPiece(move.getStartPosition()));
            tempBoard.addPiece(move.getStartPosition(), null);

            // If the moving piece is the king, check if the new position is safe
            if (piece.getPieceType() == ChessPiece.PieceType.KING) {
                if (!isSquareUnderAttack(tempBoard, move.getEndPosition(), piece.getTeamColor())) {
                    legalMoves.add(move);
                }
            } else {
                // For other pieces, check if the move leaves the king in check
                if (!isKingInCheckAfterMove(tempBoard, piece.getTeamColor())) {
                    legalMoves.add(move);
                }
            }
        }
        return legalMoves;
    }

    private boolean isKingInCheckAfterMove(ChessBoard board, ChessGame.TeamColor teamColor) {
        // Find the king's position on the updated board
        ChessPosition kingPosition = null;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                    kingPosition = pos;
                    break;
                }
            }
            if (kingPosition != null) break;
        }

        // Check if the king is under attack
        return isSquareUnderAttack(board, kingPosition, teamColor);
    }




    /**
     * Makes a move in a chess game
     *
     *
     * @throws InvalidMoveException if move is invalid
     */

    /*public boolean canCastle(ChessPosition kingPosition, boolean isKingside) {
        int row = kingPosition.getRow();
        int rookCol = isKingside ? 8 : 1;
        int stepDirection = isKingside ? 1 : -1;

        // Check if the king or rook has already moved
        if (hasKingMoved(currentTurn) || hasRookMoved(currentTurn, isKingside)) {
            return false;
        }

        // Ensure there are no pieces between the king and the rook
        for (int col = kingPosition.getColumn() + stepDirection; col != rookCol; col += stepDirection) {
            if (board.getPiece(new ChessPosition(row, col)) != null) {
                return false;
            }
        }

        // Ensure the king does not pass through or end up in check
        for (int col = kingPosition.getColumn(); col != kingPosition.getColumn() + 2 * stepDirection; col += stepDirection) {
            ChessPosition pos = new ChessPosition(row, col);
            if (isSquareUnderAttack(board, pos, currentTurn)) {
                return false;
            }
        }

        return true;
    }*/

    public void makeMove(ChessMove move) throws InvalidMoveException {
        // Get the piece at the start position
        ChessPiece piece = board.getPiece(move.getStartPosition());
        // Check if piece exists or belongs to current team
        if (piece == null || piece.getTeamColor() != currentTurn){
            throw new InvalidMoveException("No valid piece at this position or wrong team's turn");
        }
        // Get valid moves
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());

        // Modify the move validation here
        boolean moveFound = false;
        for (ChessMove validMove : validMoves) {
            if (validMove.getStartPosition().equals(move.getStartPosition()) &&
                    validMove.getEndPosition().equals(move.getEndPosition())) {
                moveFound = true;
                break;
            }
        }
        if (!moveFound) {
            throw new InvalidMoveException("Move not valid for this piece");
        }

        // Proceed with move validation and execution
        // Clone the board and simulate the move
        ChessBoard tempBoard = board.deepCopyBoard();
        tempBoard.addPiece(move.getEndPosition(), piece);
        tempBoard.addPiece(move.getStartPosition(), null); // Clear start position

        // Check if the move leaves king in check
        if(isInCheck(currentTurn)){
            throw new InvalidMoveException("Move leaves the king in check");
        }

        // Move is validated, check for pawn promotion case
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            // Check if the pawn has reached the last row
            if ((piece.getTeamColor() == TeamColor.WHITE && move.getEndPosition().getRow() == 8) ||
                    (piece.getTeamColor() == TeamColor.BLACK && move.getEndPosition().getRow() == 1)) {

                // Get the promotion type
                ChessPiece.PieceType promotionType = move.getPromotionPiece();
                if (promotionType != null) {
                    // Create the promoted piece
                    ChessPiece promotedPiece = new ChessPiece(piece.getTeamColor(), promotionType);

                    // Place the promoted piece at the end position
                    board.addPiece(move.getEndPosition(), promotedPiece);
                    board.addPiece(move.getStartPosition(), null); // Clear start position
                } else {
                    // If no promotion type was specified, promote automatically to Queen
                    ChessPiece promotedPiece = new ChessPiece(piece.getTeamColor(), ChessPiece.PieceType.QUEEN);
                    board.addPiece(move.getEndPosition(), promotedPiece);
                    board.addPiece(move.getStartPosition(), null);
                }
            } else {
                // If the pawn hasn't reached the last row, perform a regular pawn move
                board.addPiece(move.getEndPosition(), piece);
                board.addPiece(move.getStartPosition(), null); // Clear start position
            }
        } else {
            // For other piece types, perform a regular move
            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(move.getStartPosition(), null); // Clear start position
        }

        // Switch turns
        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }


    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */

    //iterate through pieces to see if king is in check
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(teamColor);
        TeamColor opponentColor = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == opponentColor) {
                    /* skip the opponent's king to avoid recursion
                    if (piece.getPieceType() == ChessPiece.PieceType.KING) {
                        continue;
                    }*/

                    Collection<ChessMove> moves = piece.pieceMoves(board, position, this);
                    for (ChessMove move : moves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true; // King is in check
                        }
                    }
                }
            }
        }
        return false; // King is not in check
    }


    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false; // Not in check, so not checkmate
        }

        // Search for a valid move to resolve check
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> validMoves = validMoves(position);

                    if (validMoves != null && !validMoves.isEmpty()) {
                        return false; // Found a move that resolves the check
                    }
                }
            }
        }

        // No valid moves to resolve the check; it's checkmate
        return true;
    }




    //helper method to find king position

    private ChessPosition findKingPosition(TeamColor teamColor){
        for (int row = 1; row <= 8; row++){
            for (int col = 1; col <= 8; col++){
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if(piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor){
                    return position; //found king
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)){
            return false;
        }

        for(int row  = 1; row <= 8; row++){
            for (int col = 1; col <= 8; col++){
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor){
                    Collection<ChessMove> validMoves = validMoves(position);
                    if(!validMoves.isEmpty()){
                        return false;
                    }
                }
            }
        }
        return true;  //no valid moves it's a stalemate
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {

        return board;
    }
}
