package evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class BoardEvaluator {
    public static int[][] piecePoints;

    // Fill in piecepoints:
    static {
        try {
            piecePoints = getPiecePoints();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static int getPoints(byte[] board) {
        int output = 0;
        for (byte i = 0; i < board.length; i++) {
            output += piecePoints[board[i] + 6][i];
        }
        return output;
    }

    private static int[][] getPiecePoints() throws FileNotFoundException {
        File file = new File("piecePoints.txt");

        Scanner scan = new Scanner(file);
        int[][] output = new int[13][64];
        for (int i = 0; i < output.length; i++) {
            for (int j = 0; j < output[i].length; j++) {
                output[i][j] = scan.nextInt();
            }
        }
        return output;
    }
}
