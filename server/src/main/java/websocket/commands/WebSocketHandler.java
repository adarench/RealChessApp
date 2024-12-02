package websocket.commands;

import chess.InvalidMoveException;
import org.eclipse.jetty.websocket.api.Session;
import server.WebSocketServer;
import websocket.GameState;
import websocket.messages.ServerMessage;
import websocket.messages.ServerMessage.ServerMessageType;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import chess.ChessGame;

import websocket.dto.GameStateDTO;

// Import your DAOs and data models
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import chess.ChessMove;

public class WebSocketHandler {
  private Gson gson = new Gson();
  private static final Map<Integer, GameState> gameStates = new ConcurrentHashMap<>(); // gameID -> GameState
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
    System.out.println("Received command: " + command.getCommandType());
    switch (command.getCommandType()) {
      case CONNECT:
        System.out.println("Dispatching to handleConnect");
        return handleConnect(command, session);
      case MAKE_MOVE:
        System.out.println("Dispatching to handleMakeMove");
        return handleMakeMove(command);
      case LEAVE:
        System.out.println("Dispatching to handleLeave");
        return handleLeave(command);
      case RESIGN:
        System.out.println("Dispatching to handleResign");
        return handleResign(command);
      default:
        System.out.println("Unknown command type received");
        return new ServerMessage(ServerMessageType.ERROR, "Unknown command type");
    }
  }

  public ServerMessage handleConnect(UserGameCommand command, Session session) {
    int gameID = command.getGameID();
    String authToken = command.getAuthToken();
    System.out.println("Handling CONNECT for gameID: " + gameID + ", authToken: " + authToken);

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

    gameStates.computeIfAbsent(gameID, id -> new GameState(id));
    GameState gameState = gameStates.get(gameID);
    System.out.println("Initialized GameState for gameID: " + gameID);
    // Determine the player's team color based on GameData
    ChessGame.TeamColor teamColor = null;
    if (userName.equals(gameData.whiteUsername())) {
      teamColor = ChessGame.TeamColor.WHITE;
    } else if (userName.equals(gameData.blackUsername())) {
      teamColor = ChessGame.TeamColor.BLACK;
    }

    boolean addedAsPlayer;
    if (teamColor != null) {
      // Add the player as they are assigned in the game
      addedAsPlayer = gameState.addPlayer(authToken, userName);
      if (addedAsPlayer) {
        gameState.assignPlayerTeamColor(authToken, teamColor); // Ensure team color is set
      } else {
        // If adding the player failed, return an error
        return new ServerMessage(ServerMessageType.ERROR, "Failed to add player to the game");
      }
    } else {
      // Add the user as an observer if they are not assigned to a team
      gameState.addObserver(authToken);
    }
    System.out.println("Current players in game " + gameID + ": " + gameState.getPlayers());
    System.out.println("Current observers in game " + gameID + ": " + gameState.getObservers());


    // Convert GameState to GameStateDTO
    GameStateDTO dto = gameState.toDTO();

    // Build and return the full game state to the connecting user
    ServerMessage loadGameMessage = new ServerMessage(ServerMessageType.LOAD_GAME, dto);


    // Notify other users in the game about the new connection
    String notificationMessage = userName + " has joined the game.";
    server.broadcastNotification(gameID, notificationMessage, authToken);
    System.out.println("Broadcasted join notification for " + userName);
    System.out.println("Players in game: " + gameState.getPlayers());
    System.out.println("Observers in game: " + gameState.getObservers());

    return loadGameMessage;
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
      // Synchronize with the database
      try {
        GameData gameData = gameDAO.getGame(gameID);
        if (gameData != null) {
          String updatedWhite = gameData.whiteUsername();
          String updatedBlack = gameData.blackUsername();

          // Clear the corresponding spot
          if (userName.equals(gameData.whiteUsername())) {
            updatedWhite = null;
          } else if (userName.equals(gameData.blackUsername())) {
            updatedBlack = null;
          }

          gameDAO.updateGame(gameID, updatedWhite, updatedBlack);
        }
      } catch (DataAccessException e) {
        e.printStackTrace();
        return new ServerMessage(ServerMessageType.ERROR, "Failed to update game state in database.");
      }

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

    // Check if the game is already over
    if (gameState.isGameOver()) {
      return new ServerMessage(ServerMessageType.ERROR, "The game is already over. You cannot resign.");
    }

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
    System.out.println("Recipients for game " + gameID + ": " + recipients);

    recipients.addAll(gameState.getObservers());
    return recipients;
  }

  private ServerMessage handleMakeMove(UserGameCommand command) {

    int gameID = command.getGameID();
    String authToken = command.getAuthToken();
    ChessMove move = command.getMove();
    System.out.println("handleMakeMove called for gameID: " + gameID + ", authToken: " + authToken);



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

    GameState gameState = gameStates.get(gameID);
    if (gameState == null) {
      return new ServerMessage(ServerMessageType.ERROR, "Game not found");
    }
    System.out.println("Found GameState: " + gameState);

    // Attempt to make the move using GameState
    GameState.MoveResult moveResult = gameState.makeMove(authToken, move);
    System.out.println("MoveResult: " + moveResult.isSuccessful() + ", Message: " + moveResult.getErrorMessage());

    if (!moveResult.isSuccessful()) {
      // Return an error message to the client
      return new ServerMessage(ServerMessageType.ERROR, moveResult.getErrorMessage());
    }

    // Convert GameState to GameStateDTO
    GameStateDTO dto = gameState.toDTO();

    // Serialize and log the DTO for debugging
    String serializedDTO = gson.toJson(dto);
    System.out.println("Serialized GameStateDTO: " + serializedDTO);

    // Create a ServerMessage with LOAD_GAME type
    ServerMessage gameStateMessage = new ServerMessage(ServerMessageType.LOAD_GAME, dto);

    // Send the message to other players and observers
    Set<String> recipients = getRecipientsForGame(gameID);
    recipients.remove(authToken); // Exclude the moving player

    for (String recipientAuthToken : recipients) {
      Session recipientSession = server.getSessionByAuthToken(recipientAuthToken);
      if (recipientSession != null && recipientSession.isOpen()) {
        server.sendMessage(recipientSession, gson.toJson(gameStateMessage));
      }
    }
    // Send a NOTIFICATION message about the move
    String notificationMessage = "A move was made: " + move.getStartPosition() + " -> " + move.getEndPosition();
    for (String recipientAuthToken : recipients) {
      Session recipientSession = server.getSessionByAuthToken(recipientAuthToken);
      if (recipientSession != null && recipientSession.isOpen()) {
        ServerMessage notification = new ServerMessage(ServerMessageType.NOTIFICATION, notificationMessage);
        server.sendMessage(recipientSession, gson.toJson(notification));
      }
    }
    // Return the updated game state to the moving player
    System.out.println("MAKE_MOVE successful for gameID: " + gameID + ", move: " + move);
    return gameStateMessage;
  }


}
