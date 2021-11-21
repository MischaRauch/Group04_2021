package logic.player;

import logic.PieceAndSquareTuple;
import logic.enums.Side;
import logic.Move;
import logic.State;
import logic.expectiminimax.MiniMax;
import logic.expectiminimax.Node;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class MiniMaxPlayer extends AIPlayer {

    private final boolean DEBUG = false;
    private final boolean DEBUG2 = false;
    private int depth;

    public MiniMaxPlayer(int depth, Side color) {
        super(color);
        this.depth=depth;
    }

    @Override
    public Move chooseMove(State state) {
        System.out.println("ExpectiMiniMaxPlayer;  chooseMove(State state): ");
        long start = System.nanoTime();
        MiniMax miniMax = new MiniMax();
        miniMax.constructTree(depth,state);
        Node bestChild = miniMax.findBestChild(true,miniMax.getTree().getRoot().getChildren(),state.diceRoll);
        System.out.println("Optimal Move: " + bestChild.getMove().toString());
        Move chosenMove = bestChild.getMove();
        updatePieceAndSquareState(state,chosenMove);
        long end = System.nanoTime();
        System.out.println("Elapsed Time to generate tree and find optimal move: "+ (end-start));
        return chosenMove;
    }

    private void updatePieceAndSquareState(State state, Move move) {
        state.printPieceAndSquare();

        List<PieceAndSquareTuple> list = new CopyOnWriteArrayList<PieceAndSquareTuple>();
        ListIterator litr = state.getPieceAndSquare().listIterator();

        while(litr.hasNext()) {
            PieceAndSquareTuple t = (PieceAndSquareTuple) litr.next();
            if (t.getSquare() == move.getOrigin()) {
                if( DEBUG2)System.out.println("skipped: " + t.getPiece().toString() + t.getSquare().toString());
            } else {
                list.add((PieceAndSquareTuple)t);
                if( DEBUG2)System.out.println("added: " + t.getPiece().toString() + t.getSquare().toString());
            }
        }
        PieceAndSquareTuple tupleForLeavingSquare = new PieceAndSquareTuple(move.getPiece(), move.getDestination());
        list.add(tupleForLeavingSquare);
        if( DEBUG2)System.out.println("added:2 " + tupleForLeavingSquare.getPiece().toString() + tupleForLeavingSquare.getSquare().toString());

        List<PieceAndSquareTuple> list2 = new CopyOnWriteArrayList<PieceAndSquareTuple>();

        ListIterator litr2 = list.listIterator();
        while(litr2.hasNext()) {
            PieceAndSquareTuple t = (PieceAndSquareTuple) litr2.next();
            if (tupleForLeavingSquare.getSquare()==t.getSquare() &&  litr2.nextIndex()==list.size()-1) {
                litr2.next();
                if( DEBUG2)System.out.println("skipped: " + t.getPiece().toString() + t.getSquare().toString());
            } else if (tupleForLeavingSquare.getSquare()!=t.getSquare() && litr2.nextIndex()!=list.size()-1){
                list2.add(t);
                if( DEBUG2)System.out.println("added:1 " + t.getPiece().toString() + t.getSquare().toString());
            } else if (litr2.nextIndex()==list.size()-1){
                list2.add(t);
                if( DEBUG2)System.out.println("added:2 " + t.getPiece().toString() + t.getSquare().toString());
            }
        }
        list2.add(tupleForLeavingSquare);

        state.setPieceAndSquare(list2);
    }

}
