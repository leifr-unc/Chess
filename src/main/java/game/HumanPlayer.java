package main.java.game;

public class HumanPlayer implements Player {
    private boolean _isWhite;
    private Game _game;

    public HumanPlayer(boolean isWhite, Game game) {
        _isWhite = isWhite;
        _game = game;
    }

    @Override
    public long getNextMove(long[] options) { return _game.askUserForMove(options); }

    @Override
    public boolean isHuman() { return true; }
}
