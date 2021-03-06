package board;

import evaluation.BoardEvaluator;
import game.Game;
import moves.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

/*
 * Board class:
 * -> The "Model" part of the Model-View-Controller design pattern.
 * -> Stores a chessboard's current state and houses relevant methods.
 */

public class Board {

    private static BoardEvaluator evaluator = new BoardEvaluator();
    private static String[] unicodeChess = new String[] {"\u2654", "\u2655",
            "\u2656", "\u2657", "\u2658", "\u2659", "  ", "\u265F", "\u265E",
            "\u265D", "\u265C", "\u265B", "\u265A"};
    private final static int MOVE_STACK_SIZE = 10000;
    private final static long[] knightAttacks = {
            0x0000000000020400L, 0x0000000000050800L, 0x00000000000A1100L,
            0x0000000000142200L, 0x0000000000284400L, 0x0000000000508800L,
            0x0000000000A01000L, 0x0000000000402000L, 0x0000000002040004L,
            0x0000000005080008L, 0x000000000A110011L, 0x0000000014220022L,
            0x0000000028440044L, 0x0000000050880088L, 0x00000000A0100010L,
            0x0000000040200020L, 0x0000000204000402L, 0x0000000508000805L,
            0x0000000A1100110AL, 0x0000001422002214L, 0x0000002844004428L,
            0x0000005088008850L, 0x000000A0100010A0L, 0x0000004020002040L,
            0x0000020400040200L, 0x0000050800080500L, 0x00000A1100110A00L,
            0x0000142200221400L, 0x0000284400442800L, 0x0000508800885000L,
            0x0000A0100010A000L, 0x0000402000204000L, 0x0002040004020000L,
            0x0005080008050000L, 0x000A1100110A0000L, 0x0014220022140000L,
            0x0028440044280000L, 0x0050880088500000L, 0x00A0100010A00000L,
            0x0040200020400000L, 0x0204000402000000L, 0x0508000805000000L,
            0x0A1100110A000000L, 0x1422002214000000L, 0x2844004428000000L,
            0x5088008850000000L, 0xA0100010A0000000L, 0x4020002040000000L,
            0x0400040200000000L, 0x0800080500000000L, 0x1100110A00000000L,
            0x2200221400000000L, 0x4400442800000000L, 0x8800885000000000L,
            0x100010A000000000L, 0x2000204000000000L, 0x0004020000000000L,
            0x0008050000000000L, 0x00110A0000000000L, 0x0022140000000000L,
            0x0044280000000000L, 0x0088500000000000L, 0x0010A00000000000L,
            0x0020400000000000L};

    // The KingSafety class allows the getAllLegalMoves method to weed out illegal moves at runtime,
    // letting it avoid the old workaround of digging one layer deeper and checking then.
    static class KingSafety {
        boolean kingInCheck;
        long pinnedPieces; // Pieces that can't move
        long[] pinnedMoveOptions; // Contains the locations of pieces pinning others.
        long kingMoveOptions; // Places the king can legally move to
        boolean kingDoubleCheck; // If the king is in check by two different pieces
        long blockCheckSpots; // Spots where moving a piece would make the king no longer in check.
    }

    /* ~~ positioning is as follows: ~~
     * -> index 0 is a1, 8 is a2, 63 is h7 (left to right, then up, with white down)
     *
     * ~~ pieces are as follows: ~~
     * -> empty: 0
     * -> pawn: 1
     * -> knight: 2
     * -> bishop: 3
     * -> castle: 4
     * -> queen: 5
     * -> king: 6
     * ^^ black is negative, white is positive
     */

    /* Board state:
     * -> each long has 64 1s and 0s, allowing a single long to represent a boolean at each spot on the board.
     * -> _whites are 0 for each non white spot, and 1 for each white spot
     * -> if a spot is true for _whites, _bishops, and _rooks, and no other bitboard, then it's a white queen.
     */
    public long _whites;
    public long _blacks;
    public long _pawns;
    public long _bishops;
    public long _rooks;
    public long _kings;

    public int  _info;

    private long[] _moveHistory;
    private int _moveHistorySize;

    public Board(Game.Type type) {
        this(startLayout(type));
    }

