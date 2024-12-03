package websocket.messages;

public class ServerMessage {
    private ServerMessageType serverMessageType;
    private String message;       // For notifications or general messages
    private String errorMessage;  // For error details
    private Object game;          // For game data
    private Object data;


    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION,
        LEGAL_MOVES
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

    // equals and hashCode methods (omitted for brevity)
}