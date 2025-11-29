package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import com.google.gson.Gson;
import websocket.commands.UserGameCommand;

@WebSocket
public class WebSocketHandler {


    // Manages all active WebSocket connections organized by game
    private final ConnectionManager connections = new ConnectionManager();

    // Called when a client establishes a WebSocket connection
    // We don't add them to a game yet - waiting for their CONNECT command
    @OnWebSocketConnect
    public void onOpen(Session session) {
        System.out.println("WebSocket connection opened");
    }

    // Called when a WebSocket connection is closed
    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket connection closed");
    }

    // Called when there's an error with the WebSocket connection
    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
    }

    // Called when a message arrives from a client
// Parses the JSON and routes to the appropriate command handler
    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        // Parse the incoming JSON message
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);

        // Route to the appropriate handler based on command type
        switch (command.getCommandType()) {
            case CONNECT -> handleConnect(command.getAuthToken(), command.getGameID(), session);
            case MAKE_MOVE -> handleMakeMove(command, session);
            case LEAVE -> handleLeave(command.getAuthToken(), command.getGameID());
            case RESIGN -> handleResign(command.getAuthToken(), command.getGameID());
        }
    }
    // Handle CONNECT command - user joining a game
    private void handleConnect(String authToken, Integer gameID, Session session) throws Exception {
        // TODO: Verify authToken, get username, add to connections, send LOAD_GAME, broadcast notification
        System.out.println("Handle CONNECT for game " + gameID);
    }

    // Handle MAKE_MOVE command - user making a move
    private void handleMakeMove(UserGameCommand command, Session session) throws Exception {
        // TODO: Validate move, update game, broadcast LOAD_GAME, send notifications
        System.out.println("Handle MAKE_MOVE for game " + command.getGameID());
    }

    // Handle LEAVE command - user leaving a game
    private void handleLeave(String authToken, Integer gameID) throws Exception {
        // TODO: Remove from game, remove from connections, broadcast notification
        System.out.println("Handle LEAVE for game " + gameID);
    }

    // Handle RESIGN command - user resigning from a game
    private void handleResign(String authToken, Integer gameID) throws Exception {
        // TODO: Mark game as over, broadcast notification to all
        System.out.println("Handle RESIGN for game " + gameID);
    }


}