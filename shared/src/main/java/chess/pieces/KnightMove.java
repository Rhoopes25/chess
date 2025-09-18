package chess.pieces;

import chess.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KnightMove {

    public static Collection<ChessMove> getMoves(ChessBoard board, ChessPosition position) {
        ChessPiece piece = board.getPiece(position);

        //LIST TO HOLD ALL VALID MOVES INTO IT
        List<ChessMove> validMoves = new ArrayList<>();
        int[][] directions = {{1,2}, {1,-2}, {-1,2}, {-1,-2}, {2,1}, {2,-1}, {-2,1}, {-2,-1}};        for (int[] direction : directions) {
            int rowDirection = direction[0];
            int colDirection = direction[1];

            // Start at the NEXT square in this direction
            int currentRow = position.getRow() + rowDirection;
            int currentCol = position.getColumn() + colDirection;

            //Now we want to keep moving until we can't
            if (currentRow >= 1 && currentRow <= 8 && currentCol >= 1 && currentCol <= 8) {
                ChessPiece pieceHere = board.getPiece(new ChessPosition(currentRow, currentCol));


                if (pieceHere == null || piece.getTeamColor() != pieceHere.getTeamColor()){
                    //We can move here
                    validMoves.add(new ChessMove(position,  new ChessPosition(currentRow, currentCol), null));


                }


            }


        }
        return validMoves;
    }
}