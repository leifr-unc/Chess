package moves;

import board.Board;
import board.BoardInfo;

public class EnPassantMove implements Move {
    private int _from;
    private int _to;
    private int _capture;

    public EnPassantMove(int from, int to) {
        _from = from;
        _to = to;
        _capture = _to + ((_to > 31) ? -8 : 8);
    }

    @Override
    public Board applyMove(byte[] layout, BoardInfo info) {
        layout = layout.clone();
        info = info.clone();

        // Make the move
        layout[_to] = layout[_from];
        layout[_from] = 0;
        layout[_capture] = 0;

        // Adjust boardInfo.
        info.numMovesSinceProgress = 0;
        info.lastMoveWasDoublePawnMove = false;

        return new Board(layout, info);
    }

    @Override
    public int getStart() {
        return _from;
    }

    @Override
    public int getEnd() {
        return _to;
    }


    @Override
    public boolean isUserPawnPromotion() {
        return false;
    }

    @Override
    public boolean isBotPawnPromotion() {
        return false;
    }

    @Override
    public String toString() {
        return getStart() + " to " + getEnd();
    }
}
