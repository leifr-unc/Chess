package moves;

import board.Board;
import board.BoardInfo;

public class PawnPromotionMove implements Move {
    private int _from;
    private int _to;
    private PawnPromotionChooser _chooser;
    private byte _fixedChoice;

    public PawnPromotionMove(int from, int to, PawnPromotionChooser chooser) {
        _from = from;
        _to = to;
        _chooser = chooser;
        _fixedChoice = -1;
    }

    public PawnPromotionMove(int from, int to, byte fixedChoice) {
        _from = from;
        _to = to;
        _chooser = null;
        _fixedChoice = fixedChoice;
    }

    @Override
    public Board applyMove(byte[] layout, BoardInfo info) {
        layout = layout.clone();
        info = info.clone();

        int choice = (_fixedChoice == -1) ? ((layout[_from] > 0 ? 1 : -1) * _chooser.getChoice()) : _fixedChoice;
        // Make the move
        layout[_to] = (byte) choice; // Move piece
        layout[_from] = 0; // Replace _from with 0.

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
        return _fixedChoice == -1;
    }

    @Override
    public boolean isBotPawnPromotion() {
        return _fixedChoice != -1;
    }

    @Override
    public String toString() {
        return getStart() + " to " + getEnd() + ", promoting the piece.";
    }
}
