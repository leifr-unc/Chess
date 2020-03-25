package moves;

import board.*;

public interface Move {
    Board applyMove(byte[] layout, BoardInfo info);
    int getStart();
    int getEnd();
    boolean isUserPawnPromotion();
    boolean isBotPawnPromotion();
}
