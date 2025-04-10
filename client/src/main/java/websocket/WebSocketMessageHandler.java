package websocket;

import websocket.dto.GameStateDTO;
import com.google.gson.Gson;
import websocket.messages.ServerMessage;
import ui.GameplayUI;

public class WebSocketMessageHandler {
  private static final Gson GSON = new Gson();

  public static void handleMessage(String message) {
    try {
      // Parse the message
      ServerMessage serverMessage = GSON.fromJson(message, ServerMessage.class);

      // Handle the message based on its type
      switch (serverMessage.getServerMessageType()) {
        case LOAD_GAME:
          // Deserialize to GameStateDTO
          GameStateDTO updatedState = GSON.fromJson(GSON.toJson(serverMessage.getGame()), GameStateDTO.class);
          GameplayUI.updateGameState(updatedState); // Update the game state
          break;
        case NOTIFICATION:
          String notification = serverMessage.getMessage();
          System.out.println("Notification: " + notification);

          if (notification.equalsIgnoreCase("You have resigned.") ||
                  notification.equalsIgnoreCase("Opponent has resigned.") ||
                  notification.equalsIgnoreCase("Checkmate.")) {
            GameplayUI.setIsInGame(false);
            GameplayUI.setCurrentGameID(-1);
            GameplayUI.getShouldTransitionToPostLogin().set(true);
          }
          break;
        case GAME_OVER:
          String gameOverMessage = serverMessage.getMessage();
          notifyUser("Game Over: " + gameOverMessage);
          displayNotification(gameOverMessage);
          GameplayUI.setIsInGame(false);
          GameplayUI.setCurrentGameID(-1);
          GameplayUI.getShouldTransitionToPostLogin().set(true); // Signal the main loop to transition
          break;
        case ERROR:
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