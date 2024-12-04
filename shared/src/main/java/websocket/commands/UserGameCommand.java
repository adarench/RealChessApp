package websocket.commands;

import java.util.Objects;
import chess.ChessMove;

/**
 * Represents a command a user can send the server over a WebSocket.
 *
 * Note: You can add to this class, but you should not alter the existing
 * methods unless necessary for compatibility.
 */
public class UserGameCommand {

    private CommandType commandType;
    private String authToken;
    private Integer gameID;
    private ChessMove move; // Only applicable for MAKE_MOVE commands
    private String pieceSquare;


    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN,
        HIGHLIGHT
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Integer getGameID() {
        return gameID;
    }

    /**
     * Returns the `ChessMove` associated with a MAKE_MOVE command.
     *
     * @return The `ChessMove`, or `null` if the command is not a MAKE_MOVE.
     */
    public ChessMove getMove() {
        return move;
    }

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void setGameID(Integer gameID) {
        this.gameID = gameID;
    }

    public void setMove(ChessMove move) {
        this.move = move;
    }

    public String getPieceSquare() {
        return pieceSquare;
    }

    public void setPieceSquare(String pieceSquare) {
        this.pieceSquare = pieceSquare;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserGameCommand)) {
            return false;
        }
        UserGameCommand that = (UserGameCommand) o;
        return getCommandType() == that.getCommandType()
                && Objects.equals(getAuthToken(), that.getAuthToken())
                && Objects.equals(getGameID(), that.getGameID())
                && Objects.equals(getMove(), that.getMove());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommandType(), getAuthToken(), getGameID(), getMove());
    }

    @Override
    public String toString() {
        return "UserGameCommand{" +
                "commandType=" + commandType +
                ", authToken='" + authToken + '\'' +
                ", gameID=" + gameID +
                ", move=" + move +
                '}';
    }
}