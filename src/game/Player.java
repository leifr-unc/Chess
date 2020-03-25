package game;

import moves.Move;

public interface Player {
    Move getNextMove();
    boolean getIsWhite();
}
