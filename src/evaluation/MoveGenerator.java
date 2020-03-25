package evaluation;

import board.Board;
import moves.Move;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MoveGenerator {
    public Move getBestMove(Board board, boolean isWhite) {
        List<Move> allMoves = board.getAllLegalMoves(isWhite, null);
        List<MoveScoreUpdater> updaters = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        int[] allScores = new int[allMoves.size()];
        for (int i = 0; i < allMoves.size(); i++) {
            if (allMoves.get(i).isUserPawnPromotion()) continue;
            int index = i;
            MoveScoreUpdater updater = newScore -> allScores[index] = newScore;
            updaters.add(updater);
            Thread thread = calculateMoveScore(3900, board.applyMove(allMoves.get(i)), !isWhite, updater);
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        try {
            Thread.sleep(4000);
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
                " , score " + allScores[allMoves.indexOf(bestMoves.get(random))] + " (index " + allMoves.indexOf(bestMoves.get(random)) + ")");
        for (int i = 0; i < allScores.length; i++) {
            System.out.print(allScores[i] + " ");
        }
        System.out.println();
        return bestMoves.get(random);
    }

    public Thread calculateMoveScore(long timeMilis, Board board, boolean isNowWhitesTurn, MoveScoreUpdater updater) {
        Thread thread = new Thread(() -> {
            long timeStart = System.currentTimeMillis();
            int depth = 0;
            while (System.currentTimeMillis() < timeStart + timeMilis) {
                depth++;
                int score = minimax(board, depth, -1000000, 1000000, isNowWhitesTurn, timeStart + timeMilis + 500);
                updater.updateScore(score);
            }
            updater.updateScore(board.getPoints());
        });
        return thread;
    }

    public int minimax(Board board, int depth, int alpha, int beta, boolean maximizing, long timeWhenMustQuit) {
        if (System.currentTimeMillis() > timeWhenMustQuit) return 0;

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
        List<Board> children = new ArrayList<Board>();
        for (Move m : moves) {
            if (!m.isUserPawnPromotion()) {
                children.add(board.applyMove(m));
            }
        }

        children.sort(new Comparator<Board>() {
            @Override
            public int compare(Board board, Board t1) {
                int points1 = board.getPoints();
                int points2 = t1.getPoints();
                return (points1 < points2) ? 1 : (points1 == points2) ? 0 : -1;
            }
        });

        if (maximizing) {
            int maxEval = -1000000;
            for (int i = 0; i < children.size(); i++) {
                int eval = minimax(children.get(i), depth-1, alpha, beta, false, timeWhenMustQuit);
                if (eval > 50000) eval--;
                if (System.currentTimeMillis() > timeWhenMustQuit) return 0;
                maxEval = (maxEval > eval ? maxEval : eval);
                alpha = (alpha > eval ? alpha : eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = 1000000;
            for (int i = children.size()-1; i >= 0; i--) {
                int eval = minimax(children.get(i), depth-1, alpha, beta, true, timeWhenMustQuit);
                if (eval < -50000) eval++;
                if (System.currentTimeMillis() > timeWhenMustQuit) return 0;
                minEval = (minEval < eval ? minEval : eval);
                beta = (beta < eval ? beta : eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }

    }
}

interface MoveScoreUpdater {
    void updateScore(int newScore);
}
