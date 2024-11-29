package websocket.commands;

import org.eclipse.jetty.websocket.api.Session;
import server.WebSocketServer;
import websocket.messages.ServerMessage;
import websocket.messages.ServerMessage.ServerMessageType;
import websocket.GameState;
import chess.ChessMove;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles WebSocket commands and manages game state interactions.
 */
public class WebSocketHandler {
  private final Map<Integer, GameState> gameStates = new ConcurrentHashMap<>(); // gameID -> GameState
  private final Map<String, String> authTokenToUser = new ConcurrentHashMap<>(); // authToken -> username
  private final Map<String, Session> authTokenToSession = new ConcurrentHashMap<>(); // authToken -> WebSocket session
  private final WebSocketServer server;

  public WebSocketHandler(WebSocketServer server) {
    this.server = server;
  }

  /**
   * Handles a WebSocket command based on its type.
   *
   * @param command The command to handle.
   * @param session The WebSocket session of the sender.
   * @return The ServerMessage to send back to the client.
   */
  public ServerMessage handleCommand(UserGameCommand command, Session session) {
    switch (command.getCommandType()) {
      case CONNECT:
        return handleConnect(command, session);
      /*case MAKE_MOVE:
        return handleMakeMove(command);*/
      case LEAVE:
        return handleLeave(command);
      case RESIGN:
        return handleResign(command);
      default:
        return new ServerMessage(ServerMessageType.ERROR, "Unknown command type");
    }
  }

  public ServerMessage handleConnect(UserGameCommand command, Session session) {
    int gameID = command.getGameID();
    String authToken = command.getAuthToken();


    // Ensure the session is mapped to the authToken
    server.addAuthTokenSessionMapping(authToken, session);

    // Automatically create the game if it doesn't exist
    gameStates.computeIfAbsent(gameID, id -> new GameState(id));

    GameState gameState = gameStates.get(gameID);

    // Assign a default username
    String userName = "Player";

    // Determine if the user should be added as a player or observer
    boolean addedAsPlayer = gameState.addPlayer(authToken, userName);

    // If unable to add as a player, add as an observer
    if (!addedAsPlayer) {
      gameState.addObserver(authToken);
    }

    // Build and return the full game state to the connecting user
    ServerMessage response = new ServerMessage(ServerMessageType.LOAD_GAME, gameState);

    // Notify other users in the game about the new connection
    String notificationMessage = userName + " has joined the game.";
    server.broadcastNotification(gameID, notificationMessage, authToken);

    return response;
  }



  /*private ServerMessage handleMakeMove(UserGameCommand command) {
    int gameID = command.getGameID();
    String authToken = command.getAuthToken();
    ChessMove move = command.getMove();

    // Check if the game exists
    if (!gameStates.containsKey(gameID)) {
      return new ServerMessage(ServerMessageType.ERROR, "Game not found");
    }

    GameState gameState = gameStates.get(gameID);

    // Make the move in the game state
    GameState.MoveResult moveResult = gameState.makeMove(authToken, move);

    if (moveResult.isSuccessful()) {
      // Broadcast the updated game state to all players and observers
      server.broadcastNotification(gameID, moveResult.getMoveDescription(), null);
      return new ServerMessage(ServerMessageType.NOTIFICATION, moveResult.getMoveDescription());
    } else {
      return new ServerMessage(ServerMessageType.ERROR, moveResult.getErrorMessage());
    }
  }*/

  private ServerMessage handleLeave(UserGameCommand command) {
    int gameID = command.getGameID();
    String authToken = command.getAuthToken();

    // Check if the game exists
    if (!gameStates.containsKey(gameID)) {
      return new ServerMessage(ServerMessageType.ERROR, "Game not found");
    }

    GameState gameState = gameStates.get(gameID);

    // Remove the user from the game
    boolean removed = gameState.removePlayer(authToken) || gameState.removeObserver(authToken);

    if (removed) {
      // Notify others in the game
      server.broadcastNotification(gameID, authTokenToUser.get(authToken) + " has left the game.", authToken);
      return new ServerMessage(ServerMessageType.NOTIFICATION, "You have left the game.");
    } else {
      return new ServerMessage(ServerMessageType.ERROR, "You are not part of this game.");
    }
  }

  private ServerMessage handleResign(UserGameCommand command) {
    int gameID = command.getGameID();
    String authToken = command.getAuthToken();

    // Check if the game exists
    if (!gameStates.containsKey(gameID)) {
      return new ServerMessage(ServerMessageType.ERROR, "Game not found");
    }

    GameState gameState = gameStates.get(gameID);

    // Resign the player
    boolean resigned = gameState.removePlayer(authToken);

    if (resigned) {
      // Mark the game as over if only one player remains
      if (gameState.getPlayers().size() == 1) {
        gameState.isGameOver();
      }

      // Notify others in the game
      server.broadcastNotification(gameID, authTokenToUser.get(authToken) + " has resigned.", authToken);
      return new ServerMessage(ServerMessageType.NOTIFICATION, "You have resigned.");
    } else {
      return new ServerMessage(ServerMessageType.ERROR, "You are not part of this game.");
    }
  }


  public void removeUserFromAllGames(String authToken) {
    gameStates.values().forEach(gameState -> {
      gameState.removePlayer(authToken);
      gameState.removeObserver(authToken);
    });
    authTokenToSession.remove(authToken);
  }

  public Set<String> getRecipientsForGame(int gameID) {
    if (!gameStates.containsKey(gameID)) {
      return Set.of(); // Return an empty set if the game doesn't exist
    }

    GameState gameState = gameStates.get(gameID);
    Set<String> recipients = new HashSet<>(gameState.getPlayers().keySet());
    recipients.addAll(gameState.getObservers());
    return recipients;
  }
}
