package websocket.messages;

public class ServerMessage {
    private ServerMessageType serverMessageType;
    private String message;       // For notifications or general messages
    private String errorMessage;  // For error details
    private Object game;          // For game data
    private Object data;

    private HighlightData highlightData;


    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION,
        HIGHLIGHT,
        GAME_OVER
    }

    // Constructor for general messages

    public void validateServerMessage(ServerMessageType type, Object data) {
        this.serverMessageType = type;
        this.data = data;
    }

    // Constructor with message content
    public ServerMessage(ServerMessageType type, String message) {
        this.serverMessageType = type;
        if (type == ServerMessageType.ERROR) {
            this.errorMessage = message;
        } else {
            this.message = message;
        }
    }

    // Constructor with game data
    public ServerMessage(ServerMessageType type, Object game) {
        this.serverMessageType = type;
        this.game = game;
    }

    // Constructor for GAME_OVER messages
    public ServerMessage(ServerMessageType type, String message, boolean isGameOver) {
        this.serverMessageType = type;
        if (type == ServerMessageType.ERROR) {
            this.errorMessage = message;
        } else {
            this.message = message;
        }
    }

    // Getter methods
    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    public String getMessage() {
        return this.message;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public Object getGame() {
        return this.game;
    }
    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public HighlightData getHighlightData() {
        return this.highlightData;
    }



    // equals and hashCode methods (omitted for brevity)
}