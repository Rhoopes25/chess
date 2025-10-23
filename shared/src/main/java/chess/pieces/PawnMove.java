package chess.pieces;

import chess.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PawnMove {

    public static Collection<ChessMove> getMoves(ChessBoard board, ChessPosition position) {
        ChessPiece piece = board.getPiece(position);
        // LIST TO HOLD ALL VALID MOVES
        List<ChessMove> validMoves = new ArrayList<>();

        // Determine direction based on team color (WHITE goes up, BLACK goes down)
        int forwardDirection = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startingRow = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;

        // Add forward moves (1 or 2 squares)
        addForwardMoves(board, position, piece, validMoves, forwardDirection, startingRow);

        // Add diagonal capture moves
        addCaptureMoves(board, position, piece, validMoves, forwardDirection);

        return validMoves;
    }

    private static void addForwardMoves(ChessBoard board, ChessPosition position,
                                        ChessPiece piece, List<ChessMove> validMoves,
                                        int forwardDirection, int startingRow) {
        int newRow = position.getRow() + forwardDirection;
        int newCol = position.getColumn();

        // Check if new position is on the board
        if (!isValidPosition(newRow, newCol)) {
            return;
        }

        ChessPiece pieceAhead = board.getPiece(new ChessPosition(newRow, newCol));
        // Can't move forward if blocked
        if (pieceAhead != null) {
            return;
        }

        // Add 1 square move (with promotion if at end)
        addMovesWithPromotion(position, new ChessPosition(newRow, newCol), validMoves, newRow);

        // Check if can move two squares from starting position
        if (position.getRow() == startingRow) {
            int twoSquareRow = position.getRow() + (2 * forwardDirection);
            ChessPiece pieceTwoAhead = board.getPiece(new ChessPosition(twoSquareRow, newCol));

            if (pieceTwoAhead == null) {
                addMovesWithPromotion(position, new ChessPosition(twoSquareRow, newCol),
                        validMoves, twoSquareRow);
            }
        }
    }

    private static void addCaptureMoves(ChessBoard board, ChessPosition position,
                                        ChessPiece piece, List<ChessMove> validMoves,
                                        int forwardDirection) {
        // Diagonal capture directions: forward-left and forward-right
        int[][] captureDirections = {{forwardDirection, -1}, {forwardDirection, 1}};

        for (int[] captureDir : captureDirections) {
            int captureRow = position.getRow() + captureDir[0];
            int captureCol = position.getColumn() + captureDir[1];

            if (!isValidPosition(captureRow, captureCol)) {
                continue;
            }

            ChessPiece captureTarget = board.getPiece(new ChessPosition(captureRow, captureCol));

            // Can capture if there's an enemy piece
            if (isEnemyPiece(captureTarget, piece)) {
                addMovesWithPromotion(position, new ChessPosition(captureRow, captureCol),
                        validMoves, captureRow);
            }
        }
    }

    private static void addMovesWithPromotion(ChessPosition start, ChessPosition end,
                                              List<ChessMove> validMoves, int endRow) {
        // If reaching promotion row, add all 4 promotion options
        if (isPromotionRow(endRow)) {
            validMoves.add(new ChessMove(start, end, ChessPiece.PieceType.QUEEN));
            validMoves.add(new ChessMove(start, end, ChessPiece.PieceType.ROOK));
            validMoves.add(new ChessMove(start, end, ChessPiece.PieceType.BISHOP));
            validMoves.add(new ChessMove(start, end, ChessPiece.PieceType.KNIGHT));
        } else {
            // Regular move with no promotion
            validMoves.add(new ChessMove(start, end, null));
        }
    }

    private static boolean isValidPosition(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    private static boolean isPromotionRow(int row) {
        return row == 1 || row == 8;
    }

    private static boolean isEnemyPiece(ChessPiece target, ChessPiece myPiece) {
        return target != null && target.getTeamColor() != myPiece.getTeamColor();
    }
}