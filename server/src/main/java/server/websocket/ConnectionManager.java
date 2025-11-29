package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

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
}