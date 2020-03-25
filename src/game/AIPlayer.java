package game;

import board.Board;
import evaluation.OldAI;
import moves.Move;


public class AIPlayer implements Player {
    private boolean _isWhite;
    private Game _game;
    private OldAI _generator;

    public AIPlayer(boolean isWhite, Game game) {
        _isWhite = isWhite;
        _game = game;
        _generator = new OldAI();
    }

    @Override
    public Move getNextMove() {
        return _generator.getBestMove(_game.getBoard(), _isWhite, _game.getAIMaxTime());
    }

    @Override
    public boolean getIsWhite() {
        return _isWhite;
    }
}
