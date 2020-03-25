package game;

import javax.swing.*;
import java.awt.*;

public class ChessWidget extends JPanel {

    private JBoard _board;
    private Game _game;

    private ChessWidget() {
        _board = new JBoard();
        _game = new Game(_board);

        setLayout(new BorderLayout());
        add(_board, BorderLayout.CENTER);
    }

    public static JFrame makeFrame() {
        JFrame mainFrame = new JFrame();
        mainFrame.setTitle("Chess");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        mainFrame.setContentPane(topPanel);

        ChessWidget chessWidget = new ChessWidget();
        topPanel.add(chessWidget, BorderLayout.CENTER);

        mainFrame.pack();
        mainFrame.setVisible(true);
        chessWidget.startGame();
        return mainFrame;
    }

    private void startGame() {
        _game.start();
    }
}
