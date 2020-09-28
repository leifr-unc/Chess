package moves;

import board.Board;
import board.BoardInfo;

import javax.swing.*;

/*
 * Contains tools for working with moves stored as longs.
 * -> Allows reading and writing moves.
 */

/* Move format:
 * [next two lines go in here][A][B][CD][E][F][G][old boardinfo (22 bits)][5 unused bits]
 * [FROM (6 bits)][TO (6 bits)][piece at TO (3 bits)] ...
 * [FROM_1 (6 bits)][TO_1 (6 bits)][piece at TO_1 (3 bits)]
 * -> A: if _layout[to_1] should just be wiped, instead of having from_1 given to it.
 * -> B: if this is a pawn promotion.
 * -> C: if the pawn promotion is for the user to reply to.
 * -> DE: if B is 0, then this represents the choice: C = bishop, D = rook, CD = queen, none = knight.
 * -> F: if this is a castle move.
 * -> G: if the side moving is white
 * -> H: if the move counts as "progress"
 * -> 4 remaining bits are never used.
 */
public abstract class MoveUtils {

    public static long applyMove(Board board, long move) {
        int from = (int) ((move) & ((1L<<6) - 1));
        int to = (int) ((move>>>6) & ((1L<<6) - 1));
        int from1 = (int) ((move>>>15) & ((1L<<6) - 1));
        int to1 = (int) ((move>>>21) & ((1L<<6) - 1));

        // Make the move

        // Save pieces captured:
        int oldTo = board.pieceAt(to);
        move = setBits(move, oldTo < 0 ? -1*oldTo : oldTo, 12, 3);
        int oldTo1 = board.pieceAt(to1);
        move = setBits(move, oldTo1 < 0 ? -1*oldTo1 : oldTo1, 27, 3);

        // Put piece from "FROM" into "TO"
        int multChoiceBy = board.white(from) ? 1 : -1; // For getting the correct color piece.

        if (isUserPawnPromotion(move)) {
            board.setPiece(to, (multChoiceBy * (askUserForPawnChoice())));
        } else if (isBotPawnPromotion(move)){
            // Is bishop?
            if (((move>>33) & 1) != 0) {
                // Is also rook?
                if (((move>>34) & 1) != 0) {
                    // Queen:
                    board.setPiece(to, (multChoiceBy * 5));
                } else {
                    // Bishop:
                    board.setPiece(to, (multChoiceBy * 5));
                }
            } else {
                // Is rook?
                if (((move>>34) & 1) != 0) {
                    // Rook
                    board.setPiece(to, (multChoiceBy * 4));
                } else {
                    // Knight
                    board.setPiece(to, (multChoiceBy * 2));
                }
            }
        } else {
            // Move is not pawn promotion.
            board.copy(from, to);
        }

        board.wipe(from);
        if (to1 != from1) {
            if (((move>>30) & 1) != 0) {
                board.wipe(to1);
            } else {
                board.setPiece(to1, ((move >> 30) & 1) != 0 ? 0 : board.pieceAt(from1));
                board.wipe(from1);
            }
        }

        // Save, and then adjust boardInfo.
        move = move | (((long) board._info) << 38);

        if (((move>>34) & 1) != 0) board._info = adjustBoardInfoCastle(board._info, ((move>>35) & 1) != 0);
        else board._info = adjustBoardInfo(board._info, board.pieceAt(to), oldTo, from, to);

        if (((move>>31) & 1) != 0) board._info = BoardInfo.setNumMovesSinceProgress(board._info, 0);

        return move;
    }

    public static void undoMove(Board board, long move) {
        int from = (int) ((move) & ((1L <<6) - 1));
        int to = (int) ((move>>>6) & ((1L<<6) - 1));
        int from1 = (int) ((move>>>15) & ((1L<<6) - 1));
        int to1 = (int) ((move>>>21) & ((1L<<6) - 1));
        int savedTo = (int) ((move>>>12) & ((1L<<3) - 1));
        int savedTo1 = (int) ((move>>>27) & ((1L<<3) - 1));
        boolean isWhite = board.white(to);

        if (isPawnPromotion(move)) {
            board.setPiece(to, isWhite ? 1 : -1);
        }

        board.copy(to, from);
        board.setPiece(to, (isWhite ? -1 : 1) * savedTo);
        if (from1 != to1) {
            if (((move>>30) & 1) == 0) {
                board.copy(to1, from1);
            }
            board.setPiece(to1, (isWhite ? -1 : 1) * savedTo1);
        }

        board._info = (int) (move>>38);
    }

    private static int adjustBoardInfoCastle(int info, boolean white) {
        info = BoardInfo.setLastMoveWasDoublePawnMove(info, false);
        if (white) {
            info = BoardInfo.setWhiteCastleA1HasMoved(info, true);
            info = BoardInfo.setWhiteCastleH1HasMoved(info, true);
            info = BoardInfo.setWhiteKingHasMoved(info, true);
        } else {
            info = BoardInfo.setBlackCastleA7HasMoved(info, true);
            info = BoardInfo.setBlackCastleH7HasMoved(info, true);
            info = BoardInfo.setBlackKingHasMoved(info, true);
        }
        return BoardInfo.setNumMovesSinceProgress(info, BoardInfo.numMovesSinceProgress(info) + 1);
    }

