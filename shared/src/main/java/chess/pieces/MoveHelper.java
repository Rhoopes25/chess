package chess.pieces;

import chess.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MoveHelper {

    /**
     * Adds moves for pieces that move one square in given directions (King, Knight)
     */
    public static Collection<ChessMove> getSingleStepMoves(ChessBoard board,
                                                           ChessPosition position,
                                                           int[][] directions) {
        ChessPiece piece = board.getPiece(position);
        List<ChessMove> validMoves = new ArrayList<>();

        for (int[] direction : directions) {
            int newRow = position.getRow() + direction[0];
            int newCol = position.getColumn() + direction[1];

            if (isValidPosition(newRow, newCol)) {
                ChessPiece pieceAtTarget = board.getPiece(new ChessPosition(newRow, newCol));

                if (canMoveTo(piece, pieceAtTarget)) {
                    validMoves.add(new ChessMove(position, new ChessPosition(newRow, newCol), null));
                }
            }
        }

        return validMoves;
    }

    /**
     * Adds moves for pieces that slide multiple squares (Bishop, Rook, Queen)
     */
    public static Collection<ChessMove> getSlidingMoves(ChessBoard board,
                                                        ChessPosition position,
                                                        int[][] directions) {
        ChessPiece piece = board.getPiece(position);
        List<ChessMove> validMoves = new ArrayList<>();

        for (int[] direction : directions) {
            int rowDirection = direction[0];
            int colDirection = direction[1];

            int currentRow = position.getRow() + rowDirection;
            int currentCol = position.getColumn() + colDirection;

            // Keep moving in this direction until blocked
            while (isValidPosition(currentRow, currentCol)) {
                ChessPiece pieceHere = board.getPiece(new ChessPosition(currentRow, currentCol));

                if (pieceHere == null) {
                    // Empty square - can move here and continue
                    validMoves.add(new ChessMove(position, new ChessPosition(currentRow, currentCol), null));
                    currentRow += rowDirection;
                    currentCol += colDirection;
                } else if (piece.getTeamColor() != pieceHere.getTeamColor()) {
                    // Enemy piece - can capture and must stop
                    validMoves.add(new ChessMove(position, new ChessPosition(currentRow, currentCol), null));
                    break;
                } else {
                    // Same team - blocked, stop here
                    break;
                }
            }
        }

        return validMoves;
    }

    private static boolean isValidPosition(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    private static boolean canMoveTo(ChessPiece myPiece, ChessPiece targetPiece) {
        return targetPiece == null || myPiece.getTeamColor() != targetPiece.getTeamColor();
    }
}
