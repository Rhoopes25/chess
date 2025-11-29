package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

@WebSocket
public class WebSocketHandler {


    // Manages all active WebSocket connections organized by game
    private final ConnectionManager connections = new ConnectionManager();

    // We'll add methods here next
}