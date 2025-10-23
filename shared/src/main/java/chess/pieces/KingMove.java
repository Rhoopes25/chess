package chess.pieces;

import chess.*;
import java.util.Collection;

public class KingMove {

    public static Collection<ChessMove> getMoves(ChessBoard board, ChessPosition position) {
        // King moves one square in any direction
        int[][] directions = {{1,1}, {1,-1}, {-1,1}, {-1,-1}, {1,0}, {-1,0}, {0,1}, {0,-1}};
        return MoveHelper.getSingleStepMoves(board, position, directions);
    }
}