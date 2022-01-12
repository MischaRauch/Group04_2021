package logic.algorithms.expectiminimax;

import logic.BoardStateAndEvaluationNumberTuple;
import logic.Move;
import logic.PieceAndSquareTuple;
import logic.State;
import logic.algorithms.BoardStateGenerator;
import logic.enums.Piece;
import logic.enums.Side;

import java.util.ArrayList;
import java.util.List;

public class ExpectiMiniMax {

    private final BoardStateGenerator gen = new BoardStateGenerator();
    private ExpectiMiniMaxTree tree;
    private ExpectiMiniMaxNode bestNode;

    public void constructTree(int depth, State initialBoardState) {
        this.tree = new ExpectiMiniMaxTree();
        State state = new State(initialBoardState);
        tree.setRoot(new ExpectiMiniMaxNode(true, state));
        constructTree(tree.getRoot(), depth);
    }

    private void constructTree(ExpectiMiniMaxNode parentNode, int depth) {
        if (depth == 0) {
            bestNode = parentNode;
        }
        while (depth != 0) {
            List<BoardStateAndEvaluationNumberTuple> allStatesAndBoardEvaluationsForGivenPieceType = generateAllPossibleStatesForGivenNode(parentNode);
            List<Piece> pieceList = generatePieceTypesForGivenNode(parentNode);

            boolean isChildMaxPlayer = (parentNode.isMaxPlayer() ? false : true);
            // parent (current node minimizer -> child maximizer
            if (!isChildMaxPlayer) {
                ExpectiMiniMaxNode bestChild = addChildrenReturnBestChild(parentNode, allStatesAndBoardEvaluationsForGivenPieceType, false, pieceList);
                depth -= 1;
                constructTree(bestChild, depth);
            }
            // parent (current node) maximizer -> child minimizer
            else if (isChildMaxPlayer) {
                ExpectiMiniMaxNode bestChild = addChildrenReturnBestChild(parentNode, allStatesAndBoardEvaluationsForGivenPieceType, true, pieceList);
                depth -= 1;
                constructTree(bestChild, depth);
            }
        }
    }

    private List<Piece> generatePieceTypesForGivenNode(ExpectiMiniMaxNode parentNode) {
        List<Piece> pieces = new ArrayList<>();
        for (int i = 1; i < 7; i++) {
            // evaluation numbers for i-th dice roll
            List<Integer> possibleEvaluationNumbersForGivenPiece = gen.getPossibleBoardStatesWeightsOfSpecificPiece(parentNode.getState().getPieceAndSquare(),
                    parentNode.getState().getColor(), i, parentNode.getState());
            if (!possibleEvaluationNumbersForGivenPiece.isEmpty()) {
                pieces.add(Piece.getPieceFromDice(i, parentNode.getState().getColor()));
            }
        }
        return pieces;
    }

    private List<BoardStateAndEvaluationNumberTuple> generateAllPossibleStatesForGivenNode(ExpectiMiniMaxNode parentNode) {
        List<BoardStateAndEvaluationNumberTuple> allStatesAndBoardEvaluationsForGivenPieceType = new ArrayList<>();
        // state generation
        // loop through for all 6 dice numbers and generate all possible states
        for (int i = 1; i < 7; i++) {
            // evaluation numbers for i-th dice roll
            List<Integer> possibleEvaluationNumbersForGivenPiece = gen.getPossibleBoardStatesWeightsOfSpecificPiece(parentNode.getState().getPieceAndSquare(),
                    parentNode.getState().getColor(), i, parentNode.getState());
            // possible board states for i-th dice roll
            List<List<PieceAndSquareTuple>> possibleBoardStatesForGivenPiece = gen.getPossibleBoardStates(parentNode.getState().getPieceAndSquare(),
                    parentNode.getState().getColor(), i, parentNode.getState());
            // add this tuple of list of possible eval numbers and board states to tuple list
            // only add if not empty
            if (!possibleEvaluationNumbersForGivenPiece.isEmpty()) {
                allStatesAndBoardEvaluationsForGivenPieceType.add(new BoardStateAndEvaluationNumberTuple(possibleBoardStatesForGivenPiece, possibleEvaluationNumbersForGivenPiece));
            }
        }
        return allStatesAndBoardEvaluationsForGivenPieceType;
    }

