package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import com.google.gson.Gson;
import websocket.messages.ServerMessage;
import java.io.IOException;

public class ConnectionManager {

    // Maps gameID -> list of connections for that game
    private final ConcurrentHashMap<Integer, ArrayList<Connection>> connections = new ConcurrentHashMap<>();


    // Inner class to represent one connection
    private static class Connection {
        public String username;
        public Session session;

        Connection(String username, Session session) {
            this.username = username;
            this.session = session;
        }
    }
    public void add(Integer gameID, String username, Session session) {
        var connection = new Connection(username, session);
        connections.computeIfAbsent(gameID, k -> new ArrayList<>()).add(connection);
    }

    public void sendToUser(Integer gameID, String username, ServerMessage message) throws IOException {
        var gameConnections = connections.get(gameID);
        if (gameConnections != null) {
            for (var conn : gameConnections) {
                if (conn.username.equals(username) && conn.session.isOpen()) {
                    conn.session.getRemote().sendString(new Gson().toJson(message));
                }
            }
        }
    }
}