package board;

public class BoardInfo {

    // Castling
    public boolean whiteKingHasMoved;
    public boolean whiteCastleA1HasMoved;
    public boolean whiteCastleH1HasMoved;

    public boolean blackKingHasMoved;
    public boolean blackCaslteA8HasMoved;
    public boolean blackCastleH8HasMoved;

    // King positions (keep updated)
    public byte whiteKingPos = 4;
    public byte blackKingPos = 60;

    // Progress (piece capture or pawn move)
    public byte numMovesSinceProgress = 0;

    // Double Pawn Moves
    public boolean lastMoveWasDoublePawnMove;
    public byte positionOfDoublePawn;

    public BoardInfo clone() {
        BoardInfo out = new BoardInfo();

        out.whiteKingHasMoved = whiteKingHasMoved;
        out.whiteCastleA1HasMoved = whiteCastleA1HasMoved;
        out.whiteCastleH1HasMoved = whiteCastleH1HasMoved;

        out.blackKingHasMoved = blackKingHasMoved;
        out.blackCaslteA8HasMoved = blackCaslteA8HasMoved;
        out.blackCastleH8HasMoved = blackCastleH8HasMoved;

        out.whiteKingPos = whiteKingPos;
        out.blackKingPos = blackKingPos;

        out.numMovesSinceProgress = numMovesSinceProgress;

        out.lastMoveWasDoublePawnMove = lastMoveWasDoublePawnMove;
        out.positionOfDoublePawn = positionOfDoublePawn;

        return out;
    }
}
