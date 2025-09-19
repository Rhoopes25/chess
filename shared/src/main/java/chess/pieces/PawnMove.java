package chess.pieces;

import chess.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PawnMove {

    public static Collection<ChessMove> getMoves(ChessBoard board, ChessPosition position) {
        ChessPiece piece = board.getPiece(position);
        //LIST TO HOLD ALL VALID MOVES INTO IT
        List<ChessMove> validMoves = new ArrayList<>();

        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            int forwardDirection = 1;

            int newRow =  position.getRow() + forwardDirection;
            int newCol =  position.getColumn();

            if (newRow >= 1 && newRow <= 8) {
                ChessPiece pieceHere = board.getPiece(new ChessPosition(newRow, newCol));

                if (pieceHere == null) {
                    //Add 1 square move
                    validMoves.add(new ChessMove(position, new ChessPosition(newRow, newCol), null));

                    //check if can move two
                    if (position.getRow() == 2) {
                        int twoSquareRow = position.getRow() + (2 * forwardDirection);
                        ChessPiece pieceTwoSquares = board.getPiece(new ChessPosition(twoSquareRow, newCol));
                        if (pieceTwoSquares == null) {
                            validMoves.add(new ChessMove(position, new ChessPosition(twoSquareRow, newCol), null));
                        }
                    }
                }
            }

            int[][] captureDirections = {{1, -1}, {1, 1}}; // forward-left, forward-right
            for (int[] captureDir : captureDirections) {
                int captureRow = position.getRow() + captureDir[0];
                int captureCol = position.getColumn() + captureDir[1];

                if (captureRow >= 1 && captureRow <= 8 && captureCol >= 1 && captureCol <= 8) {
                    ChessPiece captureTarget = board.getPiece(new ChessPosition(captureRow, captureCol));

                    if (captureTarget != null && captureTarget.getTeamColor() != piece.getTeamColor()) {
                        // Enemy piece - can capture!
                        validMoves.add(new ChessMove(position, new ChessPosition(captureRow, captureCol), null));
                    }
                }
            }


        } else {
            // Black pawn logic
            int forwardDirection = -1;
            int newRow =  position.getRow() + forwardDirection;
            int newCol =  position.getColumn();

            if (newRow >= 1 && newRow <= 8) {
                ChessPiece pieceHere = board.getPiece(new ChessPosition(newRow, newCol));


                if (pieceHere == null) {
                    //Add 1 square move
                    validMoves.add(new ChessMove(position, new ChessPosition(newRow, newCol), null));

                    //check if can move two
                    if (pieceHere == null && position.getRow() == 7) {
                        int twoSquareRow = position.getRow() + (2 * forwardDirection);
                        ChessPiece pieceTwoSquares = board.getPiece(new ChessPosition(twoSquareRow, newCol));
                        if (pieceTwoSquares == null) {
                            validMoves.add(new ChessMove(position, new ChessPosition(twoSquareRow, newCol), null));
                        }
                    }
                }

            }
            int[][] captureDirections = {{-1, -1}, {-1, 1}}; // forward-left, forward-right
            for (int[] captureDir : captureDirections) {
                int captureRow = position.getRow() + captureDir[0];
                int captureCol = position.getColumn() + captureDir[1];

                if (captureRow >= 1 && captureRow <= 8 && captureCol >= 1 && captureCol <= 8) {
                    ChessPiece captureTarget = board.getPiece(new ChessPosition(captureRow, captureCol));

                    if (captureTarget != null && captureTarget.getTeamColor() != piece.getTeamColor()) {
                        // Enemy piece - can capture!
                        validMoves.add(new ChessMove(position, new ChessPosition(captureRow, captureCol), null));
                    }
                }
            }
        }




        return validMoves;
    }
}