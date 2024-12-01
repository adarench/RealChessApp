package websocket;
import com.google.gson.Gson;
import ui.Main;
import websocket.messages.ServerMessage;

public class WebSocketMessageHandler {
  private static final Gson gson = new Gson();
  private static Main mainInstance; // Reference to Main instance

  public static void initialize(Main main) {
    mainInstance = main;
  }

  public static void handleMessage(String message) {
    System.out.println("Processing message: " + message);

    // Parse the message
    ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);

    // Handle the message based on its type
    switch (serverMessage.getServerMessageType()) {

      case LOAD_GAME:
        // Update local game state
        GameState updatedState = gson.fromJson(gson.toJson(serverMessage.getGame()), GameState.class);
        mainInstance.updateGameState(updatedState);
        // Redraw the chessboard
        mainInstance.drawChessBoard(mainInstance.isWhitePlayer(),updatedState);
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