    public Board(int[] layout) {
        this(layout, BoardInfo.START_INFO);
    }

    public Board(int[] layout, int info) {
        for (int i = 0; i < 64; i++) {
            if (layout[i] > 0) _whites = setBit(_whites, true, i);
            if (layout[i] < 0) _blacks = setBit(_blacks, true, i);
            if (layout[i] == 1 || layout[i] == -1) _pawns = setBit(_pawns, true, i);
            if (layout[i] == 3 || layout[i] == -3 || layout[i] == 5 || layout[i] == -5) _bishops = setBit(_bishops, true, i);
            if (layout[i] == 4 || layout[i] == -4 || layout[i] == 5 || layout[i] == -5) _rooks = setBit(_rooks, true, i);
            if (layout[i] == 6 || layout[i] == -6) _kings = setBit(_kings, true, i);
        }
        _info = info;
        _moveHistory = new long[MOVE_STACK_SIZE];
        _moveHistorySize = 0;
    }

    public Board(long whites, long blacks, long pawns, long bishops, long rooks, long kings, int info) {
        _whites = whites;
        _blacks = blacks;
        _pawns = pawns;
        _bishops = bishops;
        _rooks = rooks;
        _kings = kings;
        _info = info;
        _moveHistory = new long[MOVE_STACK_SIZE];
        _moveHistorySize = 0;
    }

    public void applyMove(long move) {
        _moveHistory[_moveHistorySize] = MoveUtils.applyMove(this, move);
        _moveHistorySize++;
    }

    public void undoMove() {
        _moveHistorySize--;
        MoveUtils.undoMove(this, _moveHistory[_moveHistorySize]);
    }

    public boolean isCheckMated(boolean white) {
        return kingIsInCheck(white) && getAllLegalMoves(white, false).size() == 0;
    }

    public List<Long> getAllLegalMoves(boolean forWhite, boolean forHuman) {
        KingSafety safety = getKingSafety(forWhite);

        List<Long> output = new ArrayList<>();

        // Go to each piece, and add all of its moves to the output.
        for (int i = 0; i < 64; i++) {
            // If there isn't a piece of the correct color at i, continue:
            if ((forWhite && !white(i)) || (!forWhite && !black(i))) continue;

            // Determine the piece, and calculate its moves accordingly.
            if (king(i)) {
                // King
                output.addAll(bitMovesToMoves(i, safety.kingMoveOptions, 0));

                // Castling
                if (forWhite && !BoardInfo.whiteKingHasMoved(_info) && !BoardInfo.whiteCastleA1HasMoved(_info) && white(0) &&
                        empty(1) && empty(2) && empty(3) && !safety.kingInCheck && !positionIsInCheck(2, true)) {
                    output.add(MoveUtils.generateMove(i, 2, this, 0));
                }
                if (forWhite && !BoardInfo.whiteKingHasMoved(_info) && !BoardInfo.whiteCastleH1HasMoved(_info) && white(7) &&
                        empty(6) && empty(5) && !safety.kingInCheck && !positionIsInCheck(6, true)) {
                    output.add(MoveUtils.generateMove(i, 6, this, 0));                }
                if (!forWhite && !BoardInfo.blackKingHasMoved(_info) && !BoardInfo.blackCastleA7HasMoved(_info) && black(56) &&
                        empty(57) && empty(58) && empty(59) && !safety.kingInCheck && !positionIsInCheck(58, false)) {
                    output.add(MoveUtils.generateMove(i, 58, this, 0));                }
                if (!forWhite && !BoardInfo.blackKingHasMoved(_info) && !BoardInfo.blackCastleH7HasMoved(_info) && black(63) &&
                        empty(62) && empty(61) && !safety.kingInCheck && !positionIsInCheck(62, false)) {
                    output.add(MoveUtils.generateMove(i, 62, this, 0));                }

            } else if (!safety.kingDoubleCheck) {
                if (bishop(i) && rook(i)) {
                    // Queen
                    output.addAll(getLineMoves(i, true, true, false, forWhite, 7, safety));
                } else if (rook(i)) {
                    // Rook
                    output.addAll(getLineMoves(i, true, false, false, forWhite, 7, safety));
                } else if (bishop(i)) {
                    // Bishop
                    output.addAll(getLineMoves(i, false, true, false, forWhite, 7, safety));
                } else if (pawn(i)) {
                    // Pawn
                    output.addAll(getPawnMoves(i, safety, forHuman));
                } else {
                    // Knight
                    output.addAll(getLineMoves(i, false, false, true, forWhite, 1, safety));
                }
            }
        }

        return output;
    }

