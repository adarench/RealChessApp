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
import chess.InvalidMoveException;


public class GameState {
  private final int gameID;
  private final ChessGame chessGame; // The chess game logic
  private final Map<String, String> players = new HashMap<>(); // authToken -> playerName

  private final Map<String, ChessGame.TeamColor> playerColors = new HashMap<>(); // authToken -> TeamColor

  private final Set<String> observers = new HashSet<>();
  private boolean gameOver;

  public GameState(int gameID) {
    this.gameID = gameID;
    this.chessGame = new ChessGame(); // Initialize the chess game logic
    this.gameOver = false;
  }


  public void setGameOver(boolean gameOver) {
    this.gameOver = gameOver;
  }


  public boolean addPlayer(String authToken, String playerName) {
    if (players.containsKey(authToken)) {
      // Player already in game
      return false;
    }
    if (players.size() < 2) {
      players.put(authToken, playerName);
      return true;
    }

    return true;
  }

  public Set<String> getAllParticipants() {
    Set<String> participants = new HashSet<>(players.keySet());
    participants.addAll(observers);
    return participants;
  }





  public void assignPlayerTeamColor(String authToken, ChessGame.TeamColor teamColor) {
    playerColors.put(authToken, teamColor);
  }



  public void addObserver(String authToken) {
    observers.add(authToken);
  }

  public boolean removePlayer(String authToken) {
    boolean removed = players.remove(authToken) != null;
    if (removed) {
      playerColors.remove(authToken); // Also remove the team color association
    }
    return removed;
  }


  public boolean removeObserver(String authToken) {
    return observers.remove(authToken);
  }

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
  public GameState.MoveResult makeMove(String authToken, ChessMove move) {
    if (gameOver) {
      return new MoveResult(false, "Game is already over");
    }

    // Ensure the player is part of the game
    if (!players.containsKey(authToken)) {
      return new MoveResult(false, "Player is not part of the game");
    }

    // Get the player's team color
    ChessGame.TeamColor playerColor = playerColors.get(authToken);
    if (playerColor == null) {
      return new MoveResult(false, "Player's team color is not assigned");
    }

    // Validate if it's the player's turn
    if (chessGame.getTeamTurn() != playerColor) {
      return new MoveResult(false, "It is not your turn");
    }

    try {
      // Make the move using ChessGame logic
      chessGame.makeMove(move);

      // Check for game-ending conditions
      ChessGame.TeamColor opponentColor = chessGame.getOpponentColor(playerColor);
      if (chessGame.isInCheckmate(opponentColor)) {
        gameOver = true;
        return new MoveResult(true, "Move successful. Checkmate!");
      } else if (chessGame.isInStalemate(opponentColor)) {
        gameOver = true;
        return new MoveResult(true, "Move successful. Stalemate!");
      }

      // If no game-ending conditions, return success
      return new MoveResult(true, "Move executed successfully");
    } catch (InvalidMoveException e) {
      return new MoveResult(false, e.getMessage());
    }
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