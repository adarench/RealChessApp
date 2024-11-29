package websocket;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A WebSocket client using Java's built-in WebSocket API.
 */
public class WebSocketClient {
  private WebSocket webSocket;
  private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

  /**
   * Connects to the WebSocket server at the given URI.
   *
   * @param serverUri The URI of the WebSocket server (e.g., "ws://localhost:8080/ws").
   * @throws Exception If the connection fails.
   */
  public void connect(String serverUri) throws Exception {
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
    }
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
      WebSocketMessageHandler.handleMessage(message); // Delegate message handling
      messageQueue.offer(message); // Still queue the message for other use
      return CompletableFuture.completedFuture(null);
    }


    @Override
    public CompletableFuture<?> onClose(WebSocket webSocket, int statusCode, String reason) {
      System.out.println("WebSocket connection closed. Code: " + statusCode + ", Reason: " + reason);
      return CompletableFuture.completedFuture(null);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
      System.err.println("WebSocket error: " + error.getMessage());
    }
  }
}