    public boolean kingIsInCheck(boolean whiteKing) {
        return positionIsInCheck(Long.numberOfTrailingZeros(_kings & (whiteKing ? _whites : _blacks)), whiteKing);
    }

    private boolean positionIsInCheck(int pos, boolean forWhite) {
        // Check for bishops and rooks.
        for (int direction = 0; direction < 8; direction++) {
            int dx = (direction == 0 || direction == 4) ? 0 : (direction < 4) ? 1 : -1;
            int dy = (direction == 2 || direction == 6) ? 0 : (direction < 6 && direction > 2) ? 1 : -1;
            int x = pos%8;
            int y = pos/8;
            boolean rook = direction % 2 == 0; // if false, assume bishop.

            // Traverse through and find bishops / rooks that put the spot in check.
            while(true) {
                x += dx;
                y += dy;
                if (x > 7 || x < 0 || y > 7 || y < 0) break;
                int currentPos = (x + 8*y);
                if (!(empty(currentPos) || (king(currentPos) && (forWhite ? white(currentPos) : black(currentPos))))) {
                    if (white(currentPos) == forWhite) {
                        break;
                    } else {
                        if ((!rook || rook(currentPos)) && (rook || bishop(currentPos))) {
                            return true;
                        }
                        break;
                    }
                }
            }
        }

        // Check for knights.
        long knightSpots = knightAttacks[pos];

        // Check for kings.
        long kingSpots = 0;
        int x = pos%8;
        int y = pos/8;
        for (int direction = 0; direction < 8; direction++) {
            int dx = (direction == 0 || direction == 4) ? 0 : (direction < 4) ? 1 : -1;
            int dy = (direction == 2 || direction == 6) ? 0 : (direction > 2 && direction < 6) ? 1 : -1;
            if (x + dx < 0 || x + dx > 7 || y + dy < 0 || y + dy > 7) continue;
            kingSpots = setBit(kingSpots, true, (x+dx) + 8*(y+dy));
        }

        // Check for pawns.
        long pawnCheckSpots = 0;
        if (forWhite ? pos / 8 != 7 : pos / 8 != 0) {
            // Left:
            if (pos % 8 != 0) {
                pawnCheckSpots = setBit(pawnCheckSpots, true, (forWhite ? pos + 7 : pos - 9));
            }
            // Right:
            if (pos % 8 != 7) {
                pawnCheckSpots = setBit(pawnCheckSpots, true, (forWhite ? pos + 9 : pos - 7));
            }
        }

        // Now, go through every square, and if any of them are a pawn, king, or knight putting pos in check, return true.
        for (int i = 0; i < 64; i++) {
            if (!(forWhite ? black(i) : white(i))) continue; // Not right color.

            if (((knightSpots>>i) & 1) != 0 && knight(i)) {
                return true;
            }

            if (((kingSpots>>i) & 1) != 0 && king(i)) {
                return true;
            }

            if (((pawnCheckSpots>>i) & 1) != 0 && pawn(i)) {
                return true;
            }
        }
        return false;
    }

    public int getPoints(boolean endgame) {
        if (isDrawThreeFoldRepetition()) return 0;
        if (isDrawFiftyMoveNoProgress()) return 0;
        return evaluator.getPoints(this, endgame);
    }

    private boolean isDrawThreeFoldRepetition() {
        return _moveHistorySize >= 9 && MoveUtils.equal(_moveHistory[_moveHistorySize-1], _moveHistory[_moveHistorySize-5], _moveHistory[_moveHistorySize-9]) &&
                MoveUtils.equal(_moveHistory[_moveHistorySize-2], _moveHistory[_moveHistorySize-6]) &&
                MoveUtils.equal(_moveHistory[_moveHistorySize-3], _moveHistory[_moveHistorySize-7]) &&
                MoveUtils.equal(_moveHistory[_moveHistorySize-4], _moveHistory[_moveHistorySize-8]); // Draw game.
    }