    private ExpectiMiniMaxNode addChildrenReturnBestChild(ExpectiMiniMaxNode parentNode,
        List<BoardStateAndEvaluationNumberTuple> allStatesAndBoardEvaluationsForGivenPieceType, boolean isChildMaxPlayer, List<Piece> pieceList) {

        int chanceDivider = allStatesAndBoardEvaluationsForGivenPieceType.size();
        for (int i = 0; i < allStatesAndBoardEvaluationsForGivenPieceType.size(); i++) {
            // only add for dice numbers that are not empty

            List<List<PieceAndSquareTuple>> statesForGivenPiece = allStatesAndBoardEvaluationsForGivenPieceType.get(i).getBoardStates();

            List<Integer> boardEvaluationNumbersForGivenPiece = allStatesAndBoardEvaluationsForGivenPieceType.get(i).getEvaluationNumbers();

            int sumOfEvalNumbers = getSumOfEvalNumbers(allStatesAndBoardEvaluationsForGivenPieceType, i);
            int nodeValue = sumOfEvalNumbers / chanceDivider;
            int bestBoardEvaluationIndex = getBestBoardEvaluationNumberIndex(allStatesAndBoardEvaluationsForGivenPieceType, i, isChildMaxPlayer);
            List<Move> legalMovesForGivePiece = gen.getValidMovesForGivenPiece(parentNode.getState(), pieceList.get(i));

            ExpectiMiniMaxNode newNode = new ExpectiMiniMaxNode(isChildMaxPlayer, statesForGivenPiece, boardEvaluationNumbersForGivenPiece,
                    chanceDivider, nodeValue, null, legalMovesForGivePiece, pieceList.get(i));

            List<PieceAndSquareTuple> bestState = statesForGivenPiece.get(bestBoardEvaluationIndex);

            Piece bestStatePieceThatMoved = (Piece) bestState.get(bestState.size() - 1).getPiece();

            // updating bestStatePiece that moved
            State newState = new State(parentNode.getState().getBoard(), Piece.getDiceFromPiece(bestStatePieceThatMoved),
                    bestStatePieceThatMoved.getColor(), parentNode.getState().isCanCastleWhite(), parentNode.getState().isCanCastleBlack(), parentNode.getState().isShortCastlingBlack(),
                    parentNode.getState().isShortCastlingWhite(), parentNode.getState().isLongCastlingBlack(), parentNode.getState().isLongCastlingWhite(),
                    parentNode.getState().castling, parentNode.getState().getPieceAndSquare(), parentNode.getState().getCumulativeTurn());

            // updated Board board and pieceAndSquare Board
            newState.applyMove(legalMovesForGivePiece.get(getBestBoardEvaluationIndexFromNode(newNode)));
            //State newState = new State(parentNode.getState());

            newNode.setState(newState);

            parentNode.addChild(newNode);
        }

        ExpectiMiniMaxNode bestChild = parentNode.getChildren().get(getBestChildIndex(parentNode, !isChildMaxPlayer));
        bestChild.setMaxPlayer(isChildMaxPlayer);
        State updatedState = new State(parentNode.getState());
        // invert color of state
        updatedState.setColor(Side.getOpposite(updatedState.getColor()));
        // don't update cummulative turn
        bestChild.setState(updatedState);
        return bestChild;
    }

    // sum evaluation numbers for given piece type
    private int getSumOfEvalNumbers(List<BoardStateAndEvaluationNumberTuple> allStatesAndBoardEvaluationsForGivenPieceType, int indexZeroBased) {
        int sum = 0;
        for (int i = 0; i < allStatesAndBoardEvaluationsForGivenPieceType.size(); i++) {
            for (int j = 0; j < allStatesAndBoardEvaluationsForGivenPieceType.get(indexZeroBased).getEvaluationNumbers().size(); j++) {
                sum += (int) allStatesAndBoardEvaluationsForGivenPieceType.get(indexZeroBased).getEvaluationNumbers().get(j);
            }
        }
        return sum;
    }

    // return values between 0 and 5
    private int getBestChildIndex(ExpectiMiniMaxNode parentNode, boolean isMax) {
        int max = Integer.MAX_VALUE;
        int min = Integer.MIN_VALUE;
        int indexBestChild = -1;
        int index = 0;
        for (ExpectiMiniMaxNode n : parentNode.getChildren()) {
            if (!isMax && n.getNodeValue() > min) {
                min = n.getNodeValue();
                indexBestChild = index;
                index++;
            } else if (isMax && n.getNodeValue() < max) {
                max = n.getNodeValue();
                indexBestChild = index;
                index++;
            }
        }
        return indexBestChild;
    }

