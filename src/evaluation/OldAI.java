package evaluation;

import board.Board;
import moves.Move;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OldAI {
    private static int TIMEOUT_RETURNVAL = 123456789;

    public Move getBestMove(Board board, boolean isWhite, long maxTime) {
        List<Move> allMoves = board.getAllLegalMoves(isWhite, null);
        List<MoveScoreUpdater> updaters = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        int[] allScores = new int[allMoves.size()];
        int[] depths = new int[allMoves.size()];
        for (int i = 0; i < allMoves.size(); i++) {
            if (allMoves.get(i).isUserPawnPromotion()) continue;
            int index = i;
            MoveScoreUpdater updater = (newScore, depth) -> {
                allScores[index] = newScore;
                depths[index] = depth;
            };
            updaters.add(updater);
            Thread thread = calculateMoveScore(maxTime, board.applyMove(allMoves.get(i)), !isWhite, updater);
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        try {
            Thread.sleep(maxTime);
        } catch (InterruptedException ignored) {}

        // Now, the threads are all finished, or are finishing.
        List<Move> bestMoves = new ArrayList<>();
        int bestScore = (isWhite ? -1000000 : 1000000);
        for (int i = 0; i < allScores.length; i++) {
            if (isWhite) {
                if (allScores[i] > bestScore) {
                    bestScore = allScores[i];
                    bestMoves = new ArrayList<>();
                    bestMoves.add(allMoves.get(i));
                } else if (allScores[i] == bestScore) {
                    bestMoves.add(allMoves.get(i));
                }
            } else {
                if (allScores[i] < bestScore) {
                    bestScore = allScores[i];
                    bestMoves = new ArrayList<>();
                    bestMoves.add(allMoves.get(i));
                } else if (allScores[i] == bestScore) {
                    bestMoves.add(allMoves.get(i));
                }
            }
        }
        int random = (int) (Math.random() * bestMoves.size());
        System.out.println("Returning random of " + bestMoves.size() + " moves: " + bestMoves.get(random) +
                " , score " + allScores[allMoves.indexOf(bestMoves.get(random))] + " (index "
                + allMoves.indexOf(bestMoves.get(random)) + ", depth " + depths[allMoves.indexOf(bestMoves.get(random))] + ")");
        for (int allScore : allScores) {
            System.out.print(allScore + " ");
        }
        System.out.println();
        return bestMoves.get(random);
    }

    private Thread calculateMoveScore(long timeMilis, Board board, boolean isNowWhiteTurn, MoveScoreUpdater updater) {
        return new Thread(() -> {
            long timeStart = System.currentTimeMillis();
            int depth = 0;
            while (System.currentTimeMillis() < timeStart + timeMilis) {
                depth++;
                int score = minimax(board, depth, -1000000, 1000000, isNowWhiteTurn, timeStart + timeMilis + 500);
                if (score != TIMEOUT_RETURNVAL) updater.updateScore(score, depth+1);
            }
        });
    }

    private int minimax(Board board, int depth, int alpha, int beta, boolean maximizing, long timeWhenMustQuit) {
        if (System.currentTimeMillis() > timeWhenMustQuit) return TIMEOUT_RETURNVAL;

        if (depth == 0) {
            return board.getPoints();
        }
        if (board.playerCannotMove(maximizing)) {
            if (board.kingIsInCheck(maximizing)) {
                return (maximizing ? -1 : 1) * 100000; // Checkmate, avoid at all costs.
            } else {
                return 0; // Draw.
            }
        }

        List<Move> moves = board.getAllLegalMoves(maximizing, null);
        List<Board> children = new ArrayList<>();
        for (Move m : moves) {
            if (!m.isUserPawnPromotion()) {
                children.add(board.applyMove(m));
            }
        }

        children.sort((board1, t1) -> {
            int points1 = board1.getPoints();
            int points2 = t1.getPoints();
            return Integer.compare(points2, points1);
        });

        if (maximizing) {
            int maxEval = -1000000;
            for (Board child : children) {
                int eval = minimax(child, /* board.pieceId((byte) moves.get(i).getEnd()) < 0 ? depth : */ depth - 1, alpha, beta, false, timeWhenMustQuit);
                if (System.currentTimeMillis() > timeWhenMustQuit) return TIMEOUT_RETURNVAL;
                eval = (int) (0.97 * eval);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = 1000000;
            for (int i = children.size()-1; i >= 0; i--) {
                int eval = minimax(children.get(i), /* board.pieceId((byte) moves.get(i).getEnd()) > 0 ? depth : */ depth-1, alpha, beta, true, timeWhenMustQuit);
                if (System.currentTimeMillis() > timeWhenMustQuit) return TIMEOUT_RETURNVAL;
                eval = (int) (0.97*eval + 1);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }

    }
}

interface MoveScoreUpdater {
    void updateScore(int newScore, int depth);
}
