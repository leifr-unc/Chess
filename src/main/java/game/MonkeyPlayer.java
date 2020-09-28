package game;

import java.util.List;

public class MonkeyPlayer implements Player {
    private boolean _white;
    private Game _game;

    public MonkeyPlayer(boolean isWhite, Game game) {
        _white = isWhite;
        _game = game;
    }

    @Override
    public long getNextMove(List<Long> options) {
        List<Long> out = _game.getBoard().getAllLegalMoves(_white, false);
        try {
            Thread.sleep(_game.getAIMaxTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return out.get((int) (Math.random()*out.size()));
    }

    @Override
    public boolean isHuman() {
        return false;
    }
}
