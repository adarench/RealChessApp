package websocket.dto;

import websocket.GameState;

import java.util.Map;
import java.util.Set;

public class GameStateDTO {
  private int gameID;
  private Map<String, String> players; // authToken -> playerName
  private Map<String, String> playerColors; // authToken -> TeamColor as String
  private Set<String> observers;
  private boolean gameOver;
  private Map<String, String> board; // e.g., "e2" -> "Pawn"

  // Constructors
  public GameStateDTO() {}

  // Getters and Setters
  public int getGameID() {
    return gameID;
  }

  public void setGameID(int gameID) {
    this.gameID = gameID;
  }

  public Map<String, String> getPlayers() {
    return players;
  }

  public void setPlayers(Map<String, String> players) {
    this.players = players;
  }

  public Map<String, String> getPlayerColors() {
    return playerColors;
  }

  public void setPlayerColors(Map<String, String> playerColors) {
    this.playerColors = playerColors;
  }

  public Set<String> getObservers() {
    return observers;
  }

  public void setObservers(Set<String> observers) {
    this.observers = observers;
  }

  public boolean isGameOver() {
    return gameOver;
  }

  public void setGameOver(boolean gameOver) {
    this.gameOver = gameOver;
  }

  public Map<String, String> getBoard() {
    return board;
  }

  public void setBoard(Map<String, String> board) {
    this.board = board;
  }

  @Override
  public String toString() {
    return "GameStateDTO{" +
            "gameID=" + gameID +
            ", players=" + players +
            ", playerColors=" + playerColors +
            ", observers=" + observers +
            ", gameOver=" + gameOver +
            ", board=" + board +
            '}';
  }
}
