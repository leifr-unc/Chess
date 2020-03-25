import board.Board;
import game.ChessWidget;
import moves.NormalMove;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        ChessWidget.makeFrame(Math.random() < 0.5);
    }
}
