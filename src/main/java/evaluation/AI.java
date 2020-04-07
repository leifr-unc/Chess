package main.java.evaluation;

import main.java.board.Board;
import main.java.moves.MoveUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AI {
    private static int TIMEOUT_RETURNVAL = 123456789;

    public long getBestMove(Board board, boolean isWhite, long maxTime, long[] allMoves, boolean endgame) {
        List<MoveScoreUpdater> updaters = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        int[] allScores = new int[allMoves.length];
        int[] depths = new int[allMoves.length];
        long time = System.currentTimeMillis();

        AtomicInteger maxDepthSoFar = new AtomicInteger();
        for (int i = 0; i < allMoves.length; i++) {
            if (MoveUtils.isUserPawnPromotion(allMoves[i])) continue;
            int index = i;
            MoveScoreUpdater updater = (newScore, depth) -> {
                allScores[index] = newScore;
                depths[index] = depth;
                if (index == 0 && depth > maxDepthSoFar.get()) {
                    System.out.println("Depth: " + depth + ", " + (System.currentTimeMillis()-time));
                    maxDepthSoFar.set(depth);
                }
            };
            updaters.add(updater);
            Board cloned = board.clone();
            cloned.applyMove(allMoves[i]);
            Thread thread = generateMinimaxThread(maxTime, cloned, !isWhite, updater, endgame);
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        try {
            Thread.sleep(maxTime);
        } catch (InterruptedException ignored) {}

        // Now, the threads are all finished, or are finishing.
        List<Long> bestMoves = new ArrayList<>();
        int bestScore = (isWhite ? -1000000 : 1000000);
        for (int i = 0; i < allScores.length; i++) {
            if (MoveUtils.isUserPawnPromotion(allMoves[i])) continue;
            if (isWhite) {
                if (allScores[i] > bestScore) {
                    bestScore = allScores[i];
                    bestMoves = new ArrayList<>();
                    bestMoves.add(allMoves[i]);
                } else if (allScores[i] == bestScore) {
                    bestMoves.add(allMoves[i]);
                }
            } else {
                if (allScores[i] < bestScore) {
                    bestScore = allScores[i];
                    bestMoves = new ArrayList<>();
                    bestMoves.add(allMoves[i]);
                } else if (allScores[i] == bestScore) {
                    bestMoves.add(allMoves[i]);
                }
            }
        }

        int random = (int) (Math.random() * bestMoves.size());
        return bestMoves.get(random);
    }

    private Thread generateMinimaxThread(long timeMilis, Board board, boolean isNowWhiteTurn, MoveScoreUpdater updater, boolean endgame) {
        return new Thread(() -> {
            long timeStart = System.currentTimeMillis();
            int depth = 0;
            while (System.currentTimeMillis() < timeStart + timeMilis) {
                depth++;
                int score = minimax(board, depth, -1000000, 1000000, isNowWhiteTurn, timeStart + timeMilis - 200, endgame, 0);
                if (score != TIMEOUT_RETURNVAL) updater.updateScore(score, depth+1);
            }
        });
    }

    private int minimax(Board board, int depth, int alpha, int beta, boolean maximizing, long timeWhenMustQuit, boolean endgame, int boring) {
        long[] moves = board.getAllLegalMoves(maximizing, false);

        if (moves.length == 0) {
            if (board.kingIsInCheck(maximizing)) {
                return (maximizing ? -100000 : 100000); // Checkmate, avoid at all costs.
            } else {
                return 0; // Draw.
            }
        }
        // NEGAMAX (Made things slower, actually).
//        long[] movesSorted = new long[moves.length];
//        int capture = 0;
//        int notCapture = moves.length-1;
//        for (long move : moves) {
//            if (!board.empty(MoveUtils.getEnd(move))) {
//                movesSorted[capture] = move;
//                capture++;
//            } else {
//                movesSorted[notCapture] = move;
//                notCapture--;
//            }
//        }
//
//        moves = movesSorted;

        boolean branchIsTooBoring = (depth > 4) ? (boring >= 3) : (depth > 2) ? (boring >= 2) : (boring >= 1);
        if (maximizing) {
            int maxEval = -1000000;

            for (long move : moves) {
                boolean interesting = moveIsInteresting(board, move);
                if (branchIsTooBoring && !interesting) continue;

                board.applyMove(move);

                int eval;
                if (depth > 1) {
                    eval = minimax(board, depth - 1, alpha, beta, false, timeWhenMustQuit, endgame, interesting ? 0 : boring+1);
                } else {
                    eval = board.getPoints(endgame);
                }

                board.undoMove();

                if (eval == TIMEOUT_RETURNVAL) return eval;
                eval = (eval - (eval>>6));
                if (eval > maxEval) maxEval = eval;
                if (eval > alpha) alpha = eval;

                if (beta <= alpha) break;
            }

            if (maxEval == -1000000) maxEval = board.getPoints(endgame);

            if (System.currentTimeMillis() > timeWhenMustQuit) return TIMEOUT_RETURNVAL;
            return maxEval;
        } else {
            int minEval = 1000000;
            for (long move : moves) {
                boolean interesting = moveIsInteresting(board, move);
                if (branchIsTooBoring && !interesting) continue;

                board.applyMove(move);

                int eval;
                if (depth > 1) {
                    eval = minimax(board, depth - 1, alpha, beta, true, timeWhenMustQuit, endgame, interesting ? 0 : boring+1);
                } else {
                    eval = board.getPoints(endgame);
                }

                board.undoMove();

                if (eval == TIMEOUT_RETURNVAL) return eval;
                eval = (eval - (eval>>6));
                if (eval < minEval) minEval = eval;
                if (eval < beta) beta = eval;
                if (beta <= alpha) break;
            }

            if (minEval == 1000000) minEval = board.getPoints(endgame);

            if (System.currentTimeMillis() > timeWhenMustQuit) return TIMEOUT_RETURNVAL;
            return minEval;
        }
    }

    private boolean moveIsInteresting(Board board, long move) {
        if (MoveUtils.isPawnPromotion(move)) return true;
        if (!board.empty(MoveUtils.getEnd(move))) return true;
        return false;
    }
}

interface MoveScoreUpdater {
    void updateScore(int newScore, int depth);
}
