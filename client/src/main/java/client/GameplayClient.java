package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
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
        System.out.println();          // optional blank line before board
        redrawBoard();
        System.out.print("[GAMEPLAY] >>> ");   // print prompt AFTER board
    }

    private void handleError(ErrorMessage message) {
        // FIX #1: Remove "Error: " prefix since server message already includes it
        System.out.println(message.getErrorMessage());
        System.out.print("[GAMEPLAY] >>> ");   // prompt after error
    }

    private void handleNotification(NotificationMessage message) {
        System.out.println(message.getMessage());
        System.out.print("[GAMEPLAY] >>> ");   // prompt after notification
    }

    private void redrawBoard() {
        if (playerColor == ChessGame.TeamColor.BLACK) {
            BoardDrawer.drawBlackBoard(currentGame, null, null);
        } else {
            BoardDrawer.drawWhiteBoard(currentGame, null, null);
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
  move <from> <to> [promotion] - Make a move (e.g., move e2 e4, move e7 e8 q)
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
            return "Error: move requires <from> <to> [promotion]";
        }

        // Parse positions (e.g., "e2" -> ChessPosition)
        ChessPosition start = parsePosition(params[0]);
        ChessPosition end = parsePosition(params[1]);

        if (start == null || end == null) {
            return "Error: Invalid position format. Use format like 'e2'";
        }

        // FIX #2: Handle promotion piece if provided
        ChessPiece.PieceType promotionPiece = null;
        if (params.length >= 3) {
            promotionPiece = parsePromotionPiece(params[2]);
            if (promotionPiece == null) {
                return "Error: Invalid promotion piece. Use q, r, b, or n";
            }
        }

        ChessMove move = new ChessMove(start, end, promotionPiece);
        ws.makeMove(authToken, gameID, move);

        return null;
    }

    private String resign() throws Exception {
        // FIX #3: Add confirmation prompt before resigning
        System.out.print("Are you sure you want to resign? (yes/no): ");
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        String response = scanner.nextLine().trim().toLowerCase();

        if (response.equals("yes") || response.equals("y")) {
            ws.resign(authToken, gameID);
            return "Resigned from game.";
        } else {
            return "Resign cancelled.";
        }
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

        // Draw the board with highlights
        System.out.println();
        if (playerColor == ChessGame.TeamColor.BLACK) {
            BoardDrawer.drawBlackBoard(currentGame, pos, validMoves);
        } else {
            BoardDrawer.drawWhiteBoard(currentGame, pos, validMoves);
        }

        return ""; // Return empty string since we already drew the board

    }

    // Helper: Parse "e2" -> ChessPosition(2, 5)
    private ChessPosition parsePosition(String pos) {
        if (pos.length() != 2) {
            return null;
        }

        char col = pos.charAt(0);
        char row = pos.charAt(1);

        if (col < 'a' || col > 'h' || row < '1' || row > '8') {
            return null;
        }

        int colNum = col - 'a' + 1;
        int rowNum = row - '0';

        return new ChessPosition(rowNum, colNum);
    }

    // Helper: Parse promotion piece letter to PieceType
    private ChessPiece.PieceType parsePromotionPiece(String piece) {
        return switch (piece.toLowerCase()) {
            case "q", "queen" -> ChessPiece.PieceType.QUEEN;
            case "r", "rook" -> ChessPiece.PieceType.ROOK;
            case "b", "bishop" -> ChessPiece.PieceType.BISHOP;
            case "n", "knight" -> ChessPiece.PieceType.KNIGHT;
            default -> null;
        };
    }

    // Helper: ChessPosition -> "e2"
    private String positionToString(ChessPosition pos) {
        char col = (char) ('a' + pos.getColumn() - 1);
        char row = (char) ('0' + pos.getRow());
        return "" + col + row;
    }
}