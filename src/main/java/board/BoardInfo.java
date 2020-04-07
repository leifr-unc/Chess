package main.java.board;

public abstract class BoardInfo {
    public static final int START_INFO = 0;

    public static boolean whiteKingHasMoved    (int info) { return ( info     & 1) != 0; }

    public static boolean whiteCastleA1HasMoved(int info) { return ((info>>1) & 1) != 0; }

    public static boolean whiteCastleH1HasMoved(int info) { return ((info>>2) & 1) != 0; }

    public static boolean blackKingHasMoved    (int info) { return ((info>>3) & 1) != 0; }

    public static boolean blackCastleA7HasMoved(int info) { return ((info>>4) & 1) != 0; }

    public static boolean blackCastleH7HasMoved(int info) { return ((info>>5) & 1) != 0; }

    public static byte numMovesSinceProgress (int info) { return (byte) ((info>>6) & 255); }

    public static boolean lastMoveWasDoublePawnMove (int info) { return ((info>>14) & 1) != 0; }

    public static byte positionOfDoublePawn (int info) { return (byte) ((info>>15) &63); }


    public static int setWhiteKingHasMoved (int info, boolean set) { return setBit(info, set, 0); }

    public static int setWhiteCastleA1HasMoved(int info, boolean set) { return setBit(info, set, 1); }

    public static int setWhiteCastleH1HasMoved(int info, boolean set) { return setBit(info, set, 2); }

    public static int setBlackKingHasMoved    (int info, boolean set) { return setBit(info, set, 3); }

    public static int setBlackCastleA7HasMoved(int info, boolean set) { return setBit(info, set, 4); }

    public static int setBlackCastleH7HasMoved(int info, boolean set) { return setBit(info, set, 5); }

    public static int setNumMovesSinceProgress (int info, int set) { return setBits(info, set, 6, 8); }

    public static int setLastMoveWasDoublePawnMove (int info, boolean set) { return setBit(info, set, 14); }

    public static int setPositionOfDoublePawn (int info, int set) { return setBits(info, set, 15, 6); }

    private static int setBit (int info, boolean set, int offset) {
        if (set) {
            info |= (1 << offset);
        } else {
            info &= ~(1 << offset);
        }
        return info;
    }

    private static int setBits (int info, int set, int offset, int numBits) {
        return ((info>>>(offset+numBits))<<(offset+numBits)) | (set << offset) | ((info<<(32-offset))>>>(32-offset));
    }
}
