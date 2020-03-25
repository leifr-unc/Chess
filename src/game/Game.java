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
    JBoard _view;
    Board _model;
    Player _white, _black;

    public Game(JBoard view) {
        this(view, new Board());
    }

    public Game(JBoard view, Board model) {
        _view = view;
        _model = model;
        _black = new AIPlayer(false, this);
        _white = new HumanPlayer(true, this);

        _view.setSpots(_model.getLayout());
        _view.setPreferredSize(new Dimension(560, 560));
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
            _view.setSpots(_model.getLayout());
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
}
