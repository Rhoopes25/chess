package chess.pieces;

import chess.*;
import java.util.Collection;

public class KnightMove {

    public static Collection<ChessMove> getMoves(ChessBoard board, ChessPosition position) {
        // Knight moves in L-shapes
        int[][] directions = {{1,2}, {1,-2}, {-1,2}, {-1,-2}, {2,1}, {2,-1}, {-2,1}, {-2,-1}};
        return MoveHelper.getSingleStepMoves(board, position, directions);
    }
}