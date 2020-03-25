package moves;

import board.*;

public class NormalMove implements Move {
    private byte _from;
    private byte _to;

    public NormalMove(byte from, byte to) {
        _from = from;
        _to = to;
    }

    public NormalMove(int from, int to) {
        _from = (byte) from;
        _to = (byte) to;
    }

    @Override
    public Board applyMove(byte[] layout, BoardInfo info) {
        layout = layout.clone();
        info = info.clone();

        // Make the move
        byte oldTo = layout[_to]; // Save piece being captured
        layout[_to] = layout[_from]; // Capture that piece.
        layout[_from] = 0; // Replace _from with 0.

        // Adjust boardInfo.
        adjustBoardInfo(info, layout[_to], oldTo);
        return new Board(layout, info);
    }

    public void adjustBoardInfo(BoardInfo info, byte newTo, byte oldTo) {
        if (_from == 0) info.whiteCastleA1HasMoved = true;
        if (_from == 4) info.whiteKingHasMoved = true;
        if (_from == 7) info.whiteCastleH1HasMoved = true;
        if (_from == 56) info.blackCaslteA8HasMoved = true;
        if (_from == 60) info.blackKingHasMoved = true;
        if (_from == 63) info.blackCastleH8HasMoved = true;

        if (newTo == 6) info.whiteKingPos = _to;
        if (newTo == -6) info.blackKingPos = _to;

        info.numMovesSinceProgress++;
        if (oldTo != 0 || newTo == 1 || newTo == -1) info.numMovesSinceProgress = 0;

        if ((newTo == 1 && _to - _from == 16) ||
                (newTo == -1 && _to - _from == -16)) {
            info.lastMoveWasDoublePawnMove = true;
            info.positionOfDoublePawn = _to;
        } else {
            info.lastMoveWasDoublePawnMove = false;
        }
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
