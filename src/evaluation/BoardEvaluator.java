package evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class BoardEvaluator {
    private static int[][] piecePoints;

    // Fill in piecepoints:
    public BoardEvaluator() {
        if (piecePoints == null) {
            try {
                piecePoints = getPiecePoints();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public int getPoints(byte[] board) {
        int output = 0;
        for (byte i = 0; i < board.length; i++) {
            output += piecePoints[board[i] + 6][i];
        }
        return output;
    }

    private static int[][] getPiecePoints() throws FileNotFoundException {
        File file = new File("data/piecePoints.txt");

        Scanner scan = new Scanner(file);
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
