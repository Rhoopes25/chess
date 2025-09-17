package chess.pieces;

import chess.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RookMove {

    public static Collection<ChessMove> getMoves(ChessBoard board, ChessPosition position) {
        ChessPiece piece = board.getPiece(position);

        //LIST TO HOLD ALL VALID MOVES INTO IT
        List<ChessMove> validMoves = new ArrayList<>();
        int[][] directions = {{1,0}, {-1,0}, {0,1}, {0,-1}};
        for (int[] direction : directions) {
            int rowDirection = direction[0];
            int colDirection = direction[1];

            // Start at the NEXT square in this direction
            int currentRow = position.getRow() + rowDirection;
            int currentCol = position.getColumn() + colDirection;

            //Now we want to keep moving until we can't
            while (currentRow >= 1 && currentRow <= 8 && currentCol >= 1 && currentCol <= 8) {
                ChessPiece pieceHere = board.getPiece(new ChessPosition(currentRow, currentCol));


                if (pieceHere == null){
                    //We can move here
                    validMoves.add(new ChessMove(position,  new ChessPosition(currentRow, currentCol), null));

                    // Keep going to next square
                    currentRow += rowDirection;
                    currentCol += colDirection;
                } else {
                    if (piece.getTeamColor() == pieceHere.getTeamColor()) {
                        // Same team - can't capture, just stop
                        break;
                    } else {
                        // Enemy piece - can capture it
                        validMoves.add(new ChessMove(position, new ChessPosition(currentRow, currentCol), null));
                        break; // Stop after capturing
                    }

                }
            }


        }
        return validMoves;
    }
}

