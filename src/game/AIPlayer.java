package game;

import board.Board;
import evaluation.MoveGenerator;
import moves.Move;


public class AIPlayer implements Player {
    private boolean _isWhite;
    private Game _game;

    public AIPlayer(boolean isWhite, Game game) {
        _isWhite = isWhite;
        _game = game;
    }

    @Override
    public Move getNextMove() {
        return new MoveGenerator().getBestMove(_game.getBoard(), _isWhite);
    }

    @Override
    public boolean getIsWhite() {
        return _isWhite;
    }
}
