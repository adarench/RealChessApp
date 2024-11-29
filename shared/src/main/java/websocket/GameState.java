package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessBoard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

public class GameState {
  private final int gameID;
  private final ChessGame chessGame; // The chess game logic
  private final Map<String, String> players = new HashMap<>(); // authToken -> playerName
  private final Set<String> observers = new HashSet<>();
  private boolean gameOver;

  public GameState(int gameID) {
    this.gameID = gameID;
    this.chessGame = new ChessGame(); // Initialize the chess game logic
    this.gameOver = false;
  }

  public boolean addPlayer(String authToken, String playerName) {
    if (players.size() < 2) {
      players.put(authToken, playerName);
      return true;
    }
    return false; // Game is full
  }

  public void addObserver(String authToken) {
    observers.add(authToken);
  }

  public boolean removePlayer(String authToken) {
    return players.remove(authToken) != null;
  }

  public boolean removeObserver(String authToken) {
    return observers.remove(authToken);
  }

  /*public MoveResult makeMove(String authToken, ChessMove move) {
    if (gameOver) {
      return new MoveResult(false, "Game is already over");
    }

    // Ensure the player is part of the game
    if (!players.containsKey(authToken)) {
      return new MoveResult(false, "Player is not part of the game");
    }

    // Validate if it's the player's turn
    ChessGame.TeamColor playerColor = players.get(authToken);
    if (chessGame.getTeamTurn() != playerColor) {
      return new MoveResult(false, "It is not your turn");
    }

    // Get the piece at the starting position
    ChessPiece piece = chessGame.getBoard().getPiece(move.getStartPosition());
    if (piece == null) {
      return new MoveResult(false, "No piece at the starting position");
    }

    // Validate the piece belongs to the player
    if (piece.getTeamColor() != playerColor) {
      return new MoveResult(false, "The selected piece does not belong to you");
    }

    // Get all valid moves for the piece
    Collection<ChessMove> validMoves = piece.pieceMoves(chessGame.getBoard(), move.getStartPosition());
    boolean moveIsValid = validMoves.stream()
            .anyMatch(validMove -> validMove.getEndPosition().equals(move.getEndPosition()));

    if (!moveIsValid) {
      return new MoveResult(false, "The move is not valid for this piece");
    }

    // Execute the move
    ChessBoard board = chessGame.getBoard();
    board.addPiece(move.getEndPosition(), piece); // Place the piece in the new position
    board.addPiece(move.getStartPosition(), null); // Clear the starting position

    // Handle pawn promotion
    if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
      if ((piece.getTeamColor() == ChessGame.TeamColor.WHITE && move.getEndPosition().getRow() == 8) ||
              (piece.getTeamColor() == ChessGame.TeamColor.BLACK && move.getEndPosition().getRow() == 1)) {
        ChessPiece.PieceType promotionType = move.getPromotionPiece() != null
                ? move.getPromotionPiece()
                : ChessPiece.PieceType.QUEEN; // Default to queen if no type specified
        ChessPiece promotedPiece = new ChessPiece(piece.getTeamColor(), promotionType);
        board.addPiece(move.getEndPosition(), promotedPiece);
      }
    }

    // Switch turn
    chessGame.setTeamTurn(playerColor == ChessGame.TeamColor.WHITE
            ? ChessGame.TeamColor.BLACK
            : ChessGame.TeamColor.WHITE);

    // Check for game over conditions
    if (chessGame.isInCheckmate(ChessGame.TeamColor.WHITE) || chessGame.isInCheckmate(ChessGame.TeamColor.BLACK)) {
      gameOver = true;
      return new MoveResult(true, "Move successful. Game over: Checkmate!");
    } else if (chessGame.isInStalemate(ChessGame.TeamColor.WHITE) || chessGame.isInStalemate(ChessGame.TeamColor.BLACK)) {
      gameOver = true;
      return new MoveResult(true, "Move successful. Game over: Stalemate!");
    }

    return new MoveResult(true, "Move executed successfully");
  }*/



  public ChessGame getChessGame() {
    return chessGame;
  }

  public int getGameID() {
    return gameID;
  }

  public Map<String, String> getPlayers() {
    return players;
  }

  public Set<String> getObservers() {
    return observers;
  }

  public boolean isGameOver() {
    return gameOver;
  }

  public static class MoveResult {
    private final boolean successful;
    private final String message;

    public MoveResult(boolean successful, String message) {
      this.successful = successful;
      this.message = message;
    }

    public boolean isSuccessful() {
      return successful;
    }

    public String getMoveDescription() {
      return successful ? message : null;
    }

    public String getErrorMessage() {
      return !successful ? message : null;
    }
  }
}