    private static int adjustBoardInfo(int info, int newTo, int oldTo, int from, int to) {
        if (from == 0 || to == 0) info = BoardInfo.setWhiteCastleA1HasMoved(info, true);
        if (from == 4) info = BoardInfo.setWhiteKingHasMoved(info, true);
        if (from == 7 || to == 7) info = BoardInfo.setWhiteCastleH1HasMoved(info, true);
        if (from == 56 || to == 56) info = BoardInfo.setBlackCastleA7HasMoved(info, true);
        if (from == 60) info = BoardInfo.setBlackKingHasMoved(info, true);
        if (from == 63 || to == 63) info = BoardInfo.setBlackCastleH7HasMoved(info, true);

        info = BoardInfo.setNumMovesSinceProgress(info, BoardInfo.numMovesSinceProgress(info) + 1);

        if (oldTo != 0 || newTo == 1 || newTo == -1) info = BoardInfo.setNumMovesSinceProgress(info, 0);

        if ((newTo == 1 && to - from == 16) ||
                (newTo == -1 && to - from == -16)) {
            info = BoardInfo.setLastMoveWasDoublePawnMove(info, true);
            info = BoardInfo.setPositionOfDoublePawn(info, to);
        } else {
            info = BoardInfo.setLastMoveWasDoublePawnMove(info, false);
        }
        return info;
    }

    public static long generateMove(int from, int to, Board board, int pawnChoice /* 0 = user defined, 2 = knight, 3 = bishop, etc. */) {
        boolean isWhite = board.white(from);
        long move = 0;
        move = setBit(move, isWhite, 36);
        move = setBits(move, from, 0, 6);
        move = setBits(move, to, 6, 6);

        boolean isEnPassant = board.pawn(from) && (isWhite ? (!board.black(to) && from/8 == 4) : (!board.white(to) && from/8 == 3)) && (to-from) % 8 != 0;
        move = setBit(move, isEnPassant || board.pawn(from) || (isWhite ? board.black(to) : board.white(to)), 37);

        // En Passant
        if (isEnPassant) {
            move = setBit(move, true, 30);
            move = setBits(move, (isWhite ? to-8 : to+8), 21, 6);
        }

        // Pawn Promotion
        if (board.pawn(from) && (isWhite ? to/8 == 7 : to/8 == 0)) {
//            throw new RuntimeException("Generating a pawn promotion from " + from + " to " + to + "\n" + board.toString());
            move = setBit(move, true, 31);
            move = setBit(move, pawnChoice == 0, 32);
            if (pawnChoice == 3 || pawnChoice == 5) {
                // Set bishop to be true.
                move = setBit(move, true, 33);
            }
            if (pawnChoice == 4 || pawnChoice == 5) {
                // Set rook to be true
                move = setBit(move, true, 34);
            }
        }

        // Castling
        if (board.king(from) && (from - to == 2 || from-to == -2)) {
            move = setBit(move, true, 35);
            int from1;
            int to1;
            if (to % 8 == 2) {
                // Left side:
                from1 = to-2;
                to1 = to+1;
            } else {
                // Right side:
                from1 = to+1;
                to1 = to-1;
            }
            move = setBits(move, from1, 15, 6);
            move = setBits(move, to1, 21, 6);
        }

        return move;
    }

    public static int getStart(long move) {
        return (int) ((move) & ((1L<<6) - 1));
    }

    public static int getEnd(long move) {
        return (int) ((move>>>6) & ((1L<<6) - 1));
    }

    public static boolean isUserPawnPromotion(long move) {
        return ((move>>31) & 1L) != 0 && ((move>>32) & 1L) != 0;
    }

    private static boolean isBotPawnPromotion(long move) {
        return ((move>>31) & 1L) != 0 && ((move>>32) & 1L) == 0;
    }

    public static boolean isPawnPromotion(long move) {
        return (move>>31 & 1L) != 0;
    }

    public static boolean equal(long move1, long move2) {
        return getStart(move1) == getStart(move2) && getEnd(move1) == getEnd(move2);
    }

    public static boolean equal(long move1, long move2, long move3) {
        return equal(move1, move2) && equal(move2, move3);
    }

    private static long setBit (long bits, boolean set, int offset) {
        if (set) {
            bits |= (1L << offset);
        } else {
            bits &= ~(1L << offset);
        }
        return bits;
    }

    private static long setBits (long bits, long set, int offset, int numBits) {
        return ((bits>>>(offset+numBits))<<(offset+numBits)) | (set << offset) | ((bits<<(32-offset))>>>(32-offset));
    }

    private static int askUserForPawnChoice() {
        String[] options = new String[] {"Queen", "Rook", "Bishop", "Knight"};
        int response = JOptionPane.showOptionDialog(null, "Choose new Piece", "Pawn Promotion",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);
        return 5 - response;
    }
}