    private int getBestBoardEvaluationNumberIndex(List<BoardStateAndEvaluationNumberTuple> allStatesAndBoardEvaluationsForGivenPieceType, int indexZeroBased, boolean isMax) {
        int bestBoardEvaluationNumber = 0;
        int bestIndex = 0;
        int max = Integer.MAX_VALUE;
        int min = Integer.MIN_VALUE;
        for (int i = 0; i < allStatesAndBoardEvaluationsForGivenPieceType.size(); i++) {
            for (int j = 0; j < allStatesAndBoardEvaluationsForGivenPieceType.get(indexZeroBased).getEvaluationNumbers().size(); j++) {
                // mazimizer
                if (isMax) {
                    if ((int) allStatesAndBoardEvaluationsForGivenPieceType.get(indexZeroBased).getEvaluationNumbers().get(j) > min) {
                        min = (int) allStatesAndBoardEvaluationsForGivenPieceType.get(indexZeroBased).getEvaluationNumbers().get(j);
                        bestBoardEvaluationNumber = min;
                        bestIndex = j;
                    }
                    // minimizer
                } else {
                    if ((int) allStatesAndBoardEvaluationsForGivenPieceType.get(indexZeroBased).getEvaluationNumbers().get(j) < max) {
                        max = (int) allStatesAndBoardEvaluationsForGivenPieceType.get(indexZeroBased).getEvaluationNumbers().get(j);
                        bestBoardEvaluationNumber = max;
                        bestIndex = j;
                    }
                }
            }
        }
        return bestIndex;
    }

    private int getBestBoardEvaluationIndexFromNode(ExpectiMiniMaxNode node) {
        int min = Integer.MIN_VALUE;
        int max = Integer.MAX_VALUE;
        int bestEval = 0;
        int indexForBestEval = -1;

        // get index of the best evaluation number -> will correspond to index of best move
        for (Integer i : node.getBoardEvaluationNumbersForGivenPiece()) {
            if (!node.isMaxPlayer() && i > min) {
                min = i;
                bestEval = min;
            } else if (node.isMaxPlayer() && i < max) {
                max = i;
                bestEval = max;
            }
        }
        for (int i = 0; i < node.getBoardEvaluationNumbersForGivenPiece().size(); i++) {
            if (bestEval == node.getBoardEvaluationNumbersForGivenPiece().get(i)) {
                indexForBestEval = i;
                break;
            }
        }
        return indexForBestEval;
    }

    // only called after tree generated
    private int getBestBoardEvaluationIndexFromBestNode() {
        int min = Integer.MIN_VALUE;
        int bestEval = 0;
        int indexForBestEval = -1;

        // get index of the best evaluation number -> will correspond to index of best move
        // root is always maximizing player
        for (Integer i : bestNode.getBoardEvaluationNumbersForGivenPiece()) {
            if(i > min) {
                min = i;
                bestEval = min;
            }
//            if (!bestNode.isMaxPlayer() && i > min) {
//                min = i;
//                bestEval = min;
//            }else if (bestNode.isMaxPlayer() && i < max) {
//                max = i;
//                bestEval = max;
//            }
        }
        for (int i = 0; i < bestNode.getBoardEvaluationNumbersForGivenPiece().size(); i++) {
            if (bestEval == bestNode.getBoardEvaluationNumbersForGivenPiece().get(i)) {
                indexForBestEval = i;
                break;
            }
        }
        return indexForBestEval;
    }

    public Move getBestMoveForBestNode() {
        List<Move> allMoveForGivenPiece = gen.getValidMovesForGivenPiece(bestNode.getState(), bestNode.getPiece());
        List<PieceAndSquareTuple> bestStatePAS = bestNode.getPossibleBoardStatesForGivenPiece().get(getBestBoardEvaluationIndexFromBestNode());
        for (PieceAndSquareTuple t : bestStatePAS) {
            Piece coloredPiece = (Piece) t.getPiece();
            for (Move m : allMoveForGivenPiece) {
                if (t.getSquare() == m.getDestination() && t.getPiece() == m.getPiece() && bestNode.getPiece().getColor() == coloredPiece.getColor()) {
                    return m;
                }
            }
        }
        return null;
    }

    public ExpectiMiniMaxTree getTree() {
        return tree;
    }

}
