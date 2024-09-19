package chess;

import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor currentTurn;
    public ChessGame() {
        this.board = new ChessBoard();
        this.currentTurn = TeamColor.WHITE;
        this.board.resetBoard();
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
        return piece.pieceMoves(board, startPosition);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
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
        //make move
        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null); //clear start pos

        if(isInCheck(currentTurn)){
            throw new InvalidMoveException("move leaves the king in check");
        }
        //switch turns
        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    //iterate through pieces to see if cking is in check
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition((teamColor));
        for (int row = 0; row < 8; row++){
            for (int col = 0; col < 8; col++){
            ChessPosition position = new ChessPosition(row, col);
            ChessPiece piece = board.getPiece(position);

            //check piece for check ability
            if (piece != null && piece.getTeamColor() != teamColor){
                Collection<ChessMove> opponentMoves = piece.pieceMoves(board, position);
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
                    Collection<ChessMove> validMoves = piece.pieceMoves(board, position);
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
                    Collection<ChessMove> validMoves = piece.pieceMoves(board, position);
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
