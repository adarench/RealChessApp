package websocket;

import websocket.dto.GameStateDTO;
import com.google.gson.Gson;
import websocket.messages.ServerMessage;
import ui.Main;
public class WebSocketMessageHandler {
  private static final Gson gson = new Gson();

  /**
   * Handles incoming messages from the WebSocket.
   *
   * @param message The JSON message received from the server.
   */
  public static void handleMessage(String message) {
    System.out.println("Processing message: " + message);

    // Parse the message
    ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);

    // Handle the message based on its type
    switch (serverMessage.getServerMessageType()) {
      case LOAD_GAME:
        // Deserialize to GameStateDTO
        GameStateDTO updatedState = gson.fromJson(gson.toJson(serverMessage.getGame()), GameStateDTO.class);
        System.out.println("Received GameStateDTO: " + gson.toJson(updatedState));
        Main.updateGameState(updatedState); // Update the game state in Main
        Main.drawChessBoard(Main.isWhitePlayer(), updatedState); // Redraw the chessboard
        break;
      case NOTIFICATION:
        // Display notification
        System.out.println("Notification: " + serverMessage.getMessage());
        break;
      case ERROR:
        // Display error message
        System.err.println("Error: " + serverMessage.getErrorMessage());
        break;
      default:
        System.err.println("Unknown server message type: " + serverMessage.getServerMessageType());
    }
  }
}
