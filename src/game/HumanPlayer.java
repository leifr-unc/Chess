package game;

import moves.Move;

public class HumanPlayer implements Player {
    private boolean _isWhite;
    private Game _game;

    public HumanPlayer(boolean isWhite, Game game) {
        _isWhite = isWhite;
        _game = game;
    }

    @Override
    public Move getNextMove() {
        return _game.askUserForMove(_isWhite);
    }

    @Override
    public boolean getIsWhite() {
        return _isWhite;
    }
}
