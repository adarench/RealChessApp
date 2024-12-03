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
    System.out.println("Raw Message Received " + message);

    try {
      // Parse the message
      ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);

      // Handle the message based on its type
      switch (serverMessage.getServerMessageType()) {
        case LOAD_GAME:
          System.out.println("Parsed ServerMessageType: LOAD_GAME");
          // Deserialize to GameStateDTO
          GameStateDTO updatedState = gson.fromJson(gson.toJson(serverMessage.getGame()), GameStateDTO.class);
          System.out.println("Deserialized GameStateDTO: " + gson.toJson(updatedState));
          System.out.println("Board map in GameStateDTO: " + updatedState.getBoard());

          Main.updateGameState(updatedState); // Update the game state in Main
          Main.drawChessBoard(Main.isWhitePlayer(), updatedState); // Redraw the chessboard
          break;
        case NOTIFICATION:
          // Display notification
          System.out.println("Notification: " + serverMessage.getMessage());
          break;
        case ERROR:
          System.out.println("Parsed ServerMessageType: ERROR");
          // Display error message
          System.err.println("Error: " + serverMessage.getErrorMessage());
          break;
        default:
          System.err.println("Unknown server message type: " + serverMessage.getServerMessageType());
      }
    } catch (Exception e) {
      System.err.println("Exception in handleMessage: " + e.getMessage());
      e.printStackTrace();
    }
  }

}