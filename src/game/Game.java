package game;

/*
 * Game class:
 * -> Stores the current state of the game, and the JBoard.
 * -> Is stored by the ChessWidget.
 * -> Manages the turns, and updates the UI when moves are made
 * -> The "Controller" in the Model-View-Controller design pattern.
 */

import board.Board;
import moves.Move;

import javax.swing.*;
import java.awt.*;

public class Game implements Runnable {
    private JBoard _view;
    private Board _model;
    private Player _white, _black;
    public enum Type {CHESS, SLAUGHTER_CHESS, HORDE}

    private static final long timeForAI = 5000;

    public Game(JBoard view, boolean whiteIsBot, boolean blackIsBot, Type type) {
        this(view, new Board(type), whiteIsBot, blackIsBot);
    }

    private Game(JBoard view, Board model, boolean whiteIsBot, boolean blackIsBot) {
        _view = view;
        _model = model;
        _white = whiteIsBot ? new AIPlayer(true, this) : new HumanPlayer(true, this);
        _black = blackIsBot ? new AIPlayer(false, this) : new HumanPlayer(false, this);
//        _white = new AIPlayer(true, this);
//        _black = new AIPlayer(false, this);

        _view.setSpots(_model.getLayout());
    }

    public void start() {
        Thread gameThread = new Thread(this);
        gameThread.start();
    }

    public Move askUserForMove(boolean isWhite) {
        return _view.askUserForMove(_model.getAllLegalMoves(isWhite, _view.getPawnChooser()));
    }

    @Override
    public void run() {
        // This is where the functionality of switching back and forth between players happens.
        _view.setSpots(_model.getLayout());
        boolean isWhiteTurn = true;
        while(!_model.playerCannotMove(isWhiteTurn)) {
            System.out.println((isWhiteTurn ? "White" : "Black") + " to move");
            Player turn = (isWhiteTurn ? _white : _black);

            Move move = turn.getNextMove();
            System.out.println(move);

            _model = _model.applyMove(move);
            _view.setSpots(_model.getLayout(), move);
            isWhiteTurn = !isWhiteTurn;
        }

        System.out.print("Game Over: ");
        if (_model.isCheckMated(true) && isWhiteTurn) {
            System.out.println("Black Won!");
        } else if (_model.isCheckMated(false) && !isWhiteTurn) {
            System.out.println("White Won!");
        } else {
            System.out.println("Draw Game!");
        }
    }

    public Board getBoard() {
        return _model;
    }

    private void sleep(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException ignored) {}
    }

    public long getAIMaxTime() {
        return timeForAI;
    }
}
