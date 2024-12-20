package websocket;

import websocket.dto.GameStateDTO;
import com.google.gson.Gson;
import websocket.messages.ServerMessage;
import ui.Main;
public class WebSocketMessageHandler {
  private static final Gson GSON= new Gson();

  /**
   * Handles incoming messages from the WebSocket.
   *
   * @param message The JSON message received from the server.
   */
  public static void handleMessage(String message) {
    //System.out.println("Raw Message Received " + message);

    try {
      // Parse the message
      ServerMessage serverMessage = GSON.fromJson(message, ServerMessage.class);

      // Handle the message based on its type
      switch (serverMessage.getServerMessageType()) {
        case LOAD_GAME:
          //System.out.println("Parsed ServerMessageType: LOAD_GAME");
          // Deserialize to GameStateDTO
          GameStateDTO updatedState = GSON.fromJson(GSON.toJson(serverMessage.getGame()), GameStateDTO.class);
          //System.out.println("Deserialized GameStateDTO: " + gson.toJson(updatedState));
          //System.out.println("Board map in GameStateDTO: " + updatedState.getBoard());

          Main.updateGameState(updatedState); // Update the game state in Main
          break;
        case NOTIFICATION:
        String notification = serverMessage.getMessage();
        System.out.println("Notification: " + notification);

        if (notification.equalsIgnoreCase("You have resigned.") ||
                notification.equalsIgnoreCase("Opponent has resigned.") ||
                notification.equalsIgnoreCase("Checkmate.")) {
                Main.isInGame = false;
                Main.currentGameID = -1;
          Main.shouldTransitionToPostLogin.set(true);
        }
        break;
        case GAME_OVER:
          String gameOverMessage = serverMessage.getMessage();
          notifyUser("Game Over: " + gameOverMessage);
          displayNotification(gameOverMessage);
          Main.isInGame = false;
          Main.currentGameID = -1;
          Main.shouldTransitionToPostLogin.set(true); // Signal the main loop to transition
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

  private static void notifyUser(String message) {
    System.out.println("\n=== Notification ===");
    System.out.println(message);
    System.out.println("====================\n");
  }

  private static void displayNotification(String message) {
    // Implement UI notification logic
    System.out.println("Notification: " + message);
  }

}