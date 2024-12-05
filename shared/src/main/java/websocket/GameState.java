package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessBoard;
import chess.ChessPosition;
import websocket.dto.GameStateDTO;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import chess.InvalidMoveException;


public class GameState {
  private final int gameID;
  private final ChessGame chessGame; // The chess game logic
  private final Map<String, String> players = new HashMap<>(); // authToken -> playerName

  private final Map<String, ChessGame.TeamColor> playerColors = new HashMap<>(); // authToken -> TeamColor

  private final Set<String> observers = new HashSet<>();
  private String winnerAuthToken;
  private boolean gameOver;

  public GameState(int gameID) {
    this.gameID = gameID;
    this.chessGame = new ChessGame(); // Initialize the chess game logic
    this.gameOver = false;
  }

  // Optional: Method to get the winner's username

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

    // Game is full, cannot add more players
    return false;
  }


  public synchronized boolean markResigned(String authToken) {
    if (!players.containsKey(authToken)) {
      return false; // Player not part of the game
    }
    this.gameOver = true;
    this.winnerAuthToken = getOpponentAuthToken(authToken);
    return true;
  }

  private String getOpponentAuthToken(String authToken) {
    for (String token : players.keySet()) {
      if (!token.equals(authToken)) {
        return token;
      }
    }
    return null; // No opponent found (e.g., observer scenario)
  }

  public String getWinnerUsername() {
    if (winnerAuthToken != null) {
      return players.get(winnerAuthToken);
    }
    return null;
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
      System.out.println("Move applied. Updated board: " + chessGame.getBoard().toString());
      // Check for game-ending conditions
      ChessGame.TeamColor opponentColor = chessGame.getOpponentColor(playerColor);
      if (chessGame.isInCheckmate(opponentColor)) {
        System.out.println("Checkmate detected! Opponent: " + opponentColor);
        gameOver = true;
        winnerAuthToken = getOpponentAuthToken(authToken);
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
  @Override
  public String toString() {
    return "GameState{" +
            "gameID=" + gameID +
            ", players=" + players +
            ", observers=" + observers +
            ", gameOver=" + gameOver +
            '}';
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

  public GameStateDTO toDTO() {
    GameStateDTO dto = new GameStateDTO();
    dto.setGameID(this.gameID);
    dto.setPlayers(this.players);

    // Convert TeamColor to String for serialization
    Map<String, String> colorMap = new HashMap<>();
    for (Map.Entry<String, ChessGame.TeamColor> entry : playerColors.entrySet()) {
      colorMap.put(entry.getKey(), entry.getValue().toString());
    }
    dto.setPlayerColors(colorMap);

    dto.setObservers(this.observers);
    dto.setGameOver(this.gameOver);

    // Add winner information if the game is over
    if (this.gameOver && this.winnerAuthToken != null) {
      String winnerUsername = this.players.get(this.winnerAuthToken);
      dto.setWinner(winnerUsername); // Ensure GameStateDTO has a 'winner' field
    }

    // Serialize the chessboard consistently from White's perspective
    Map<String, String> boardMap = new HashMap<>();
    ChessBoard board = this.chessGame.getBoard();
    for (int row = 1; row <= 8; row++) {
      for (int col = 1; col <= 8; col++) {
        ChessPosition pos = new ChessPosition(row, col); // row 1 is White's side
        ChessPiece piece = board.getPiece(pos);
        if (piece != null) {
          boardMap.put(pos.toString(), piece.toString());
        }
      }
    }
    dto.setBoard(boardMap);

    return dto;
  }

}