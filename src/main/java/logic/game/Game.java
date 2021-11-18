package logic.game;

import logic.board.Board0x88;
import logic.enums.Piece;
import logic.enums.Side;
import logic.enums.Square;
import logic.*;

import java.util.Stack;

import static logic.enums.Piece.BLACK_ROOK;

public abstract class Game {

    public static String openingFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1 1";
    protected static Game CURRENT_GAME;

    protected final Stack<State> previousStates;
    protected final Stack<State> redoStates;
    protected final LegalMoveEvaluator evaluator = new LegalMoveEvaluator();
    protected State currentState;
    protected boolean gameOver = false;

    //indicated is in last state a castling was performed to disable castling rights
    //for the beginning of the next move - 0 = none, 1 = shortCasltingWhite
    //2 = shortCastlingBlack, 3 = longCastlingWhite, 4 = longCastlingBlack
    public static int castlingPerformed = 0;

    protected Stack<PieceAndTurnDeathTuple<Piece, Integer>> deadBlackPieces = new Stack<>();
    protected Stack<PieceAndTurnDeathTuple<Piece, Integer>> deadWhitePieces = new Stack<>();

    protected Stack<PieceAndTurnDeathTuple<Piece, Integer>> redoDeadBlackPieces = new Stack<>();
    protected Stack<PieceAndTurnDeathTuple<Piece, Integer>> redoDeadWhitePieces = new Stack<>();



    public Game() {
        this(openingFEN);
        //currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.OFF_BOARD, Square.b1));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.BLACK_ROOK, Square.a8));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.BLACK_KNIGHT, Square.b8));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.BLACK_BISHOP, Square.c8));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.BLACK_QUEEN, Square.d8));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.BLACK_KING, Square.e8));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.BLACK_BISHOP, Square.f8));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.BLACK_KNIGHT, Square.g8));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.BLACK_ROOK, Square.h8));

        //currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.BLACK_ROOK, Square.a4));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.BLACK_PAWN, Square.a7));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.BLACK_PAWN, Square.b7));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.BLACK_PAWN, Square.c7));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.BLACK_PAWN, Square.d7));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.BLACK_PAWN, Square.e7));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.BLACK_PAWN, Square.f7));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.BLACK_PAWN, Square.g7));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.BLACK_PAWN, Square.h7));

        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.WHITE_PAWN, Square.a2));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.WHITE_PAWN, Square.b2));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.WHITE_PAWN, Square.c2));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.WHITE_PAWN, Square.d2));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.WHITE_PAWN, Square.e2));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.WHITE_PAWN, Square.f2));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.WHITE_PAWN, Square.g2));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.WHITE_PAWN, Square.h2));

        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.WHITE_ROOK, Square.a1));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.WHITE_KNIGHT, Square.b1));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.WHITE_BISHOP, Square.c1));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.WHITE_QUEEN, Square.d1));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.WHITE_KING, Square.e1));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.WHITE_BISHOP, Square.f1));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.WHITE_KNIGHT, Square.g1));
        currentState.getPieceAndSquare().add(new PieceAndSquareTuple(Piece.WHITE_ROOK, Square.h1));
        System.out.println("GAME");
    }

    public Game(String initialPosition) {
        currentState = new State(new Board0x88(initialPosition), Math.random() < 0.5 ? 1 : 2, Side.WHITE);
        currentState.diceRoll = Dice.roll(currentState, Side.WHITE);
        previousStates = new Stack<>();
        redoStates = new Stack<>();
        CURRENT_GAME = this;
    }

    public void undoState() {
        if (!previousStates.isEmpty()) {
            redoStates.push(currentState);              //push current state to redo stack in case user wants to redo
            currentState = previousStates.pop();        //pop the previous state off the stack
        }
    }

    public void redoState() {
        if (!redoStates.isEmpty()) {
            previousStates.push(currentState);          //add current state to previous states stack
            currentState = redoStates.pop();            //update the current state

        }
    }

    protected void processCastling() {
        //check if castling was performed
        if (currentState.isApplyCastling()) {
            if (currentState.castling == Square.f8) {
                System.out.println("SHORT CASTLING BLACK WAS PERFROMED 09");
                castlingPerformed = 2;
            }
            if (currentState.castling == Square.d8) {
                System.out.println("LONG CASTLING BLACK WAS PERFORMED 09");
                castlingPerformed = 4;
            }
            if (currentState.castling == Square.f1) {
                System.out.println("SHORT CASTLING WHITE WAS PERFORMED 09");
                castlingPerformed = 1;
            }
            if (currentState.castling == Square.d1) {
                System.out.println("LONG CASTLING WHITE WAS PERFORMED 09");
                castlingPerformed = 3;
            }

            currentState.castling = Square.INVALID;
        }
    }

    public static Game getInstance() {
        return CURRENT_GAME;
    }

    public State getCurrentState() {
        return currentState;
    }

    public Side getTurn() {
        return currentState.color;
    }

    public int getDiceRoll() {
        return currentState.diceRoll;
    }

    public Stack<State> getPreviousStates() {
        return previousStates;
    }

    public Stack<State> getRedoStates() {
        return redoStates;
    }

    public int getCastlingPerformed() { return castlingPerformed; }

    public void setCastlingPerformed(int casPerf) { castlingPerformed = casPerf; }

    public Stack<PieceAndTurnDeathTuple<Piece, Integer>> getRedoDeadBlackPieces() {
        return redoDeadBlackPieces;
    }

    public Stack<PieceAndTurnDeathTuple<Piece, Integer>> getRedoDeadWhitePieces() {
        return redoDeadWhitePieces;
    }

    public Stack<PieceAndTurnDeathTuple<Piece, Integer>> getDeadBlackPieces() {
        return deadBlackPieces;
    }

    public Stack<PieceAndTurnDeathTuple<Piece, Integer>> getDeadWhitePieces() {
        return deadWhitePieces;
    }


}
