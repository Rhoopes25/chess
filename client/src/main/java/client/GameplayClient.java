package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;
import ui.BoardDrawer;
import websocket.messages.*;

public class GameplayClient implements NotificationHandler {

    private final WebSocketFacade ws;
    private final String authToken;
    private final int gameID;
    private final ChessGame.TeamColor playerColor; // null if observer
    private ChessGame currentGame;

    public GameplayClient(String serverUrl, String authToken, int gameID, ChessGame.TeamColor playerColor) throws Exception {
        this.authToken = authToken;
        this.gameID = gameID;
        this.playerColor = playerColor;
        this.currentGame = new ChessGame(); // Start with default board

        // Connect to WebSocket
        String wsUrl = serverUrl.replace("http", "ws") + "/ws";
        this.ws = new WebSocketFacade(wsUrl, this);

        // Send CONNECT command
        ws.connect(authToken, gameID);
    }

    // Handle messages from server
    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> handleLoadGame((LoadGameMessage) message);
            case ERROR -> handleError((ErrorMessage) message);
            case NOTIFICATION -> handleNotification((NotificationMessage) message);
        }
    }

    private void handleLoadGame(LoadGameMessage message) {
        this.currentGame = message.getGame();
        redrawBoard();
    }

    private void handleError(ErrorMessage message) {
        System.out.println("Error: " + message.getErrorMessage());
    }

    private void handleNotification(NotificationMessage message) {
        System.out.println(message.getMessage());
    }

    private void redrawBoard() {
        if (playerColor == ChessGame.TeamColor.BLACK) {
            BoardDrawer.drawBlackBoard(currentGame);
        } else {
            BoardDrawer.drawWhiteBoard(currentGame);
        }
    }

    // Evaluate user commands during gameplay
    public String eval(String input) {
        var tokens = input.split(" ");
        var command = tokens[0];
        var params = java.util.Arrays.copyOfRange(tokens, 1, tokens.length);

        try {
            return switch (command) {
                case "help" -> help();
                case "redraw" -> { redrawBoard(); yield "Board redrawn."; }
                case "leave" -> leave();
                case "move" -> makeMove(params);
                case "resign" -> resign();
                case "highlight" -> highlightMoves(params);
                default -> "Unknown command. Type 'help' for available commands.";
            };
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String help() {
        return """
Available commands:
  help - Show this help message
  redraw - Redraw the chess board
  leave - Leave the game
  move <from> <to> - Make a move (e.g., move e2 e4)
  resign - Resign from the game
  highlight <position> - Show legal moves for a piece (e.g., highlight e2)
""";
    }

    private String leave() throws Exception {
        ws.leave(authToken, gameID);
        return "LEFT"; // Signal to exit gameplay mode
    }

    private String makeMove(String[] params) throws Exception {
        if (params.length < 2) {
            return "Error: move requires <from> <to>";
        }

        // Parse positions (e.g., "e2" -> ChessPosition)
        ChessPosition start = parsePosition(params[0]);
        ChessPosition end = parsePosition(params[1]);

        if (start == null || end == null) {
            return "Error: Invalid position format. Use format like 'e2'";
        }

        ChessMove move = new ChessMove(start, end, null);
        ws.makeMove(authToken, gameID, move);

        return "Move sent.";
    }

    private String resign() throws Exception {
        // Confirm resignation
        System.out.print("Are you sure you want to resign? (yes/no): ");
        // For now, just resign - you can add confirmation later
        ws.resign(authToken, gameID);
        return "Resigned from game.";
    }

    private String highlightMoves(String[] params) {
        if (params.length < 1) {
            return "Error: highlight requires <position>";
        }

        ChessPosition pos = parsePosition(params[0]);
        if (pos == null) {
            return "Error: Invalid position format.";
        }

        var validMoves = currentGame.validMoves(pos);
        if (validMoves == null || validMoves.isEmpty()) {
            return "No legal moves for that piece.";
        }

        // Draw board with highlights - we'll need to update BoardDrawer for this
        // For now, just list the moves
        StringBuilder result = new StringBuilder("Legal moves: ");
        for (var move : validMoves) {
            result.append(positionToString(move.getEndPosition())).append(" ");
        }
        return result.toString();
    }

    // Helper: Parse "e2" -> ChessPosition(2, 5)
    private ChessPosition parsePosition(String pos) {
        if (pos.length() != 2) return null;

        char col = pos.charAt(0);
        char row = pos.charAt(1);

        if (col < 'a' || col > 'h' || row < '1' || row > '8') {
            return null;
        }

        int colNum = col - 'a' + 1;
        int rowNum = row - '0';

        return new ChessPosition(rowNum, colNum);
    }

    // Helper: ChessPosition -> "e2"
    private String positionToString(ChessPosition pos) {
        char col = (char) ('a' + pos.getColumn() - 1);
        char row = (char) ('0' + pos.getRow());
        return "" + col + row;
    }}