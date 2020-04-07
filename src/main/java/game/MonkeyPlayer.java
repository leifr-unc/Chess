package main.java.game;

public class MonkeyPlayer implements Player {
    boolean _white;
    Game _game;

    public MonkeyPlayer(boolean isWhite, Game game) {
        _white = isWhite;
        _game = game;
    }
    @Override
    public long getNextMove(long[] options) {
        long[] out = _game.getBoard().getAllLegalMoves(_white, false);
        try {
            Thread.sleep(_game.getAIMaxTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return out[((int) (Math.random()*out.length))];
    }

    @Override
    public boolean isHuman() {
        return false;
    }
}
