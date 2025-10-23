package chess.pieces;

import chess.*;
import java.util.Collection;

public class QueenMove {

    public static Collection<ChessMove> getMoves(ChessBoard board, ChessPosition position) {
        // Queen moves like bishop + rook (all 8 directions)
        int[][] directions = {{1,1}, {1,-1}, {-1,1}, {-1,-1}, {1,0}, {-1,0}, {0,1}, {0,-1}};
        return MoveHelper.getSlidingMoves(board, position, directions);
    }
}