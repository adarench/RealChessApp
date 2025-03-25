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


public class WebSocketClient {
  private static WebSocketClient instance;
  private WebSocket webSocket;
  private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
  private final Gson gson = new Gson();

  // Private constructor to prevent instantiation
  public WebSocketClient() {}


  public static synchronized WebSocketClient getInstance() {
    if (instance == null) {
      instance = new WebSocketClient();
    }
    return instance;
  }


  public void connect(String serverUri) throws Exception {
    if (webSocket != null) {
      //System.out.println("WebSocket is already connected.");
      return;
    }
    HttpClient client = HttpClient.newHttpClient();
    webSocket = client.newWebSocketBuilder()
            .buildAsync(URI.create(serverUri), new WebSocketListener())
            .join();
    //System.out.println("Connected to WebSocket server: " + serverUri);
  }


  public void sendMessage(String message) {
    if (webSocket != null) {
      webSocket.sendText(message, true)
              .thenRun(() -> System.out.println("Message sent." ))
              .exceptionally(ex -> {
                System.err.println("Failed to send message: " + ex.getMessage());
                return null;
              });
    } else {
      System.err.println("WebSocket is not connected.");
    }
  }


  public String receiveMessage() {
    try {
      return messageQueue.take(); // Block until a message is available
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt(); // Restore interrupt status
      System.err.println("Error receiving message: " + e.getMessage());
      return null;
    }
  }


  public void sendMakeMoveCommand(String authToken, int gameID, ChessMove move) {
    UserGameCommand makeMoveCommand = new UserGameCommand();
    makeMoveCommand.setCommandType(UserGameCommand.CommandType.MAKE_MOVE);
    makeMoveCommand.setAuthToken(authToken);
    makeMoveCommand.setGameID(gameID);
    makeMoveCommand.setMove(move);
    sendMessage(gson.toJson(makeMoveCommand));
  }

  private class WebSocketListener implements WebSocket.Listener {

    @Override
    public void onOpen(WebSocket webSocket) {
      //System.out.println("WebSocket connection opened.");
      WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletableFuture<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
      String message = data.toString();
      //System.out.println("Message received: " + message);

      // Enqueue the message for processing
      messageQueue.offer(message);
      //System.out.println("Message queued for processing: " + message);

      // Request the next message
      webSocket.request(1);

      // Return a completed future to indicate we're ready for the next message
      return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<?> onClose(WebSocket webSocket, int statusCode, String reason) {
      //System.out.println("WebSocket connection closed. Code: " + statusCode + ", Reason: " + reason);
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