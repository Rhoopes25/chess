package ui;

import chess.*;

import static ui.EscapeSequences.*;

public class BoardDrawer {

    // Draw the board from white's perspective (a1 at bottom-left)
// Draw the board from white's perspective WITH HIGHLIGHTING
    public static void drawWhiteBoard(ChessGame game, ChessPosition startPos, java.util.Collection<ChessMove> legalMoves) {
        java.util.Set<ChessPosition> highlights = new java.util.HashSet<>();
        if (legalMoves != null) {
            for (ChessMove move : legalMoves) {
                highlights.add(move.getEndPosition());
            }
        }

        System.out.println("\n"); // Blank line before board

        // Draw top border with column letters
        drawBorder(true);

        // Draw rows 8 down to 1 (white perspective)
        for (int row = 8; row >= 1; row--) {
            drawRowWithHighlights(row, game, true, startPos, highlights);
        }

        // Draw bottom border with column letters
        drawBorder(true);

        System.out.println(); // Blank line after board
    }

    // Draw the board from black's perspective WITH HIGHLIGHTING
    public static void drawBlackBoard(ChessGame game, ChessPosition startPos, java.util.Collection<ChessMove> legalMoves) {
        java.util.Set<ChessPosition> highlights = new java.util.HashSet<>();
        if (legalMoves != null) {
            for (ChessMove move : legalMoves) {
                highlights.add(move.getEndPosition());
            }
        }

        System.out.println("\n"); // Blank line before board

        // Draw top border with column letters (reversed)
        drawBorder(false);

        // Draw rows 1 up to 8 (black perspective)
        for (int row = 1; row <= 8; row++) {
            drawRowWithHighlights(row, game, false, startPos, highlights);
        }

        // Draw bottom border with column letters (reversed)
        drawBorder(false);

        System.out.println(); // Blank line after board
    }

    // Draw a single row with highlighting support
    private static void drawRowWithHighlights(int row, ChessGame game, boolean whiteView,
                                              ChessPosition startPos, java.util.Set<ChessPosition> highlights) {
        // Print row number on left
        System.out.print(SET_BG_COLOR_LIGHT_GREY + " " + row + " " + RESET_BG_COLOR);

        // Determine which columns to iterate (a-h or h-a)
        if (whiteView) {
            for (int col = 1; col <= 8; col++) {
                drawSquareWithHighlight(row, col, game, startPos, highlights);
            }
        } else {
            for (int col = 8; col >= 1; col--) {
                drawSquareWithHighlight(row, col, game, startPos, highlights);
            }
        }

        // Print row number on right
        System.out.print(SET_BG_COLOR_LIGHT_GREY + " " + row + " " + RESET_BG_COLOR);
        System.out.println();
    }

    // Draw a single square with highlighting support
    private static void drawSquareWithHighlight(int row, int col, ChessGame game,
                                                ChessPosition startPos, java.util.Set<ChessPosition> highlights) {
        ChessPosition position = new ChessPosition(row, col);
        boolean isBlackSquare = (row + col) % 2 == 0;
        boolean isHighlighted = highlights.contains(position);
        boolean isStartPos = startPos != null && startPos.equals(position);

        // Set the background color based on highlighting
        if (isStartPos) {
            System.out.print(SET_BG_COLOR_GREEN); // Green for the selected piece
        } else if (isHighlighted) {
            System.out.print(SET_BG_COLOR_YELLOW); // Yellow for legal moves
        } else if (isBlackSquare) {
            System.out.print(SET_BG_COLOR_BLACK);
        } else {
            System.out.print(SET_BG_COLOR_MAGENTA);
        }

        // Get the piece at this position
        ChessPiece piece = game.getBoard().getPiece(position);

        // Print the piece or empty space
        if (piece == null) {
            System.out.print(EMPTY);
        } else {
            // Set text color based on piece team
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                System.out.print(SET_TEXT_COLOR_WHITE);
            } else {
                System.out.print(SET_TEXT_COLOR_LIGHT_GREY);
            }

            // Print the piece symbol
            System.out.print(getPieceSymbol(piece));
        }

        // Reset colors
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
    }
    // Draw the border with column letters (a-h or h-a)
    private static void drawBorder(boolean whiteView) {
        // Start with empty corner space
        System.out.print(SET_BG_COLOR_LIGHT_GREY + "   ");

        // Print column letters
        if (whiteView) {
            // a to h for white
            for (char col = 'a'; col <= 'h'; col++) {
                System.out.print(" " + col + " ");
            }
        } else {
            // h to a for black
            for (char col = 'h'; col >= 'a'; col--) {
                System.out.print(" " + col + " ");
            }
        }

        // End with empty corner space
        System.out.print("   " + RESET_BG_COLOR);
        System.out.println();
    }

    // Draw a single row of the board
    private static void drawRow(int row, ChessGame game, boolean whiteView) {
        // Print row number on left
        System.out.print(SET_BG_COLOR_LIGHT_GREY + " " + row + " " + RESET_BG_COLOR);

        // Determine which columns to iterate (a-h or h-a)
        if (whiteView) {
            for (int col = 1; col <= 8; col++) {
                drawSquare(row, col, game);
            }
        } else {
            for (int col = 8; col >= 1; col--) {
                drawSquare(row, col, game);
            }
        }

        // Print row number on right
        System.out.print(SET_BG_COLOR_LIGHT_GREY + " " + row + " " + RESET_BG_COLOR);
        System.out.println();
    }

    // Draw a single square with its piece
    private static void drawSquare(int row, int col, ChessGame game) {
        // Determine if this square should be pink or black
        // Chess rule: bottom-right square (h1) is light color
        boolean isBlackSquare = (row + col) % 2 == 0;

        // Set the background color
        if (isBlackSquare) {
            System.out.print(SET_BG_COLOR_BLACK);
        } else {
            System.out.print(SET_BG_COLOR_MAGENTA);
        }

        // Get the piece at this position
        ChessPosition position = new ChessPosition(row, col);
        ChessPiece piece = game.getBoard().getPiece(position);

        // Print the piece or empty space
        if (piece == null) {
            System.out.print(EMPTY);
        } else {
            // Set text color based on piece team
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                System.out.print(SET_TEXT_COLOR_WHITE);
            } else {
                System.out.print(SET_TEXT_COLOR_LIGHT_GREY);
            }

            // Print the piece symbol
            System.out.print(getPieceSymbol(piece));
        }

        // Reset colors
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    private static String getPieceSymbol(ChessPiece piece) {
        ChessPiece.PieceType type = piece.getPieceType();
        ChessGame.TeamColor color = piece.getTeamColor();

        // Return the appropriate symbol based on piece type and color
        if (color == ChessGame.TeamColor.WHITE) {
            return switch (type) {
                case KING -> WHITE_KING;
                case QUEEN -> WHITE_QUEEN;
                case BISHOP -> WHITE_BISHOP;
                case KNIGHT -> WHITE_KNIGHT;
                case ROOK -> WHITE_ROOK;
                case PAWN -> WHITE_PAWN;
            };
        } else {
            return switch (type) {
                case KING -> BLACK_KING;
                case QUEEN -> BLACK_QUEEN;
                case BISHOP -> BLACK_BISHOP;
                case KNIGHT -> BLACK_KNIGHT;
                case ROOK -> BLACK_ROOK;
                case PAWN -> BLACK_PAWN;
            };
        }
    }
}