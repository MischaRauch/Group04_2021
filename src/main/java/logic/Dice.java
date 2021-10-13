package logic;

import logic.board.Board;
import logic.board.Board0x88;
import logic.enums.Piece;
import logic.enums.Side;
import logic.enums.Square;

import java.util.ArrayList;

import static logic.enums.Piece.*;

public class Dice {

    public static Piece[] diceToPiece = {PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING};

    /**
     * Need to only return valid piece types. We need some way to check if the number returned is valid
     *
     * @return dice number
     */
    public static int roll() {
        return (int) ((Math.random() * 6.0) + 1);
    }

    public static int roll(State state, Side side) {
        ArrayList<Integer> validRolls = new ArrayList<>();

        if (canMove(PAWN.getColoredPiece(side), state)) {
            validRolls.add(1);
        }

        if (canMove(KNIGHT.getColoredPiece(side), state)) {
            validRolls.add(2);
        }

        if (canMove(BISHOP.getColoredPiece(side), state)) {
            validRolls.add(3);
        }

        if (canMove(ROOK.getColoredPiece(side), state)) {
            validRolls.add(4);
        }

        if (canMove(QUEEN.getColoredPiece(side), state)) {
            validRolls.add(5);
        }

        if (canMove(KING.getColoredPiece(side), state)) {
            validRolls.add(6);
        }

        return validRolls.get((int) (Math.random() * validRolls.size()));

    }

    public static boolean canMove(Piece piece, State state) {

        Board board = state.getBoard();
        Board0x88 b = (Board0x88) board;
        for (int i = 0; i < b.getBoardArray().length; i++) {
            Piece p = b.getBoardArray()[i];
            Square location = Square.getSquareByIndex(i);

            if (p == piece) {
                switch (piece.getType()) {
                    case PAWN -> {
                        //this one is more complex and weird since it depends on board state with the en passant and capturing
                        Square naturalMove = Square.getSquare(location.getSquareNumber() + piece.getOffsets()[0]);
                        if (board.isEmpty(naturalMove))
                            return true;

                        for (int k = 1; k < 3; k++) {
                            if (!board.isOffBoard(location.getSquareNumber() + piece.getOffsets()[k])) {
                                Square validTarget = Square.getSquare(location.getSquareNumber() + piece.getOffsets()[k]);
                                if (board.getPieceAt(validTarget) != Piece.EMPTY && !board.getPieceAt(validTarget).isFriendly(piece.getColor()))
                                    return true;
                            }
                        }

                        return false;
                    }

                    case KNIGHT, BISHOP, ROOK, QUEEN, KING -> {
                        for (int offset : piece.getOffsets()) {
                            if (!board.isOffBoard(location.getSquareNumber() + offset)) {
                                Square target = Square.getSquare(location.getSquareNumber() + offset);
                                if (board.isEmpty(target) || !board.getPieceAt(target).isFriendly(piece.getColor())) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * @param n             dice number rolled
     * @param possibleRolls array length 6 of all dice numbers
     *                      Example: during construction diceArr should be {1,2,3,4,5,6}
     * @return true if dice number rolled is not in dice number array
     */
    public static boolean isValid(int n, int[] possibleRolls) {
        //arr should be length 6
        for (int i = 0; i < 6; i++) {
            if (n == possibleRolls[i]) {
                return false;
            }
        }
        return true;
    }

}
