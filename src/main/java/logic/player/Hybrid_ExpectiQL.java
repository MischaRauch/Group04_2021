package logic.player;

import logic.LegalMoveEvaluator;
import logic.Move;
import logic.State;
import logic.algorithms.expectiminimax.ExpectiMiniMaxThread;
import logic.enums.Side;

public class Hybrid_ExpectiQL extends AIPlayer{private final LegalMoveEvaluator evaluator = new LegalMoveEvaluator();
    private final int depth;
    private long timeNeeded;

    public Hybrid_ExpectiQL(int depth, Side color) {
        super(color);
        this.depth = depth;
    }

    @Override
    public Move chooseMove(State state) {
        // java creates a seperate thread to compute minimax while main thread still computing
        ExpectiMiniMaxThread thread = new ExpectiMiniMaxThread(depth,state, true);
        thread.start();
        try {
            // threads execute code in sequence
            // waits for thread to complete continuing executing this code of choose move
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Move chosenMove = thread.getBestMove();
        evaluator.isLegalMove(chosenMove, state, true, true);
        //System.out.println("Hybrid_ExpectiQL: Color: " + this.color.toString() + " Next optimal Move: " + chosenMove);
        timeNeeded = thread.getTimeNeeded();
        return chosenMove;
    }

    @Override
    public String getNameAi() {
        return "Hybrid_ExpectiQL";
    }

    @Override
    public long getTimeNeeded() {
        return timeNeeded;
    }

}
