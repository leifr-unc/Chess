package main.java.game;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ChessWidget extends JPanel implements MouseListener {

    private JBoard _board;
    private Game _game;

    private boolean _playerIsWhite;
    private Game.Type _type;

    private ChessWidget(boolean playerIsWhite, Game.Type type) {
        _playerIsWhite = playerIsWhite;
        _type = type;
        _board = new JBoard(_playerIsWhite);
        _game = new Game(_board, !_playerIsWhite, playerIsWhite, _type);

        setLayout(new BorderLayout());

        add(_board, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BorderLayout());
        bottom.setBackground(new Color(190/255f, 185/255f, 180/255f));

        JLabel label = new JLabel();
        bottom.add(label, BorderLayout.WEST);
        bottom.setPreferredSize(new Dimension(300, 20));
        label.setVerticalAlignment(SwingConstants.CENTER);

        JLabel undo = new JLabel("\u21A9");
        bottom.add(undo, BorderLayout.EAST);
        undo.addMouseListener(this);
        undo.setText(" U̲n̲d̲o̲ ");
        undo.setVerticalAlignment(SwingConstants.CENTER);
        undo.setHorizontalAlignment(SwingConstants.RIGHT);
        undo.setToolTipText("Undo your last move");
        // ADD THE TEXT HINT GIVER TO THE LAYOUT:
        add(bottom, BorderLayout.SOUTH);
        _game.setStatusLabel(label);
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
        mainFrame.setResizable(false);
        mainFrame.setVisible(true);
        chessWidget.startGame();
        return mainFrame;
    }

    private void startGame() {
        _game.start();
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        _game.undoMove();
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }
}
