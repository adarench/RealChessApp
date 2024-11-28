package server;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.commands.WebSocketHandler;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages WebSocket connections, routes incoming messages to the handler, and sends responses to clients.
 */
@WebSocket
public class WebSocketServer {

  private final Gson gson = new Gson();
  private final WebSocketHandler handler = new WebSocketHandler();
  private static final ConcurrentHashMap<Session, String> activeSessions = new ConcurrentHashMap<>();

  /**
   * Called when a new client connects to the WebSocket server.
   *
   * @param session The session associated with the connected client.
   */
  @OnWebSocketConnect
  public void onConnect(Session session) {
    if (session == null) {
      System.err.println("Error: Null session received in onConnect");
      return;
    }

    System.out.println("New connection: " + session);
    activeSessions.put(session, null);

    // Send a welcome message or acknowledgement to the client
    sendMessage(session, gson.toJson(new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION)));
  }



  /**
   * Called when the server receives a message from a client.
   *
   * @param session The session that sent the message.
   * @param message The message content as a string.
   */
  @OnWebSocketMessage
  public void onMessage(Session session, String message) {
    System.out.println("Received message: " + message);

    try {
      UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

      ServerMessage response;
      switch (command.getCommandType()) {
        case CONNECT -> {
          response = handler.handleConnect(command);

          // If successful, associate the session with the authToken or game
          if (response.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
            activeSessions.put(session, command.getAuthToken());
          }
        }
        default -> response = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
      }

      sendMessage(session, gson.toJson(response));
    } catch (Exception e) {
      e.printStackTrace();
      sendMessage(session, gson.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR)));
    }
  }



  /**
   * Called when a client disconnects from the WebSocket server.
   *
   * @param session    The session associated with the disconnected client.
   * @param statusCode The WebSocket status code.
   * @param reason     The reason for disconnection.
   */
  @OnWebSocketClose
  public void onClose(Session session, int statusCode, String reason) {
    System.out.println("Connection closed: " + session + ", reason: " + reason);
    activeSessions.remove(session); // Remove the session from the tracking map
  }

  /**
   * Sends a message to a specific client.
   *
   * @param session The session of the client.
   * @param message The message to send.
   */
  private void sendMessage(Session session, String message) {
    try {
      if (session.isOpen()) {
        session.getRemote().sendString(message);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


}
