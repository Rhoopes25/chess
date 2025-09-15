package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type
            ;
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        if (piece.getPieceType() == PieceType.BISHOP) {
            //LIST TO HOLD ALL VALID MOVES INTO IT
            List<ChessMove> validMoves = new ArrayList<>();
            int[][] directions = {{1,1}, {1,-1}, {-1,1}, {-1,-1}};
            for (int[] direction : directions) {
                int rowDirection = direction[0];
                int colDirection = direction[1];

                // Start at the NEXT square in this direction
                int currentRow = myPosition.getRow() + rowDirection;
                int currentCol = myPosition.getColumn() + colDirection;

                //Now we want to keep moving until we can't
                while (currentRow >= 1 && currentRow <= 8 && currentCol >= 1 && currentCol <= 8) {
                    ChessPiece pieceHere = board.getPiece(new ChessPosition(currentRow, currentCol));

                    // is something at next square
                    ChessPiece pieceAtCurrentSquare = board.getPiece(new ChessPosition(currentRow, currentCol));

                    if (pieceAtCurrentSquare == null){
                        //We can move here
                        validMoves.add(new ChessMove(myPosition,  new ChessPosition(currentRow, currentCol), null));

                        // Keep going to next square
                        currentRow += rowDirection;
                        currentCol += colDirection;
                    } else {
                        // There's a piece here - now what?
                        // TODO: check if friend or enemy
                        break; // Stop this direction for now

                    }
                }


                }


        }
        return List.of();
    }
}
