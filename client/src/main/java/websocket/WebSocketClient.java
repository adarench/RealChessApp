package websocket;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import websocket.commands.UserGameCommand;

import com.google.gson.Gson;
import websocket.messages.ServerMessage;
import chess.ChessMove;

/**
 * A Singleton WebSocket client using Java's built-in WebSocket API.
 */
public class WebSocketClient {
  private static WebSocketClient instance;
  private WebSocket webSocket;
  private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
  private final Gson gson = new Gson();

  // Private constructor to prevent instantiation
  public WebSocketClient() {}

  /**
   * Retrieves the singleton instance of WebSocketClient.
   *
   * @return The singleton WebSocketClient instance.
   */
  public static synchronized WebSocketClient getInstance() {
    if (instance == null) {
      instance = new WebSocketClient();
    }
    return instance;
  }

  /**
   * Connects to the WebSocket server at the given URI.
   *
   * @param serverUri The URI of the WebSocket server (e.g., "ws://localhost:8080/ws").
   * @throws Exception If the connection fails.
   */
  public void connect(String serverUri) throws Exception {
    if (webSocket != null) {
      System.out.println("WebSocket is already connected.");
      return;
    }
    HttpClient client = HttpClient.newHttpClient();
    webSocket = client.newWebSocketBuilder()
            .buildAsync(URI.create(serverUri), new WebSocketListener())
            .join();
    System.out.println("Connected to WebSocket server: " + serverUri);
  }

  /**
   * Sends a message to the WebSocket server.
   *
   * @param message The message to send (as a JSON string).
   */
  public void sendMessage(String message) {
    if (webSocket != null) {
      webSocket.sendText(message, true)
              .thenRun(() -> System.out.println("Message sent: " + message))
              .exceptionally(ex -> {
                System.err.println("Failed to send message: " + ex.getMessage());
                return null;
              });
    } else {
      System.err.println("WebSocket is not connected.");
    }
  }

  /**
   * Blocks until a message is received from the WebSocket server.
   *
   * @return The received message, or null if interrupted.
   */
  public String receiveMessage() {
    try {
      return messageQueue.take(); // Block until a message is available
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt(); // Restore interrupt status
      System.err.println("Error receiving message: " + e.getMessage());
      return null;
    }
  }

  /**
   * Disconnects from the WebSocket server.
   */
  public void disconnect() {
    if (webSocket != null) {
      webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Goodbye!")
              .thenRun(() -> System.out.println("WebSocket connection closed."))
              .exceptionally(ex -> {
                System.err.println("Failed to close WebSocket: " + ex.getMessage());
                return null;
              });
      webSocket = null;
    } else {
      System.err.println("WebSocket is not connected.");
    }
  }

  /**
   * Sends a MAKE_MOVE command via WebSocket.
   *
   * @param authToken The authentication token of the user.
   * @param gameID    The ID of the game.
   * @param move      The chess move to make.
   */
  public void sendMakeMoveCommand(String authToken, int gameID, ChessMove move) {
    UserGameCommand makeMoveCommand = new UserGameCommand();
    makeMoveCommand.setCommandType(UserGameCommand.CommandType.MAKE_MOVE);
    makeMoveCommand.setAuthToken(authToken);
    makeMoveCommand.setGameID(gameID);
    makeMoveCommand.setMove(move);
    sendMessage(gson.toJson(makeMoveCommand));
  }

  /**
   * Internal WebSocket listener to handle events and incoming messages.
   */
  private class WebSocketListener implements WebSocket.Listener {

    @Override
    public void onOpen(WebSocket webSocket) {
      System.out.println("WebSocket connection opened.");
      WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletableFuture<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
      String message = data.toString();
      System.out.println("Message received: " + message);

      // Enqueue the message for processing
      messageQueue.offer(message);
      System.out.println("Message queued for processing: " + message);

      // Request the next message
      webSocket.request(1);

      // Return a completed future to indicate we're ready for the next message
      return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<?> onClose(WebSocket webSocket, int statusCode, String reason) {
      System.out.println("WebSocket connection closed. Code: " + statusCode + ", Reason: " + reason);
      webSocket = null;
      return CompletableFuture.completedFuture(null);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
      System.err.println("WebSocket error: " + error.getMessage());
      error.printStackTrace();
    }
  }
}