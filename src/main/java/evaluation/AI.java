package evaluation;

import board.Board;
import moves.MoveUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AI {
    private static int TIMEOUT_RETURNVAL = 123456789;

    public long getBestMove(Board board, boolean isWhite, long maxTime, List<Long> allMoves, boolean endgame) {
        List<MoveScoreUpdater> updaters = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        int[] allScores = new int[allMoves.size()];
        int[] depths = new int[allMoves.size()];
        long time = System.currentTimeMillis();

        AtomicInteger maxDepthSoFar = new AtomicInteger();
        for (int i = 0; i < allMoves.size(); i++) {
            if (MoveUtils.isUserPawnPromotion(allMoves.get(i))) continue;
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
            cloned.applyMove(allMoves.get(i));
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
            if (MoveUtils.isUserPawnPromotion(allMoves.get(i))) continue;
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
        return bestMoves.get(random);
    }

    private Thread generateMinimaxThread(long timeMilis, Board board, boolean isNowWhiteTurn, MoveScoreUpdater updater, boolean endgame) {
        return new Thread(() -> {
            long timeStart = System.currentTimeMillis();
            int depth = 0;
            Map<Integer, Integer> scoresFromLastRun = new HashMap<>();
            while (System.currentTimeMillis() < timeStart + timeMilis) {
                depth++;
                Map<Integer, Integer> next = new HashMap<>();
                int score = minimax(board, depth, -1000000, 1000000, isNowWhiteTurn,timeStart + timeMilis - 200, endgame);
                scoresFromLastRun = next;
                if (score != TIMEOUT_RETURNVAL) updater.updateScore(score, depth+1);
            }
        });
    }

    private int minimax(Board board, int depth, int alpha, int beta, boolean maximizing, long timeWhenMustQuit, boolean endgame) {
        List<Long> moves = board.getAllLegalMoves(maximizing, false);

        if (moves.size() == 0) {
            if (board.kingIsInCheck(maximizing)) {
                return (maximizing ? -100000 : 100000); // Checkmate, avoid at all costs.
            } else {
                return 0; // Draw.
            }
        }

        if (maximizing) {
            int maxEval = -1000000;
            for (long move : moves) {
                board.applyMove(move);

                int eval;
                if (depth > 1) {
                    eval = minimax(board, depth - 1, alpha, beta, false, timeWhenMustQuit, endgame);
                } else {
                    eval = board.getPoints(endgame);
                }

                board.undoMove();

                if (eval == TIMEOUT_RETURNVAL) return eval;
                eval = (eval - (eval>>6));

                if (eval > maxEval) maxEval = eval;
                if (eval > alpha) alpha = eval;
                if (alpha > beta) break;
            }

            if (maxEval == -1000000) maxEval = board.getPoints(endgame);

            if (depth > 3 && System.currentTimeMillis() > timeWhenMustQuit) return TIMEOUT_RETURNVAL;
            return maxEval;
        } else {
            int minEval = 1000000;
            for (long move : moves) {
                board.applyMove(move);

                int eval;
                if (depth > 1) {
                    eval = minimax(board, depth - 1, alpha, beta, true, timeWhenMustQuit, endgame);
                } else {
                    eval = board.getPoints(endgame);
                }

                board.undoMove();

                if (eval == TIMEOUT_RETURNVAL) return eval;
                eval = (eval - (eval>>6));

                if (eval < minEval) minEval = eval;
                if (eval < beta) beta = eval;
                if (alpha > beta) break;
            }

            if (minEval == 1000000) minEval = board.getPoints(endgame);

            if (depth > 3 && System.currentTimeMillis() > timeWhenMustQuit) return TIMEOUT_RETURNVAL;
            return minEval;
        }
    }

    private boolean moveIsInteresting(Board board, long move) {
        if (MoveUtils.isPawnPromotion(move)) return true;
        if (!board.empty(MoveUtils.getEnd(move))) return true;
        return false;
    }

    private boolean boardIsInteresting(Board board, boolean whiteIsNext, boolean endgame) {
        // Do null move: if score doesn't change much, return false.
        if (board.kingIsInCheck(whiteIsNext) || board.kingIsInCheck(!whiteIsNext)) return true;

        List<Long> moves = board.getAllLegalMoves(!whiteIsNext, false);

        if (moves.size() == 0) return true;

        int returnVal = whiteIsNext ? 1000000000 : -1000000000;
        for (long move : moves) {
            board.applyMove(move);

            List<Long> reactions = board.getAllLegalMoves(whiteIsNext, false);
            if (reactions.size() == 0) {
                board.undoMove();
                return true;
            }
            int extreme = whiteIsNext ? -1000000000 : 1000000000;
            for(long reaction : reactions) {
                board.applyMove(reaction);

                int eval = board.getPoints(endgame);
                if ((whiteIsNext && (eval > extreme)) || (!whiteIsNext && (eval < extreme))) extreme = eval;

                board.undoMove();
            }
            if ((whiteIsNext && (extreme < returnVal)) || (!whiteIsNext && (extreme > returnVal))) returnVal = extreme;

            board.undoMove();
        }
        int offset = board.getPoints(endgame)- returnVal;
        if (offset < 0) offset = 0 - offset;
        return offset > 50;
    }
}

interface MoveScoreUpdater {
    void updateScore(int newScore, int depth);
}
