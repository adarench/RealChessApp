package websocket.commands;

import org.eclipse.jetty.websocket.api.Session;
import server.WebSocketServer;
import websocket.GameState;
import websocket.messages.ServerMessage;
import websocket.messages.ServerMessage.ServerMessageType;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

// Import your DAOs and data models
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

public class WebSocketHandler {
  private final Map<Integer, GameState> gameStates = new ConcurrentHashMap<>(); // gameID -> GameState
  private final Map<String, Session> authTokenToSession = new ConcurrentHashMap<>(); // authToken -> WebSocket session
  private final WebSocketServer server;

  // Add DAO references
  private final AuthDAO authDAO;
  private final GameDAO gameDAO;

  public WebSocketHandler(WebSocketServer server) {
    this.server = server;
    this.authDAO = new AuthDAO(); // Instantiate the DAO
    this.gameDAO = new GameDAO(); // Instantiate the DAO
  }

  public ServerMessage handleCommand(UserGameCommand command, Session session) {
    switch (command.getCommandType()) {
      case CONNECT:
        return handleConnect(command, session);
      // case MAKE_MOVE:
      //     return handleMakeMove(command);
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

    // Validate the authToken using AuthDAO
    String userName;
    try {
      AuthData authData = authDAO.getAuth(authToken);
      if (authData == null) {
        return new ServerMessage(ServerMessageType.ERROR, "Invalid auth token");
      }
      userName = authData.username();
    } catch (DataAccessException e) {
      e.printStackTrace();
      return new ServerMessage(ServerMessageType.ERROR, "Server error during authentication");
    }

    // Ensure the session is mapped to the authToken
    server.addAuthTokenSessionMapping(authToken, session);

    // Check if the game exists using GameDAO
    GameData gameData;
    try {
      gameData = gameDAO.getGame(gameID);
      if (gameData == null) {
        return new ServerMessage(ServerMessageType.ERROR, "Game not found");
      }
    } catch (DataAccessException e) {
      e.printStackTrace();
      return new ServerMessage(ServerMessageType.ERROR, "Server error during game retrieval");
    }

    // Synchronize gameStates map
    gameStates.computeIfAbsent(gameID, id -> new GameState(id));

    GameState gameState = gameStates.get(gameID);

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

  private ServerMessage handleLeave(UserGameCommand command) {
    int gameID = command.getGameID();
    String authToken = command.getAuthToken();

    // Validate the authToken using AuthDAO
    String userName;
    try {
      AuthData authData = authDAO.getAuth(authToken);
      if (authData == null) {
        return new ServerMessage(ServerMessageType.ERROR, "Invalid auth token");
      }
      userName = authData.username();
    } catch (DataAccessException e) {
      e.printStackTrace();
      return new ServerMessage(ServerMessageType.ERROR, "Server error during authentication");
    }

    // Check if the game exists
    if (!gameStates.containsKey(gameID)) {
      return new ServerMessage(ServerMessageType.ERROR, "Game not found");
    }

    GameState gameState = gameStates.get(gameID);

    // Remove the user from the game
    boolean removed = gameState.removePlayer(authToken) || gameState.removeObserver(authToken);

    if (removed) {

      gameState.removePlayer(authToken);

      // If no players are left, remove the GameState
      if (gameState.getPlayers().isEmpty()) {
        gameStates.remove(gameID);
      }
      // Notify others in the game
      String notificationMessage = userName + " has left the game.";
      server.broadcastNotification(gameID, notificationMessage, authToken);

      return null;
    } else {
      return new ServerMessage(ServerMessageType.ERROR, "You are not part of this game.");
    }
  }

  private ServerMessage handleResign(UserGameCommand command) {
    int gameID = command.getGameID();
    String authToken = command.getAuthToken();

    // Validate the authToken using AuthDAO
    String userName;
    try {
      AuthData authData = authDAO.getAuth(authToken);
      if (authData == null) {
        return new ServerMessage(ServerMessageType.ERROR, "Invalid auth token");
      }
      userName = authData.username();
    } catch (DataAccessException e) {
      e.printStackTrace();
      return new ServerMessage(ServerMessageType.ERROR, "Server error during authentication");
    }

    // Check if the game exists
    if (!gameStates.containsKey(gameID)) {
      return new ServerMessage(ServerMessageType.ERROR, "Game not found");
    }

    GameState gameState = gameStates.get(gameID);

    // Resign the player
    boolean resigned = gameState.removePlayer(authToken);

    if (resigned) {
      // Mark the game as over if only one player remains
      if (gameState.getPlayers().size() <= 1) {
        gameState.setGameOver(true);
      }

      // Notify others in the game
      String notificationMessage = userName + " has resigned.";
      server.broadcastNotification(gameID, notificationMessage, authToken);
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