    public boolean isDraw() {
        return isDrawFiftyMoveNoProgress() || isDrawThreeFoldRepetition();
    }

    private boolean isDrawFiftyMoveNoProgress() {
        return BoardInfo.numMovesSinceProgress(_info) >= 50;
    }

    private static int[] startLayout(Game.Type type) {
        String path = type == Game.Type.CHESS ? "data/chessStart.txt" :
                         type == Game.Type.SLAUGHTER_CHESS ? "data/slaughterChessStart.txt" :
                         type == Game.Type.HORDE ? "data/hordeStart.txt" : null;
        Scanner layoutScanner;
        try {
            layoutScanner = new Scanner(new File(path));
//            layoutScanner = new Scanner(Board.class.getClassLoader().getResourceAsStream(path));
        } catch (Exception e) {
            System.out.println("file could not be found: " + path);
            return null;
        }
        int[] output = new int[64];

        // Loop runs for each x position, filling in the board.
        for (int y = 7; y >= 0; y--) {
            for (int x = 0; x < 8; x++) {
                output[8*y + x] = layoutScanner.nextByte();
            }
        }
        return output;
    }

    public int[] getLayout() {
        int[] layout = new int[64];
        for (int i = 0; i < layout.length; i++) {
            layout[i] = pieceAt(i);
        }
        return layout;
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        for (int i = 7; i >= 0; i--) {
            for (int j = 0; j < 8; j++) {
                out.append(unicodeChess[pieceAt(8 * i + j) + 6]).append(j == 7 ? "" : " ");
        }
            out.append("\n");
        }
        return out.toString();
    }

    private List<Long> getPawnMoves(int pos, KingSafety safety, boolean forHuman) {
        List<Long> moves = new ArrayList<>();
        boolean isWhite = white(pos);

        boolean isPromotion = isWhite ? (pos/8 == 6) : (pos/8 == 1);
        boolean spaceInFront = isWhite ? (pos/8 != 7 && empty(pos + 8)) : (pos/8 != 0 && empty(pos - 8));
        boolean spaceDoubleInFront = isWhite ? (pos/8 < 6 && empty(pos + 16)) : (pos/8 > 1 && empty(pos - 16));
        boolean canTakeLeft = isWhite ? (pos/8 != 7 && pos % 8 != 0 && black(pos + 7)) : (pos/8 != 0 && pos % 8 != 0 && white(pos - 9));
        boolean canTakeRight = isWhite ? (pos/8 != 7 && pos % 8 != 7 && black(pos + 9)) : (pos/8 != 0 && pos % 8 != 7 && white(pos - 7));
        boolean enPassantLeft = isWhite ? (BoardInfo.lastMoveWasDoublePawnMove(_info) && pos/8 == 4 && pos % 8 != 0 &&
                empty(pos + 7) && pos-1 == BoardInfo.positionOfDoublePawn(_info)) : (BoardInfo.lastMoveWasDoublePawnMove(_info)
                && pos/8 == 3 && pos % 8 != 0 && empty(pos - 9) && pos-1 == BoardInfo.positionOfDoublePawn(_info));
        boolean enPassantRight = isWhite ? (BoardInfo.lastMoveWasDoublePawnMove(_info) && pos/8 == 4 && pos % 8 != 7 &&
                empty(pos + 9) && pos+1 == BoardInfo.positionOfDoublePawn(_info)) : (BoardInfo.lastMoveWasDoublePawnMove(_info)
                && pos/8 == 3 && pos % 8 != 7 && empty(pos - 7) && pos+1 == BoardInfo.positionOfDoublePawn(_info));

        int inFront = isWhite ? pos + 8 : pos - 8;


        if (spaceInFront && (!safety.kingInCheck || ((safety.blockCheckSpots >> inFront) & 1) != 0) &&
                (((safety.pinnedPieces>>pos)&1l) == 0 || ((safety.pinnedMoveOptions[pos]>>inFront)&1l) != 0)) {
            moves.addAll(getMoveAndPromotion(pos, inFront, isPromotion, forHuman));
        }

        int inFrontTwo = isWhite ? pos + 16 : pos - 16;
        if (spaceInFront && spaceDoubleInFront && (isWhite ? (pos / 8 <= 1) : (pos / 8 >= 6))
                && (!safety.kingInCheck || ((safety.blockCheckSpots >> inFrontTwo) & 1) != 0) &&
                (((safety.pinnedPieces>>pos)&1l) == 0 || ((safety.pinnedMoveOptions[pos]>>inFrontTwo)&1l) != 0)) {
            moves.add(MoveUtils.generateMove(pos, inFrontTwo, this, 0));
        }

        int toLeft = isWhite ? pos + 7 : pos - 9;
        if (((safety.pinnedPieces>>pos) & 1L) == 0 || ((safety.pinnedMoveOptions[pos] >> toLeft) & 1) != 0) {
            if (canTakeLeft && (!safety.kingInCheck || ((safety.blockCheckSpots >> (toLeft)) & 1) != 0)) {
                moves.addAll(getMoveAndPromotion(pos, toLeft, isPromotion, forHuman));
            }

            if (enPassantLeft && (!safety.kingInCheck || ((safety.blockCheckSpots >> (toLeft)) & 1) != 0)) {
                moves.add(MoveUtils.generateMove(pos, toLeft, this, 0));
            }
        }


        int toRight = isWhite ? pos + 9 : pos - 7;
        if (((safety.pinnedPieces>>pos) & 1L) == 0 || ((safety.pinnedMoveOptions[pos] >> toRight) & 1) != 0) {
            if (canTakeRight && (!safety.kingInCheck || ((safety.blockCheckSpots >> (toRight)) & 1) != 0)) {
                moves.addAll(getMoveAndPromotion(pos, toRight, isPromotion, forHuman));
            }

            if (enPassantRight && (!safety.kingInCheck || ((safety.blockCheckSpots >> (toRight)) & 1) != 0)) {
                moves.add(MoveUtils.generateMove(pos, toRight, this, 0));
            }
        }
        return moves;
    }


