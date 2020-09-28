package game;

import java.util.List;

public interface Player {
    long getNextMove(List<Long> options);
    boolean isHuman();
}
