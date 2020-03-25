package game;

import javax.swing.*;
import java.awt.*;

public class ChessWidget extends JPanel {

    private JBoard _board;
    private Game _game;

    private ChessWidget(boolean playerIsWhite, Game.Type type) {
        _board = new JBoard(playerIsWhite);
        _game = new Game(_board, !playerIsWhite, playerIsWhite, type);

        setLayout(new BorderLayout());
        add(_board, BorderLayout.CENTER);
    }

    public static JFrame makeFrame(boolean playerIsWhite, Game.Type type) {
        JFrame mainFrame = new JFrame();
        mainFrame.setTitle("Chess");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        mainFrame.setContentPane(topPanel);

        ChessWidget chessWidget = new ChessWidget(playerIsWhite, type);
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
