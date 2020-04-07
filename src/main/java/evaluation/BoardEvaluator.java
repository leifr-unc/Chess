package main.java.evaluation;

import main.java.board.Board;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class BoardEvaluator {
    private static int[][] piecePoints;
    private static int[][] piecePointsEnd;

    // Fill in piecepoints:
    public BoardEvaluator() {
        if (piecePoints == null) {
            try {
                piecePoints = getPiecePoints("data/piecePoints.txt");
                piecePointsEnd = getPiecePoints("data/piecePointsEndgame.txt");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getPoints(Board board, boolean endgame) {
        long protectedWhite = 0;
        long protectedBlack = 0;

        int score = 0;
        if (endgame) {
            for (byte i = 0; i < 64; i++) {
                if (!board.empty(i)) score += piecePointsEnd[board.pieceAt(i) + 6][i];
            }
        } else {
            for (byte i = 0; i < 64; i++) {
                if (!board.empty(i)) score += piecePoints[board.pieceAt(i) + 6][i];
            }
        }
        return score;
    }

    private static int[][] getPiecePoints(String path) {
        Scanner scan = null;
        try {
            scan = new Scanner(new File(path)/*BoardEvaluator.class.getClassLoader().getResourceAsStream(path)*/);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int[][] output = new int[13][64];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < output[i].length; j++) {
                int nextInt = scan.nextInt();
                output[i][j] = -1 * nextInt;
                int i1 = 8 * ((63 - j) / 8) + j % 8;
                output[12-i][i1] = nextInt;
            }
        }

        return output;
    }
}
