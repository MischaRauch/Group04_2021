package logic;

import gui.PromotionPrompt;
import javafx.application.Platform;
import logic.board.Board;
import logic.enums.CastlingRights;
import logic.enums.Piece;
import logic.enums.Side;
import logic.enums.Square;

import java.sql.SQLOutput;
import java.util.EnumSet;

import static logic.enums.Piece.*;
import static logic.enums.Side.*;

public class State {

    public static int gameOver;
    public Board board;
    public int diceRoll;
    public Side color;
    static boolean applyCastling = false;
    static boolean shortCastlingWhite = true;
    static boolean longCastlingWhite = true;
    static boolean shortCastlingBlack = true;
    static boolean longCastlingBlack = true;

    public Square enPassant = Square.INVALID;

    EnumSet<CastlingRights> castleRights = EnumSet.allOf(CastlingRights.class);
    EnumSet<Piece> availableWhitePieces = EnumSet.of(WHITE_PAWN, WHITE_KNIGHT, WHITE_BISHOP, WHITE_ROOK, WHITE_QUEEN, WHITE_KING);
    EnumSet<Piece> availableBlackPieces = EnumSet.of(BLACK_PAWN, BLACK_KNIGHT, BLACK_BISHOP, BLACK_ROOK, BLACK_QUEEN, BLACK_KING);

    public State(Board board, int diceRoll, Side color) {
        this.board = board;
        this.diceRoll = diceRoll;
        this.color = color;
    }

    public Board getBoard() {
        return this.board;
    }

    public int getGameOver() {
        return gameOver;
    }

    public State applyMove(Move move) {
        //extract castling en passant dice roll

        //check if king got captured
        if (board.getPieceAt(move.getDestination()) == WHITE_KING) {
            gameOver = -1;
        }

        if (board.getPieceAt(move.getDestination()) == BLACK_KING) {
            gameOver = 1;
        }

        int newRoll = Dice.roll();

        Side nextTurn = color == WHITE ? BLACK : WHITE;

        System.out.println("PIECE FOR CASTLING "+ move.castling);
        System.out.println("Boolean for castling S B "+shortCastlingBlack);
        System.out.println("Boolean for castling S W "+shortCastlingWhite);
        System.out.println("Boolean for castling L B "+longCastlingBlack);
        System.out.println("Boolean for castling L W "+longCastlingWhite);
        //update available pieces sets
        Board newBoard = board.movePiece(move.origin, move.destination);

        if (move.enPassantCapture) {
            newBoard.removePiece(color == WHITE ? move.destination.getSquareBelow() : move.destination.getSquareAbove());
        }
        //check if castling has happend and the rook needs to move
        if (applyCastling) {
        // (1) if (shortCastlingWhite || shortCastlingBlack || longCastlingBlack || longCastlingWhite) {
        // (2) if ((shortCastlingWhite || shortCastlingBlack || longCastlingBlack || longCastlingWhite)) = false {
        // (1) would check till all castle moves are not possible anymore at the beginning of the game, while
        //(2) would check till the end of the game if castling is possible --> I created a new applyCastling boolean
        //to check if castling was done - more efficient over the long run but not optimal
            System.out.println("WHY");
            if (move.castling != Square.INVALID) {
                //check which rook has to move based on the setted square
                if (move.castling == Square.f1) {
                    newBoard.setPiece(EMPTY, Square.h1);
                    newBoard.setPiece(WHITE_ROOK, move.castling);
                    longCastlingWhite = false;
                }
                if (move.castling == Square.d1) {
                    newBoard.setPiece(EMPTY, Square.a1);
                    newBoard.setPiece(WHITE_ROOK, move.castling);
                    shortCastlingWhite = false;
                }
                if (move.castling == Square.f8) {
                    newBoard.setPiece(EMPTY, Square.h8);
                    newBoard.setPiece(BLACK_ROOK, Square.f8);
                    longCastlingBlack = false;
                }
                if (move.castling == Square.d8) {
                    newBoard.setPiece(EMPTY, Square.a8);
                    newBoard.setPiece(BLACK_ROOK, move.castling);
                    shortCastlingBlack = false;
                }
            }
            applyCastling = false;
        }

        if (move.promotionMove) {
            newBoard.setPiece(move.promotionPiece, move.destination);
        }

        State nextState = new State(newBoard, newRoll, nextTurn);

        if (move.enPassantMove) {
            nextState.enPassant = move.enPassant;
        }

        nextState.diceRoll = Dice.roll(nextState, nextTurn);

        newBoard.printBoard();
        return nextState;
    }


}