    private List<Long> getMoveAndPromotion(int from, int to, boolean isPromotion, boolean forHuman) {
        List<Long> moves = new ArrayList<>();

        if (!isPromotion) {
            moves.add(MoveUtils.generateMove(from, to, this, 0));
        } else {
            if (forHuman) {
                moves.add(MoveUtils.generateMove(from, to, this, 0));
            } else {
                moves.add(MoveUtils.generateMove(from, to, this, 2));
                moves.add(MoveUtils.generateMove(from, to, this, 3));
                moves.add(MoveUtils.generateMove(from, to, this, 4));
                moves.add(MoveUtils.generateMove(from, to, this, 5));
            }
        }

        return moves;
    }

    /**
     * @param startPos the start position of all moves in moves.
     * @param moves the bitboard of moves from startPos.
     * @param pawnChoice the default pawn choice for promotion moves - 0 = user input, 2 = knight, 3 = bishop, etc.
     * @return a list of moves from startPos.
     */
    private List<Long> bitMovesToMoves(int startPos, long moves, int pawnChoice) {
        List<Long> output = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            if (getBit(moves, i)) {
                 output.add(MoveUtils.generateMove(startPos, i, this, pawnChoice));
            }
        }
        return output;
    }

    /**
     * @param startPos the start position of all considered moves (will not be included in output).
     * @param rook if rook moves should be included in the output.
     * @param bishop if bishop moves should be included in the output.
     * @param knight if knight moves should be included in the output.
     * @param forWhite if the piece to be moved is white.
     * @param maxDist the max distance of the traversal. Should be 7 for rooks, bishops, queens, 1 for kings and knights.
     * @return a bitBoard representing each square as 1 if the piece at startPos can move into it, and 0 if not.
     */
    private List<Long> getLineMoves(int startPos, boolean rook, boolean bishop, boolean knight, boolean forWhite, int maxDist, KingSafety safety) {
        List<Long> output = new ArrayList<>();
        if (safety.kingInCheck && safety.kingDoubleCheck && !king(startPos)) {
            return output;
        }

        // Rook moves
        if (rook) {
            for (int i = 0; i < 4; i++) {
                int dx = (i == 1) ? 1 : (i == 3) ? -1 : 0;
                int dy = (i == 2) ? 1 : (i == 0) ? -1 : 0;
                output.addAll(getMovesInDirection(startPos, forWhite, dx, dy, maxDist, safety));
            }
        }

        // Bishop moves
        if (bishop) {
            for (int i = 0; i < 4; i++) {
                int dx = (i < 2) ? 1 : -1;
                int dy = (i == 1 || i == 2) ? 1 : -1;
                output.addAll(getMovesInDirection(startPos, forWhite, dx, dy, maxDist, safety));
            }
        }

        // Knight moves
        if (knight) {
            for (byte i = 0; i < 8; i++) {
                int dx = (i == 0 || i == 3) ? 1 : (i == 4 || i == 7) ? -1 : (i == 1 || i == 2) ? 2 : -2;
                int dy = (i == 0 || i == 7) ? -2 : (i == 1 || i == 6) ? -1 : (i == 2 || i == 5) ? 1 : 2;
                output.addAll(getMovesInDirection(startPos, forWhite, dx, dy, maxDist, safety));
            }
        }

        return output;
    }

    /**
     * @param startPos the start position of the traversal (will not be included in output).
     * @param forWhite true if the piece moving is white (will not be allowed to capture white pieces).
     * @param dx the change in x of the traversal.
     * @param dy the change in y of the traversal.
     * @param maxDist the max distance to be traversed.  Use 7 or more for everything, and 1 for knights and kings.
     * @return a bit board of all spots that the piece in startPos could move into along the direction specified
     */
    private List<Long> getMovesInDirection(int startPos, boolean forWhite, int dx, int dy, int maxDist, KingSafety safety) {
        List<Long> output = new ArrayList<>();
        int x = startPos%8;
        int y = startPos/8;

        for (int i = 0; i < maxDist; i++) {
            x += dx;
            y += dy;
            if (x < 0 || x > 7 || y < 0 || y > 7) break;
            int currentPos = x + 8*y;

            // If this piece is pinned, and this move will put the king in check, continue.
            if (((safety.pinnedPieces>>startPos) & 1L) == 1 && ((safety.pinnedMoveOptions[startPos]>>currentPos) & 1) == 0) {
                if (empty(currentPos)) continue; else break;
            }

            // If the spot is empty, add to output and continue.
            if (empty(currentPos)) {
                if (!(safety.kingInCheck && ((safety.blockCheckSpots>>currentPos) & 1L) == 0)) {
                    output.add(MoveUtils.generateMove(startPos, currentPos, this, 0));
                }
                continue;
            }

            // If the spot is of the other color, add to output.
            if (white(currentPos) != forWhite && !empty(currentPos) && !(safety.kingInCheck && ((safety.blockCheckSpots>>currentPos) & 1L) == 0)) {
                output.add(MoveUtils.generateMove(startPos, currentPos, this, 0));
            }

            // Quit, since this position wasn't empty.
            break;
        }
        return output;
    }

    /**
     * @return the king safety of this board, for use in calculating all LEGAL moves from this board.
     */
    private KingSafety getKingSafety(boolean forWhite) {

        // KingSafety generation outline: first go to king, and then
        // <> Go outward in each direction and
        //   > get pinned pieces
        //   > get pieces giving check (including how many)
        //   > get block check spots.
        //
        // <> Check for knights and pawns that could put the spot in check.
        //
        // <> Go to each spot the king could move to
        //   > Check if there is a piece putting that spot in check

        int kingPos = Long.numberOfTrailingZeros((forWhite ? _whites : _blacks) & _kings);
        if (kingPos == 64) {
            throw new RuntimeException("No king for color " + (forWhite ? "white" : "black") + " on board \n" + this + "with _kings: " + bitString(_kings)
            + "\nand whites: " + bitString(_whites) + "\nand blacks " + bitString(_blacks));
        }

        KingSafety output = new KingSafety();

        output.pinnedMoveOptions = new long[64];
        output.kingInCheck = kingIsInCheck(forWhite);
        int numPiecesGivingCheck = 0;

        // Loop for each direction going out of king ... finds all blockable checks and pinned pieces.
        for (int direction = 0; direction < 8; direction++) {
            int dx = (direction == 0 || direction == 4) ? 0 : (direction < 4) ? -1 : 1;
            int dy = (direction == 2 || direction == 6) ? 0 : (direction < 2 || direction > 6) ? -1 : 1;
            int x = kingPos%8;
            int y = kingPos/8;

            long blockableCheckSpots = 0;
            long pinnedPieces = 0;
            boolean moreThanOnePinnedPiece = false; // If this is true, then there are no pinned pieces in this direction.
            long piecesGivingCheck = 0;

            // Traverse through and find all important info.
            while(true) {
                x += dx;
                y +=  dy;
                if (x < 0 || x > 7 || y < 0 || y > 7) break;
                int pos = x + 8*y;
                if (empty(pos)) {
                    blockableCheckSpots = setBit(blockableCheckSpots, true, pos);
                } else if (white(pos) == forWhite) {
                    if (pinnedPieces == 0) {
                        pinnedPieces = setBit(pinnedPieces, true, pos);
                    } else {
                        moreThanOnePinnedPiece = true;
                    }
                } else {
                    if ((((dy == 0 || dx == 0) && rook(pos)) || (dx != 0 && dy != 0 && bishop(pos)))) {
                        piecesGivingCheck = setBit(piecesGivingCheck, true, pos);
                        blockableCheckSpots = setBit(blockableCheckSpots, true, pos);
                    }
                    break;
                }
            }

            // Untangle the results:
            if (piecesGivingCheck != 0) {
                if (pinnedPieces == 0) {
                    // The piece giving check is actually giving check.
                    numPiecesGivingCheck++;

                    // The blockable check spots are actually spots that can block the check.
                    output.blockCheckSpots = output.blockCheckSpots | blockableCheckSpots;
                } else if (!moreThanOnePinnedPiece) {
                    // The pinned pieces are actually pinned.
                    output.pinnedPieces = output.pinnedPieces | pinnedPieces;
                    output.pinnedMoveOptions[Long.numberOfTrailingZeros(pinnedPieces)] = blockableCheckSpots;
                }
            }
        }

        // Find all knights putting the king in check
        long knightCheckSpots = knightAttacks[kingPos];

        // Pawn attacks:
        long pawnCheckSpots = 0;
        if (forWhite ? kingPos / 8 != 7 : kingPos / 8 != 0) {
            // Left:
            if (kingPos % 8 != 0) {
                pawnCheckSpots = setBit(pawnCheckSpots, true, forWhite ? kingPos + 7 : kingPos - 9);
            }
            // Right:
            if (kingPos % 8 != 7) {
                pawnCheckSpots = setBit(pawnCheckSpots, true, forWhite ? kingPos + 9 : kingPos - 7);
            }
        }

        // Now, go through each square, and if the square is a pawn or knight that can put the king in check, mark that.
        for (int i = 0; i < 64; i++) {
            // If the piece is not of the opposite color, skip it.
            if (!(forWhite ? black(i) : white(i))) continue;

            // Check for knights and pawns:
            if ((((knightCheckSpots >> i) & 1L) != 0 && knight(i)) || (((pawnCheckSpots >> i) & 1L) != 0 && pawn(i))) {
                numPiecesGivingCheck++;
                output.blockCheckSpots = setBit(output.blockCheckSpots, true, i);
            }
        }

        // If more than one piece is giving check, mark as double check.
        // Double checks can only be stopped by moving the king, so this avoids wasting time on other types of moves in that case.
        output.kingDoubleCheck = numPiecesGivingCheck > 1;

        // Now, check each spot near the king, and check if the king can move there.
        for (int direction = 0; direction < 8; direction++) {
            int x = (kingPos%8) + ((direction == 0 || direction == 4) ? 0 : (direction < 4) ? 1 : -1);
            int y = (kingPos/8) + ((direction == 2 || direction == 6) ? 0 : (direction > 2 && direction < 6) ? 1 : -1);
            if (x > 7 || x < 0 || y > 7 || y < 0) continue;
            int spot = x + 8*y;
            if (!positionIsInCheck(spot, forWhite) && (forWhite ? !white(spot) : !black(spot))) output.kingMoveOptions = setBit(output.kingMoveOptions, true, spot);
        }

        return output;
    }

    public boolean white(int pos) { return getBit(_whites, pos); }

    public boolean black(int pos) { return getBit(_blacks, pos); }

    public boolean empty(int pos) { return !getBit(_whites | _blacks, pos); }

    public boolean king(int pos) { return getBit(_kings, pos); }

    public boolean rook(int pos) { return getBit(_rooks, pos); }

    public boolean bishop(int pos) { return getBit(_bishops, pos); }

    public boolean queen(int pos) { return getBit(_bishops & _rooks, pos); }

    public boolean pawn(int pos) { return getBit(_pawns, pos); }

    public boolean knight(int pos) { return (white(pos) || black(pos)) && (!king(pos) && !pawn(pos) && !bishop(pos) && !rook(pos)); }

    public void copy(int from, int to) {
        _whites = setBit(_whites, getBit(_whites, from), to);
        _blacks = setBit(_blacks, getBit(_blacks, from), to);
        _kings = setBit(_kings, getBit(_kings, from), to);
        _bishops = setBit(_bishops, getBit(_bishops, from), to);
        _rooks = setBit(_rooks, getBit(_rooks, from), to);
        _pawns = setBit(_pawns, getBit(_pawns , from), to);
    }

    public void wipe(int pos) {
        _whites = setBit(_whites, false, pos);
        _blacks = setBit(_blacks, false, pos);
        _kings = setBit(_kings, false, pos);
        _bishops = setBit(_bishops, false, pos);
        _rooks = setBit(_rooks, false, pos);
        _pawns = setBit(_pawns,  false, pos);
    }

    public int pieceAt(int pos) {
        int pieceMult = white(pos) ? 1 : -1;
        int piece = 0;
        if (king(pos)) {
            piece = 6;
        } else if (pawn(pos)) {
            piece = 1;
        } else if (knight(pos)) {
            piece = 2;
        } else if (bishop(pos)) {
            piece = 3;
        } else if (rook(pos)) {
            piece = 4;
        }
        if (queen(pos)) {
            piece = 5;
        }
        return piece * pieceMult;
    }

    public void setPiece(int pos, int piece) {
        if (piece > 0) {
            _whites = setBit(_whites, true, pos);
            _blacks = setBit(_blacks, false, pos);
        } else if (piece < 0) {
            _blacks = setBit(_blacks, true, pos);
            _whites = setBit(_whites, false, pos);
        } else {
            _whites = setBit(_whites, false, pos);
            _blacks = setBit(_blacks, false, pos);
        }
        _kings = setBit(_kings, (piece == 6 || piece == -6), pos);
        _pawns = setBit(_pawns, (piece == 1 || piece == -1), pos);
        _bishops = setBit(_bishops, (piece == 3 || piece == -3 || piece == 5 || piece == -5), pos);
        _rooks = setBit(_rooks, (piece == 4 || piece == -4 || piece == 5 || piece == -5), pos);
    }

    private static long setBit (long bits, boolean set, int offset) {
        if (set) {
            bits |= (1L << offset);
        } else {
            bits &= ~(1L << offset);
        }
        return bits;
    }

    private static boolean getBit(long bits, int pos) {
        return ((bits>>>pos) & 1) != 0;
    }

    private static String bitString(long bits) {
        StringBuilder sb = new StringBuilder();
        for (int i = 63; i >= 0; i--) {
            sb.append((bits>>i) & 1);
        }
        return sb.toString();
    }

    public Board clone() {
        Board output = new Board(_whites, _blacks, _pawns, _bishops, _rooks, _kings, _info);
        return output;
    }

    public boolean getIsEndgame() {
        int count = 0;
        for (int i = 0; i < 64; i++) {
            if (!empty(i)) count++;
        }
        return count < 14;
    }

    public String toTextString() {
        StringBuilder out = new StringBuilder();
        for (int i = 7; i >= 0; i--) {
            for (int j = 0; j < 8; j++) {
                out.append(pieceAt(8 * i + j)).append(j == 7 ? "" : " ");
            }
            out.append("\n");
        }
        return out.toString();
    }

    public int getNumMoves() {
        return _moveHistorySize;
    }
}