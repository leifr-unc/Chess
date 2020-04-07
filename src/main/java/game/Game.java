package main.java.game;

/*
 * Game class:
 * -> Stores the current state of the game, and the JBoard.
 * -> Is stored by the ChessWidget.
 * -> Manages the turns, and updates the UI when moves are made
 * -> The "Controller" in the Model-View-Controller design pattern.
 */

import main.java.board.Board;
import main.java.evaluation.AI;
import main.java.moves.MoveUtils;

import javax.swing.*;
import java.util.Stack;

public class Game implements Runnable {
    private JBoard _view;
    private Board _model;
    private Player _white, _black;
    private JLabel _statusLabel = null;
    private boolean _endgame;
    public enum Type {CHESS, SLAUGHTER_CHESS, HORDE}
    public Type _type;
    boolean _whiteBot;
    boolean _blackBot;
    private boolean _reset;

    private int _numUndos;
    public boolean _movesUndone;

    private static final long timeForAI = 5000;

    public Game(JBoard view, boolean whiteIsBot, boolean blackIsBot, Type type) {
        this(view, new Board(type), whiteIsBot, blackIsBot);
        _type = type;
    }

    private Game(JBoard view, Board model, boolean whiteIsBot, boolean blackIsBot) {
        _view = view;
        _view.setGame(this);
        _model = model;

        initializePlayers(whiteIsBot, blackIsBot);

        _whiteBot = whiteIsBot;
        _blackBot = blackIsBot;

        _view.setSpots(_model.getLayout());
    }

    private void initializePlayers(boolean whiteIsBot, boolean blackIsBot) {
        _white = whiteIsBot ? new AIPlayer(true, this) : new HumanPlayer(true, this);
        _black = blackIsBot ? new AIPlayer(false, this) : new HumanPlayer(false, this);
        _white = new AIPlayer(true, this);
//        _black = new AIPlayer(false, this);
//        _white = new HumanPlayer(true, this);
        _black = new HumanPlayer(false, this);
//        _white = new MonkeyPlayer(true, this);
//        _black = new MonkeyPlayer(false, this);
    }

    public void start() {
        Thread gameThread = new Thread(this);
        gameThread.start();
    }

    public long askUserForMove(long[] options) {
        return _view.askUserForMove(options);
    }

    @Override
    public void run() {
        // This is where the functionality of switching back and forth between players happens.
        _view.setSpots(_model.getLayout());

        boolean isWhiteTurn = true;
        for (int i = 0; (i < timeForAI/100); i++) {
            if (!_reset) break;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while(true) {
            Player turn = (isWhiteTurn ? _white : _black);
            long[] moveOptions = _model.getAllLegalMoves(isWhiteTurn, turn.isHuman());
            if (moveOptions.length == 0 && _model.kingIsInCheck(isWhiteTurn)) break;
            if (_model.isDraw()) break;

            if (_statusLabel != null) {
                if (turn.isHuman()) {
                    _statusLabel.setText((isWhiteTurn ? " White" : " Black") + " to move");
                } else {
                    _statusLabel.setText((isWhiteTurn ? " White" : " Black") + " is thinking . . .");
                }
            }

            _numUndos = turn.isHuman() ? 2 : 1;
            _movesUndone = false;
            long move = turn.getNextMove(moveOptions);

            if (_movesUndone) {
                if (!turn.isHuman()) isWhiteTurn = !isWhiteTurn;
                _endgame = _model.getIsEndgame();
                _view.removeAllHighlighting();

                continue;
            }
            if (_reset) {
                _reset = false;
                return;
            }
            _model.applyMove(move);
            _view.setSpots(_model.getLayout(), move);

            isWhiteTurn = !isWhiteTurn;
            _endgame = _model.getIsEndgame();
            System.out.println(getBoard());
        }
        try {
            if (_model.isCheckMated(true) && isWhiteTurn) {
                _statusLabel.setText(" Game Over - Black Won!");
            } else if (_model.isCheckMated(false) && !isWhiteTurn) {
                _statusLabel.setText(" Game Over - White Won!");
            } else {
                System.out.println(" Draw Game!");
            }
        } catch (NullPointerException e) {
            System.out.print("Game Over: ");
            if (_model.isCheckMated(true) && isWhiteTurn) {
                System.out.println("Black Won!");
            } else if (_model.isCheckMated(false) && !isWhiteTurn) {
                System.out.println("White Won!");
            } else {
                System.out.println("Draw Game!");
            }
        }
    }

    public void undoMove() {
        if (_model.getNumMoves() < _numUndos) return;
        for (int i = 0; i < _numUndos; i++) {
            _model.undoMove();
        }
        _view.setSpots(_model.getLayout());
        _numUndos = 2;
        _movesUndone = true;
    }

    public Board getBoard() {
        return _model.clone();
    }

    public long getAIMaxTime() {
        return timeForAI;
    }

    public boolean isEndgame() {
        return _endgame;
    }

    public void setStatusLabel(JLabel statusLabel) {
        _statusLabel = statusLabel;
    }
}
