package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

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


}