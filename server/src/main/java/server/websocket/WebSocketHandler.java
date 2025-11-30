package server.websocket;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.GameData;
import websocket.commands.MakeMoveCommand;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

@WebSocket
public class WebSocketHandler {


    // Manages all active WebSocket connections organized by game
    private final ConnectionManager connections = new ConnectionManager();

    // Data access objects for database operations
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    // Constructor to inject DAOs
    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

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
            case MAKE_MOVE -> {
                MakeMoveCommand moveCommand = new Gson().fromJson(message, MakeMoveCommand.class);
                handleMakeMove(moveCommand, session);
            }
            case LEAVE -> handleLeave(command.getAuthToken(), command.getGameID());
            case RESIGN -> handleResign(command.getAuthToken(), command.getGameID());
        }
    }
    // Handle CONNECT command - user joining a game
    private void handleConnect(String authToken, Integer gameID, Session session) throws Exception {
        // Step 1: Verify authToken and get username
        var authData = authDAO.getAuth(authToken);
        if (authData == null) {
            connections.sendToUser(gameID, "unknown", new ErrorMessage("Error: Invalid auth token"));
            return;
        }
        String username = authData.username();

        // Step 2: Get the game from database
        var game = gameDAO.getGame(gameID);
        if (game == null) {
            connections.sendToUser(gameID, username, new ErrorMessage("Error: Game not found"));
            return;
        }

        // Step 3: Add this connection to the ConnectionManager
        connections.add(gameID, username, session);

        // Step 4: Send LOAD_GAME to this user so they can see the board
        connections.sendToUser(gameID, username, new LoadGameMessage(game.game()));

        // Step 5: Send NOTIFICATION to everyone else that this user joined
        String role = determineRole(game, username);
        connections.broadcast(gameID, username, new NotificationMessage(username + " joined as " + role));
    }

    // Determine if user is playing WHITE, BLACK, or is an OBSERVER
    private String determineRole(model.GameData game, String username) {
        if (username.equals(game.whiteUsername())) {
            return "WHITE";
        } else if (username.equals(game.blackUsername())) {
            return "BLACK";
        } else {
            return "OBSERVER";
        }
    }

    // Handle MAKE_MOVE command - user making a move
    private void handleMakeMove(MakeMoveCommand command, Session session) throws Exception {
        // Step 1: Verify authToken and get username
        var authData = authDAO.getAuth(command.getAuthToken());
        if (authData == null) {
            connections.sendToUser(command.getGameID(), "unknown", new ErrorMessage("Error: Invalid auth token"));
            return;
        }
        String username = authData.username();

        // Step 2: Get the game from database
        var gameData = gameDAO.getGame(command.getGameID());
        if (gameData == null) {
            connections.sendToUser(command.getGameID(), username, new ErrorMessage("Error: Game not found"));
            return;
        }

        var game = gameData.game();

        // Step 3: Verify it's this player's turn
        if (!isPlayersTurn(gameData, username, game)) {
            connections.sendToUser(command.getGameID(), username, new ErrorMessage("Error: Not your turn"));
            return;
        }

        // Step 4: Make the move
        try {
            game.makeMove(command.getMove());
        } catch (Exception e) {
            connections.sendToUser(command.getGameID(), username, new ErrorMessage("Error: Invalid move - " + e.getMessage()));
            return;
        }

        // Step 5: Update game in database
        var updatedGameData = new GameData(command.getGameID(), gameData.whiteUsername(),
                gameData.blackUsername(), gameData.gameName(), game);
        gameDAO.updateGame(updatedGameData);        // Step 6: Send LOAD_GAME to everyone
        connections.broadcastToAll(command.getGameID(), new LoadGameMessage(game));

        // Step 7: Send NOTIFICATION to everyone else about the move
        connections.broadcast(command.getGameID(), username,
                new NotificationMessage(username + " made a move: " + command.getMove()));

        // Step 8: Check for check/checkmate and notify
        checkGameStatus(command.getGameID(), game);
    }
    // Handle LEAVE command - user leaving a game
    private void handleLeave(String authToken, Integer gameID) throws Exception {
        // Step 1: Verify authToken and get username
        var authData = authDAO.getAuth(authToken);
        if (authData == null) {
            return; // Invalid auth, just ignore
        }
        String username = authData.username();

        // Step 2: Get the game from database
        var gameData = gameDAO.getGame(gameID);
        if (gameData != null) {
            // Step 3: If they're a player, remove them from the game
            String newWhite = gameData.whiteUsername();
            String newBlack = gameData.blackUsername();

            if (username.equals(gameData.whiteUsername())) {
                newWhite = null; // Remove white player
            } else if (username.equals(gameData.blackUsername())) {
                newBlack = null; // Remove black player
            }

            // Update game in database (only if a player left)
            if (!username.equals(gameData.whiteUsername()) || !username.equals(gameData.blackUsername())) {
                var updatedGameData = new GameData(gameID, newWhite, newBlack,
                        gameData.gameName(), gameData.game());
                gameDAO.updateGame(updatedGameData);
            }
        }

        // Step 4: Remove from ConnectionManager
        connections.remove(gameID, username);

        // Step 5: Send NOTIFICATION to everyone else
        connections.broadcast(gameID, username, new NotificationMessage(username + " left the game"));
    }

    // Handle RESIGN command - user resigning from a game
    private void handleResign(String authToken, Integer gameID) throws Exception {
        // TODO: Mark game as over, broadcast notification to all
        System.out.println("Handle RESIGN for game " + gameID);
    }

    // Check if it's this player's turn
    private boolean isPlayersTurn(GameData gameData, String username, chess.ChessGame game) {
        var currentTurn = game.getTeamTurn();
        if (currentTurn == chess.ChessGame.TeamColor.WHITE) {
            return username.equals(gameData.whiteUsername());
        } else {
            return username.equals(gameData.blackUsername());
        }
    }

    // Check game status and send notifications for check/checkmate/stalemate
    private void checkGameStatus(Integer gameID, chess.ChessGame game) throws Exception {
        var white = chess.ChessGame.TeamColor.WHITE;
        var black = chess.ChessGame.TeamColor.BLACK;

        if (game.isInCheckmate(white)) {
            connections.broadcastToAll(gameID, new NotificationMessage("WHITE is in checkmate! BLACK wins!"));
        } else if (game.isInCheckmate(black)) {
            connections.broadcastToAll(gameID, new NotificationMessage("BLACK is in checkmate! WHITE wins!"));
        } else if (game.isInCheck(white)) {
            connections.broadcastToAll(gameID, new NotificationMessage("WHITE is in check!"));
        } else if (game.isInCheck(black)) {
            connections.broadcastToAll(gameID, new NotificationMessage("BLACK is in check!"));
        } else if (game.isInStalemate(white) || game.isInStalemate(black)) {
            connections.broadcastToAll(gameID, new NotificationMessage("Game ended in stalemate!"));
        }
    }


}