package websocket;

public class WebSocketMessageHandler {

  /**
   * Processes a message received from the WebSocket server.
   * @param message The incoming message as a JSON string.
   */
  public static void handleMessage(String message) {
    System.out.println("Processing message: " + message);

    // Parse and handle the message based on its type
    if (message.contains("update")) {
      // Handle game updates (real-time chessboard state)
      System.out.println("Game update received: " + message);
      // Example: You could parse and apply the update to the game state here
    } else if (message.contains("notification")) {
      // Handle server notifications
      System.out.println("Notification: " + message);
    } else {
      // Handle unknown or error messages
      System.err.println("Unknown message type: " + message);
    }
  }
}
