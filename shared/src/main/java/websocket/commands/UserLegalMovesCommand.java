
package websocket.commands;

import chess.ChessPosition;

/**
 * Command representing a user's request to get legal moves for a selected piece.
 */
public class UserLegalMovesCommand {
  private String authToken;
  private int gameID;
  private ChessPosition startPosition;

  // Constructor
  public UserLegalMovesCommand(String authToken, int gameID, ChessPosition startPosition) {
    this.authToken = authToken;
    this.gameID = gameID;
    this.startPosition = startPosition;
  }

  // Getters
  public String getAuthToken() {
    return authToken;
  }

  public int getGameID() {
    return gameID;
  }

  public ChessPosition getStartPosition() {
    return startPosition;
  }

  // Setters (optional)
  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  public void setGameID(int gameID) {
    this.gameID = gameID;
  }

  public void setStartPosition(ChessPosition startPosition) {
    this.startPosition = startPosition;
  }
}
