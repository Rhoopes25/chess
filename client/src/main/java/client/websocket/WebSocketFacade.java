package client.websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.*;
import websocket.messages.ServerMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.ErrorMessage;
import websocket.messages.NotificationMessage;

import jakarta.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class WebSocketFacade extends Endpoint {

    private Session session;
    private NotificationHandler notificationHandler;

    // Constructor - connects to the WebSocket server
    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws Exception {
        this.notificationHandler = notificationHandler;

        URI uri = new URI(url);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);

        // Set up message handler - simpler approach
        this.session.addMessageHandler(String.class, this::handleMessage);
    }

    // Called when connection opens
    @Override
    public void onOpen(Session session, EndpointConfig config) {
    }

    // Handle incoming messages from server
    private void handleMessage(String message) {
        try {
            var gson = new Gson();
            // First parse to get the type
            ServerMessage baseMessage = gson.fromJson(message, ServerMessage.class);

            // Then parse again based on the specific type to get the full data
            ServerMessage fullMessage = switch (baseMessage.getServerMessageType()) {
                case LOAD_GAME -> gson.fromJson(message, websocket.messages.LoadGameMessage.class);
                case ERROR -> gson.fromJson(message, websocket.messages.ErrorMessage.class);
                case NOTIFICATION -> gson.fromJson(message, websocket.messages.NotificationMessage.class);
            };

            notificationHandler.notify(fullMessage);
        } catch (Exception e) {
            System.err.println("Error handling message: " + e.getMessage());
        }
    }

    // Send CONNECT command to join a game
    public void connect(String authToken, int gameID) throws Exception {
        var command = new ConnectCommand(authToken, gameID);
        sendCommand(command);
    }

    // Send MAKE_MOVE command
    public void makeMove(String authToken, int gameID, ChessMove move) throws Exception {
        var command = new MakeMoveCommand(authToken, gameID, move);
        sendCommand(command);
    }

    // Send LEAVE command
    public void leave(String authToken, int gameID) throws Exception {
        var command = new LeaveCommand(authToken, gameID);
        sendCommand(command);
    }

    // Send RESIGN command
    public void resign(String authToken, int gameID) throws Exception {
        var command = new ResignCommand(authToken, gameID);
        sendCommand(command);
    }

    // Helper method to send any command as JSON
    private void sendCommand(Object command) throws Exception {
        String json = new Gson().toJson(command);
        this.session.getBasicRemote().sendText(json);
    }
}