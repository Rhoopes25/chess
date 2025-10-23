package chess.pieces;

import chess.*;
import java.util.Collection;

public class RookMove {

    public static Collection<ChessMove> getMoves(ChessBoard board, ChessPosition position) {
        // Rook moves horizontally and vertically
        int[][] directions = {{1,0}, {-1,0}, {0,1}, {0,-1}};
        return MoveHelper.getSlidingMoves(board, position, directions);
    }
}

