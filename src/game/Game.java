package game;

/*
 * Game class:
 * -> Stores the current state of the game, and the JBoard.
 * -> Is stored by the ChessWidget.
 * -> Manages the turns, and updates the UI when moves are made
 * -> The "Controller" in the Model-View-Controller design pattern.
 */

import board.Board;
import evaluation.AI;
import moves.MoveUtils;

import java.util.Stack;

public class Game implements Runnable {
    private JBoard _view;
    private Board _model;
    private Player _white, _black;
    private Stack<Long> _moves;
    private boolean _endgame;
    public enum Type {CHESS, SLAUGHTER_CHESS, HORDE}

    private static final long timeForAI = 5000;

    public Game(JBoard view, boolean whiteIsBot, boolean blackIsBot, Type type) {
        this(view, new Board(type), whiteIsBot, blackIsBot);
    }

    private Game(JBoard view, Board model, boolean whiteIsBot, boolean blackIsBot) {
        System.out.println(new Board(Type.CHESS));
        _view = view;
        _model = model;
        _moves = new Stack<>();
        _white = whiteIsBot ? new AIPlayer(true, this) : new HumanPlayer(true, this);
        _black = blackIsBot ? new AIPlayer(false, this) : new HumanPlayer(false, this);
//        _white = new AIPlayer(true, this);
//        _black = new AIPlayer(false, this);
//        _white = new HumanPlayer(true, this);
//        _black = new HumanPlayer(false, this);

        _view.setSpots(_model.getLayout());
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
        while(true) {
            Player turn = (isWhiteTurn ? _white : _black);
            long[] moveOptions = _model.getAllLegalMoves(isWhiteTurn, turn.isHuman());
            if (moveOptions.length == 0 && _model.kingIsInCheck(isWhiteTurn)) break;

            System.out.println((isWhiteTurn ? "White" : "Black") + " to move");
            long move = turn.getNextMove(moveOptions);

            _model.applyMove(move);
            _view.setSpots(_model.getLayout(), move);

            isWhiteTurn = !isWhiteTurn;
            _endgame = _model.getIsEndgame();
            System.out.println(getBoard());
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
        return _model.clone();
    }

    public long getAIMaxTime() {
        return timeForAI;
    }

    public boolean isEndgame() {
        return _endgame;
    }
}
