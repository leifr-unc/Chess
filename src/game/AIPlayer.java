package game;

import evaluation.AI;


public class AIPlayer implements Player {
    private boolean _isWhite;
    private Game _game;
    private AI _generator;

    public AIPlayer(boolean isWhite, Game game) {
        _isWhite = isWhite;
        _game = game;
        _generator = new AI();
    }

    @Override
    public long getNextMove(long[] options) {
        return _generator.getBestMove(_game.getBoard(), _isWhite, _game.getAIMaxTime(), options, _game.isEndgame());
    }

    @Override
    public boolean getIsWhite() { return _isWhite; }

    @Override
    public boolean isHuman() { return false; }
}
