package main.java.game;

import main.java.evaluation.AI;


public class AIPlayer implements Player {
    private boolean _isWhite;
    private Game _game;
    private AI _ai;

    public AIPlayer(boolean isWhite, Game game) {
        _isWhite = isWhite;
        _game = game;
        _ai = new AI();
    }

    @Override
    public long getNextMove(long[] options) {
        return _ai.getBestMove(_game.getBoard(), _isWhite, _game.getAIMaxTime(), options, _game.isEndgame());
    }

    @Override
    public boolean isHuman() { return false; }
}
