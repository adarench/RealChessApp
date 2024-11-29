package server;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.commands.WebSocketHandler;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages WebSocket connections, routes incoming messages to the handler, and sends responses to clients.
 */
@WebSocket
public class WebSocketServer {

  private final Gson gson = new Gson();
  private final WebSocketHandler handler;
  private final ConcurrentHashMap<Session, String> sessionToAuthToken = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Session> authTokenToSession = new ConcurrentHashMap<>();

  public WebSocketServer() {
    this.handler = new WebSocketHandler(this);
  }

  @OnWebSocketConnect
  public void onConnect(Session session) {
    System.out.println("New connection: " + session);
  }

  @OnWebSocketMessage
  public void onMessage(Session session, String message) {
    System.out.println("Received message: " + message);

    try {
      UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
      ServerMessage response = handler.handleCommand(command, session);
      if (response != null) {
        sendMessage(session, gson.toJson(response));
      }
    } catch (Exception e) {
      e.printStackTrace();
      sendMessage(session, gson.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Invalid message format")));
    }
  }

  @OnWebSocketClose
  public void onClose(Session session, int statusCode, String reason) {
    System.out.println("Connection closed: " + session + ", reason: " + reason);
    String authToken = sessionToAuthToken.remove(session);
    if (authToken != null) {
      authTokenToSession.remove(authToken);
      handler.removeUserFromAllGames(authToken);
    }
  }

  @OnWebSocketError
  public void onError(Session session, Throwable error) {
    System.err.println("WebSocket Error for session " + session + ": " + error.getMessage());
    error.printStackTrace();
  }

  public void sendMessage(Session session, String message) {
    try {
      if (session != null && session.isOpen()) {
        session.getRemote().sendString(message);
      } else {
        System.err.println("Session is closed or null. Cannot send message: " + message);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void broadcastNotification(int gameID, String message, String excludeAuthToken) {
    Set<String> recipients = handler.getRecipientsForGame(gameID);
    if (excludeAuthToken != null) {
      recipients.remove(excludeAuthToken);
    }
    ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
    for (String authToken : recipients) {
      Session recipientSession = authTokenToSession.get(authToken);
      if (recipientSession != null && recipientSession.isOpen()) {
        sendMessage(recipientSession, gson.toJson(notification));
      }
    }
  }


  public void addAuthTokenSessionMapping(String authToken, Session session) {
    sessionToAuthToken.put(session, authToken);
    authTokenToSession.put(authToken, session);
  }

  public void removeAuthTokenSessionMapping(String authToken) {
    Session session = authTokenToSession.remove(authToken);
    if (session != null) {
      sessionToAuthToken.remove(session);
    }
  }
  public Session getSessionByAuthToken(String authToken) {
    return authTokenToSession.get(authToken);
  }

  public void sendMessageToAuthToken(String authToken, String message) {
    Session session = authTokenToSession.get(authToken);
    if (session != null && session.isOpen()) {
      sendMessage(session, message);
    }
  }


}
