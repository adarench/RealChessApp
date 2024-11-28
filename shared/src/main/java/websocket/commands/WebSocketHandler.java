package websocket.commands;

import websocket.messages.ServerMessage;
import websocket.messages.ServerMessage.ServerMessageType;

/**
 * Handles incoming WebSocket commands and processes them to manage gameplay logic.
 */
public class WebSocketHandler {

  public ServerMessage handleConnect(UserGameCommand command) {
    // Validate the auth token
    if (!isValidAuthToken(command.getAuthToken())) {
      return new ServerMessage(ServerMessageType.ERROR);
    }

    // Validate the game ID
    if (!gameExists(command.getGameID())) {
      return new ServerMessage(ServerMessageType.ERROR);
    }

    // Determine if the user is a player or an observer
    boolean isPlayer = addPlayerToGame(command.getAuthToken(), command.getGameID());
    if (!isPlayer) {
      addObserverToGame(command.getAuthToken(), command.getGameID());
    }

    ServerMessage loadGameMessage = new ServerMessage(ServerMessageType.LOAD_GAME);

    String userName = getUserNameFromAuthToken(command.getAuthToken());
    broadcastNotification(command.getGameID(), userName + " joined the game");

    return loadGameMessage;
  }

  private ServerMessage handleMakeMove(UserGameCommand command) {
    // Placeholder for making a move in the game
    if (!isValidAuthToken(command.getAuthToken()) || !gameExists(command.getGameID())) {
      return new ServerMessage(ServerMessageType.ERROR);
    }
    // Assume a valid move is made
    broadcastNotification(command.getGameID(), "A move was made");
    return new ServerMessage(ServerMessageType.NOTIFICATION);
  }

  private ServerMessage handleLeave(UserGameCommand command) {
    // Remove the user from the game (either player or observer)
    removeUserFromGame(command.getAuthToken(), command.getGameID());
    broadcastNotification(command.getGameID(), "A user left the game");
    return new ServerMessage(ServerMessageType.NOTIFICATION);
  }

  private ServerMessage handleResign(UserGameCommand command) {
    // Placeholder for resign logic
    broadcastNotification(command.getGameID(), "A player resigned");
    return new ServerMessage(ServerMessageType.NOTIFICATION);
  }

  // Placeholder methods to be implemented as needed

  private boolean isValidAuthToken(String authToken) {
    // Implement actual auth token validation logic (e.g., check database or cache)
    return true;
  }

  private boolean gameExists(int gameID) {

    return true;
  }

  private boolean addPlayerToGame(String authToken, int gameID) {
    // Add the user as a player if there's an open spot
    return true; // Placeholder
  }

  private void addObserverToGame(String authToken, int gameID) {
    // Add the user as an observer to the game
  }

  private String getUserNameFromAuthToken(String authToken) {
    // Fetch the username associated with the auth token
    return "User"; // Placeholder
  }

  private void broadcastNotification(int gameID, String message) {
    // Notify all other clients in the game about the event
  }
}
