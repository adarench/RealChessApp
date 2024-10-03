package chess;

import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessPosition lastDoubleMovePawn = null;
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
    public void updateMoveFlags(ChessPiece piece, ChessPosition startPosition) {
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
    }

    public boolean isSquareUnderAttack(ChessBoard board, ChessPosition position, TeamColor defenderColor) {
        // Loop through all the pieces on the board
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(currentPosition);

                // If the piece belongs to the opponent, check its valid moves
                if (piece != null && piece.getTeamColor() != defenderColor) {
                    Collection<ChessMove> opponentMoves = piece.pieceMoves(board, currentPosition, this);
                    for (ChessMove move : opponentMoves) {
                        if (move.getEndPosition().equals(position)) {
                            return true; // The square is under attack
                        }
                    }
                }
            }
        }
        return false; // The square is not under attack
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
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null){
            return null;
        }
        return piece.pieceMoves(board, startPosition, this);
    }

    /**
     * Makes a move in a chess game
     *
     *
     * @throws InvalidMoveException if move is invalid
     */

    public boolean canCastle(ChessPosition kingPosition, boolean isKingside) {
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
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        //get start position
        ChessPiece piece = board.getPiece(move.getStartPosition());
        //check if piece exists or belongs to current team
        if (piece == null || piece.getTeamColor() != currentTurn){
            throw new InvalidMoveException("no valid piece at this position or wrong team's turn");
        }
        //is move valid
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (!validMoves.contains(move)) {
            throw new InvalidMoveException("move not valid for this piece");
        }

        // castling logic for the king
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            int startCol = move.getStartPosition().getColumn();
            int endCol = move.getEndPosition().getColumn();

            // check if the move is a castling move (king moves two spaces)
            if (Math.abs(endCol - startCol) == 2) {
                boolean isKingside = endCol > startCol;

                // validate if castling is allowed using canCastle
                if (!canCastle(move.getStartPosition(), isKingside)) {
                    throw new InvalidMoveException("Castling not allowed");
                }

                // Move the rook for castling
                ChessPosition rookStart = new ChessPosition(move.getStartPosition().getRow(), isKingside ? 8 : 1);
                ChessPosition rookEnd = new ChessPosition(move.getStartPosition().getRow(), isKingside ? 6 : 4);
                ChessPiece rook = board.getPiece(rookStart);
                board.addPiece(rookEnd, rook);
                board.addPiece(rookStart, null);
            }
        }
        //clone the board and simulate the move
        ChessBoard tempBoard = board.deepCopyBoard();
        tempBoard.addPiece(move.getEndPosition(), piece);
        tempBoard.addPiece(move.getStartPosition(), null); //clear start pos
        //check if the move leaves king in check
        if(isInCheck(currentTurn)){
            throw new InvalidMoveException("move leaves the king in check");
        }
        //move is validated, check for pawn promotion case
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            // check if the pawn has reached the last row
            if ((piece.getTeamColor() == TeamColor.WHITE && move.getEndPosition().getRow() == 8) ||
                    (piece.getTeamColor() == TeamColor.BLACK && move.getEndPosition().getRow() == 1)) {

                // if promotion exists promote the pawn
                ChessPiece.PieceType promotionType = move.getPromotionPiece();
                if (promotionType != null) {
                    // create the promoted piece
                    ChessPiece promotedPiece = new ChessPiece(piece.getTeamColor(), promotionType);

                    // place the promoted piece at the end position
                    board.addPiece(move.getEndPosition(), promotedPiece);
                    board.addPiece(move.getStartPosition(), null); // Clear start position
                } else {
                    // if no promotion type was specified, throw an error or promote automatically to Queen
                    ChessPiece promotedPiece = new ChessPiece(piece.getTeamColor(), ChessPiece.PieceType.QUEEN);
                    board.addPiece(move.getEndPosition(), promotedPiece);
                    board.addPiece(move.getStartPosition(), null);
                }
            } else {
                // if the pawn hasn't reached the last row, perform a regular pawn move
                board.addPiece(move.getEndPosition(), piece);
                board.addPiece(move.getStartPosition(), null); // Clear start position
            }
        } else {
            // for other piece types, perform a regular move
            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(move.getStartPosition(), null); // Clear start position
        }
        //update move flags for castling
        updateMoveFlags(piece, move.getStartPosition());
        //switch turns
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
        ChessPosition kingPosition = findKingPosition((teamColor));
        for (int row = 0; row < 8; row++){
            for (int col = 0; col < 8; col++){
            ChessPosition position = new ChessPosition(row, col);
            ChessPiece piece = board.getPiece(position);

            //check piece for check ability
            if (piece != null && piece.getTeamColor() != teamColor){
                Collection<ChessMove> opponentMoves = piece.pieceMoves(board, position, this);
                for (ChessMove move : opponentMoves){
                    if(move.getEndPosition().equals(kingPosition)){
                        return true; //king in check
                    }
                }
              }
            }
        }
        return false; //king not in check
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)){
            return false; //If not in check, it's not checkmate
        }

        //search for a valid move to resolve check
        for (int row = 0; row < 8; row++){
            for(int col = 0; col < 8; col++){
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if(piece != null && piece.getTeamColor() == teamColor){
                    Collection<ChessMove> validMoves = piece.pieceMoves(board, position, this);
                    for (ChessMove move : validMoves){
                        ChessBoard tempBoard = board.deepCopyBoard();
                        tempBoard.addPiece(move.getEndPosition(), piece);
                        tempBoard.addPiece(move.getStartPosition(), null);
                        if (!isInCheck(teamColor)){
                            return false;
                        }

                    }
                }
            }
        }
        return true;
    }

    //helper method to find king position

    private ChessPosition findKingPosition(TeamColor teamColor){
        for (int row = 0; row < 8; row++){
            for (int col = 0; col < 0; col++){
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

        for(int row  = 0; row < 8; row++){
            for (int col = 0; col < 8; col++){
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor){
                    Collection<ChessMove> validMoves = piece.pieceMoves(board, position, this);
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
