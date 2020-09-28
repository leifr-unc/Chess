package game;

import java.util.List;

public class HumanPlayer implements Player {
    private boolean _isWhite;
    private Game _game;

    public HumanPlayer(boolean isWhite, Game game) {
        _isWhite = isWhite;
        _game = game;
    }

    @Override
    public long getNextMove(List<Long> options) { return _game.askUserForMove(options); }

    @Override
    public boolean isHuman() { return true; }
}
