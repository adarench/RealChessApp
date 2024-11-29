package websocket;

import com.google.gson.Gson;
import websocket.messages.ServerMessage;

public class WebSocketMessageHandler {

  private static final Gson gson = new Gson();

  /**
   * Processes a message received from the WebSocket server.
   * @param message The incoming message as a JSON string.
   */
  public static void handleMessage(String message) {
    System.out.println("Processing message: " + message);

    // Parse the message
    ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);

    // Handle the message based on its type
    switch (serverMessage.getServerMessageType()) {
      case LOAD_GAME:
        // Handle game load logic
        System.out.println("Game loaded successfully.");
        break;
      case NOTIFICATION:
        // Handle notifications
        System.out.println("Notification: " + serverMessage.getMessage());
        break;
      case ERROR:
        // Handle errors
        System.err.println("Error: " + serverMessage.getErrorMessage());
        break;
      default:
        System.err.println("Unknown server message type: " + serverMessage.getServerMessageType());
    }
  }
}
