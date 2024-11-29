package websocket.commands;

import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.messages.ServerMessage.ServerMessageType;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles incoming WebSocket commands and processes them to manage gameplay logic.
 */
public class WebSocketHandler {
  private final Map<Integer, Set<String>> gamePlayers = new ConcurrentHashMap<>();
  private final Map<Integer, Set<String>> gameObservers = new ConcurrentHashMap<>();
  private final Map<String, String> authTokenToUser = new ConcurrentHashMap<>();

  /**
   * Handle a CONNECT command.
   *
   * @param command The UserGameCommand containing connection details.
   * @return A ServerMessage indicating success or failure.
   */
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

    // Create a LOAD_GAME message
    ServerMessage loadGameMessage = new ServerMessage(ServerMessageType.LOAD_GAME);

    // Notify other users
    String userName = getUserNameFromAuthToken(command.getAuthToken());
    broadcastNotification(command.getGameID(), userName + " joined the game");

    return loadGameMessage;
  }

  /**
   * Handle other commands like MAKE_MOVE, LEAVE, RESIGN.
   *
   * @param command The UserGameCommand containing the command type.
   * @return A ServerMessage indicating the result.
   */
  public ServerMessage handleCommand(UserGameCommand command) {
    switch (command.getCommandType()) {
      case MAKE_MOVE:
        return handleMakeMove(command);
      case LEAVE:
        return handleLeave(command);
      case RESIGN:
        return handleResign(command);
      default:
        return new ServerMessage(ServerMessageType.ERROR);
    }
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

  // Helper methods

  private boolean isValidAuthToken(String authToken) {
    // Implement actual validation logic (e.g., check database or cache)
    return authTokenToUser.containsKey(authToken);
  }

  private boolean gameExists(int gameID) {
    return gamePlayers.containsKey(gameID) || gameObservers.containsKey(gameID);
  }

  private boolean addPlayerToGame(String authToken, int gameID) {
    // Add the user as a player if there's an open spot
    gamePlayers.putIfAbsent(gameID, ConcurrentHashMap.newKeySet());
    Set<String> players = gamePlayers.get(gameID);
    if (players.size() < 2) { // Assuming a 2-player game
      players.add(authToken);
      return true;
    }
    return false; // Game is full
  }

  private void addObserverToGame(String authToken, int gameID) {
    gameObservers.putIfAbsent(gameID, ConcurrentHashMap.newKeySet());
    gameObservers.get(gameID).add(authToken);
  }

  private void removeUserFromGame(String authToken, int gameID) {
    if (gamePlayers.containsKey(gameID)) {
      gamePlayers.get(gameID).remove(authToken);
    }
    if (gameObservers.containsKey(gameID)) {
      gameObservers.get(gameID).remove(authToken);
    }
  }

  private String getUserNameFromAuthToken(String authToken) {
    // Fetch the username associated with the auth token
    return authTokenToUser.getOrDefault(authToken, "Unknown User");
  }

  private void broadcastNotification(int gameID, String message) {
    // Notify all players and observers in the game
    Set<String> recipients = ConcurrentHashMap.newKeySet();
    if (gamePlayers.containsKey(gameID)) {
      recipients.addAll(gamePlayers.get(gameID));
    }
    if (gameObservers.containsKey(gameID)) {
      recipients.addAll(gameObservers.get(gameID));
    }

    for (String authToken : recipients) {
      // This method would need access to the actual WebSocket sessions
      System.out.println("Broadcast to " + authToken + ": " + message);
    }
  }
}
