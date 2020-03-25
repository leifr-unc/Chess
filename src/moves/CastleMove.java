package moves;

import board.Board;
import board.BoardInfo;

public class CastleMove implements Move {
    private byte _from, _to;

    public CastleMove(byte from, byte to) {
        _from = from;
        _to = to;
    }

    @Override
    public Board applyMove(byte[] layout, BoardInfo info) {
        boolean white = layout[_from] > 0;

        layout = layout.clone();
        info = info.clone();
        layout[_to] = layout[_from];
        layout[_from] = 0;
        int fromCastle = (_to % 8) == 2 ? _to - 2 : _to + 1;
        int toCastle = (_to % 8) == 2 ? _to + 1 : _to - 1;
        layout[toCastle] = layout[fromCastle];
        layout[fromCastle] = 0;

        info.lastMoveWasDoublePawnMove = false;
        if (white) {
            info.whiteCastleA1HasMoved = true;
            info.whiteCastleH1HasMoved = true;
            info.whiteKingHasMoved = true;
            info.whiteKingPos = _to;
            info.numMovesSinceProgress++;
        } else {
            info.blackCaslteA8HasMoved = true;
            info.blackCastleH8HasMoved = true;
            info.blackKingHasMoved = true;
            info.blackKingPos = _to;
            info.numMovesSinceProgress++;
        }
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
        return getStart() + " to " + getEnd() + ", castling.";
    }
}
