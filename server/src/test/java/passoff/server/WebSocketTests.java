package passoff.server;

import chess.*;
import org.junit.jupiter.api.*;
import model.*;
import websocket.*;
import server.Server;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static websocket.messages.ServerMessage.ServerMessageType.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WebSocketTests {
    // This test class will be implemented in future phases
}