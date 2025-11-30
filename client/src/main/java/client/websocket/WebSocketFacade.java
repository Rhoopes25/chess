package client.websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.*;
import websocket.messages.ServerMessage;

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
        ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
        notificationHandler.notify(serverMessage);
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