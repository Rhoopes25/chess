package chess.pieces;

import chess.*;
import java.util.Collection;

public class BishopMoves {

    public static Collection<ChessMove> getMoves(ChessBoard board, ChessPosition position) {
        // Bishop moves diagonally
        int[][] directions = {{1,1}, {1,-1}, {-1,1}, {-1,-1}};
        return MoveHelper.getSlidingMoves(board, position, directions);
    }
}