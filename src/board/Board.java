package board;

import evaluation.BoardEvaluator;
import game.Game;
import moves.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/*
 * Board class:
 * -> The "Model" part of the Model-View-Controller design pattern.
 * -> Stores a chessboard's current state and houses relevant methods.
 */

public class Board {
    /* index 0 is a1, 8 is a2, 63 is h7 (left to right, then up)
     * stuff.pieces are as follows:
     * empty: 0
     * pawn: 1
     * knight: 2
     * bishop: 3
     * castle: 4
     * queen: 5
     * king: 6
     * black is negative, white is positive
     */
    private static BoardEvaluator evaluator = new BoardEvaluator();

    private byte[] _layout;
    private BoardInfo _info;
    private static String[] unicodeChess = new String[] {"\u2654", "\u2655", "\u2656", "\u2657", "\u2658", "\u2659", " ",
            "\u265F", "\u265E", "\u265D", "\u265C", "\u265B", "\u265A"};

    private int _whiteKingIsInCheck = 0;
    private int _blackKingIsInCheck = 0;
    private int _whiteCanMove = 0;
    private int _blackCanMove = 0;


    public Board(Game.Type type) {
        this(startLayout(type));
    }

    public Board(byte[] layout) {
        this(layout, new BoardInfo());
    }

    public Board(byte[] layout, BoardInfo info) {
        _layout = layout.clone();
        _info = info.clone();
    }

    public Board applyMove(Move move) {
        return move.applyMove(_layout, _info);
    }

    public List<Move> getAllLegalMoves(boolean forWhite, PawnPromotionChooser chooser) {
        List<Move> moves = getAllMoves(forWhite, chooser, true);

        // Finally, remove all moves that would put the king in check.
        List<Move> output = new ArrayList<>();
        for (Move m : moves) {
            if (m.isUserPawnPromotion()) {
                Move botM = new PawnPromotionMove(m.getStart(), m.getEnd(), (byte) 5);
                if (!applyMove(botM).kingIsInCheck(forWhite)) {
                    output.add(m);
                }
            } else if (!applyMove(m).kingIsInCheck(forWhite)) {
                output.add(m);
            }
        }
        return output;
    }

    private List<Move> getAllMoves(boolean forWhite, PawnPromotionChooser chooser
            /* use null for chooser if you want four separate moves for promotions */, boolean worryAboutCheck) {
        List<Move> moves = new ArrayList<>();

        // Go to each piece of correct color, and add all of its moves to the moves.
        for (int i = 0; i < _layout.length; i++) {
            if ((forWhite && _layout[i] <= 0) || (!forWhite && _layout[i] >= 0)) continue;

            // Now, we know that the piece at i is the right color.

            // Determine the piece, and calculate moves accordingly.
            if (_layout[i] == 6 || _layout[i] == -6) {
                // King
                moves.addAll(getLineMoves(i, true, true, false, 1));
                if (worryAboutCheck) {
                    // Castling
                    if (forWhite && !_info.whiteKingHasMoved && !_info.whiteCastleA1HasMoved &&
                            _layout[1] == 0 && _layout[2] == 0 && _layout[3] == 0 && !kingIsInCheck(true)) {
                        moves.add(new CastleMove((byte) 4, (byte) 2));
                    }
                    if (forWhite && !_info.whiteKingHasMoved && !_info.whiteCastleH1HasMoved &&
                            _layout[6] == 0 && _layout[5] == 0 && !kingIsInCheck(true)) {
                        moves.add(new CastleMove((byte) 4, (byte) 6));
                    }
                    if (!forWhite && !_info.blackKingHasMoved && !_info.blackCaslteA8HasMoved &&
                            _layout[57] == 0 && _layout[58] == 0 && _layout[59] == 0 && !kingIsInCheck(false)) {
                        moves.add(new CastleMove((byte) 60, (byte) 58));
                    }
                    if (!forWhite && !_info.blackKingHasMoved && !_info.blackCastleH8HasMoved &&
                            _layout[62] == 0 && _layout[61] == 0 && !kingIsInCheck(false)) {
                        moves.add(new CastleMove((byte) 60, (byte) 62));
                    }
                }
            } else if (_layout[i] == 5 || _layout[i] == -5) {
                // Queen
                moves.addAll(getLineMoves(i, true, true, false, 8));
            } else if (_layout[i] == 4 || _layout[i] == -4) {
                // Rook
                moves.addAll(getLineMoves(i, true, false, false, 8));
            } else if (_layout[i] == 3 || _layout[i] == -3) {
                // Bishop
                moves.addAll(getLineMoves(i, false, true, false, 8));
            } else if (_layout[i] == 2 || _layout[i] == -2) {
                // Knight
                moves.addAll(getLineMoves(i, false, false, true, 1));
            } else {
                // Pawn
                moves.addAll(getPawnMoves(i, chooser));
            }
        }
        return moves;
    }

    public boolean kingIsInCheck(boolean whiteKing) {
        if (whiteKing && _whiteKingIsInCheck != 0) {
            return (_whiteKingIsInCheck > 0);
        }
        if (!whiteKing && _blackKingIsInCheck != 0) {
            return (_blackKingIsInCheck > 0);
        }

        // Get the position of the king:
        int kingPos = -1;
        for (int i = 0; i < 64; i++) {
            if (_layout[i] == (whiteKing ? 6 : -6)) {
                kingPos = i;
                break;
            }
        }

        List<Move> moves = getAllMoves(!whiteKing, null, false);

        for (Move m : moves) {
            if (m.getEnd() == kingPos) {
                if (whiteKing) {
                    _whiteKingIsInCheck = 1;
                } else {
                    _blackKingIsInCheck = 1;
                }
                return true;
            }
        }
        if (whiteKing) {
            _whiteKingIsInCheck = -1;
        } else {
            _blackKingIsInCheck = -1;
        }        return false;
    }

    public int getPoints() {
        return evaluator.getPoints(_layout);
    }

    private static byte[] startLayout(Game.Type type) {
        String address = type == Game.Type.CHESS ? "data/chessStart.txt." :
                         type == Game.Type.SLAUGHTER_CHESS ? "data/slaughterChessStart.txt" :
                         type == Game.Type.HORDE ? "data/hordeStart.txt" : null;
        File layoutFile = new File(address);
        Scanner layoutScanner;
        try {
            layoutScanner = new Scanner(layoutFile);
        } catch (FileNotFoundException e) {
            System.out.println("file could not be found: " + address);
            return null;
        }
        byte[] output = new byte[64];

        // Loop runs for each x position, filling in the board.
        for (int y = 7; y >= 0; y--) {
            for (int x = 0; x < 8; x++) {
                output[8*y + x] = layoutScanner.nextByte();
            }
        }
        return output;
    }

    public byte[] getLayout() {
        return _layout.clone();
    }

    public boolean isCheckMated(boolean isWhite) {
        return playerCannotMove(isWhite) && kingIsInCheck(isWhite);
    }

    public boolean playerCannotMove(boolean isWhite) {
        if (isWhite && _whiteCanMove != 0) return _whiteCanMove < 0;
        if (!isWhite && _blackCanMove != 0) return _blackCanMove < 0;

        boolean output = getAllLegalMoves(isWhite, null).size() == 0;
        if (isWhite) {
            _whiteCanMove = output ? -1 : 1;
        } else {
            _blackCanMove = output ? -1 : 1;
        }
        return output;
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        for (int i = 7; i >= 0; i--) {
            for (int j = 0; j < 8; j++) {
                out.append(unicodeChess[_layout[8 * i + j] + 6]).append(j == 7 ? "" : " ");
            }
            out.append(i == 7 ? "" : "\n");
        }
        return out.toString();
    }

    private List<Move> getPawnMoves(int pos, PawnPromotionChooser chooser) {
        List<Move> moves = new ArrayList<>();
        boolean isWhite = _layout[pos] > 0;

        boolean isPromotion = isWhite ? (pos/8 == 6) : (pos/8 == 1);
        boolean spaceInFront = isWhite ? (pos/8 != 7 && _layout[pos + 8] == 0) : (pos/8 != 0 && _layout[pos - 8] == 0);
        boolean spaceDoubleInFront = isWhite ? (pos/8 < 6 && _layout[pos + 16] == 0) : (pos/8 > 1 && _layout[pos - 16] == 0);
        boolean canTakeLeft = isWhite ? (pos/8 != 7 && pos % 8 != 0 && _layout[pos + 7] < 0) : (pos/8 != 0 && pos % 8 != 0 && _layout[pos - 9] > 0);
        boolean canTakeRight = isWhite ? (pos/8 != 7 && pos % 8 != 7 && _layout[pos + 9] < 0) : (pos/8 != 0 && pos % 8 != 7 && _layout[pos - 7] > 0);
        boolean enPassantLeft = isWhite ? (_info.lastMoveWasDoublePawnMove && pos/8 == 4 && pos % 8 != 0 && _layout[pos + 7] == 0 && pos-1 == _info.positionOfDoublePawn) :
                (_info.lastMoveWasDoublePawnMove && pos/8 == 3 && pos % 8 != 0 && _layout[pos - 9] == 0 && pos-1 == _info.positionOfDoublePawn);
        boolean enPassantRight = isWhite ? (_info.lastMoveWasDoublePawnMove && pos/8 == 4 && pos % 8 != 7 && _layout[pos + 9] == 0 && pos+1 == _info.positionOfDoublePawn) :
                (_info.lastMoveWasDoublePawnMove && pos/8 == 3 && pos % 8 != 7 && _layout[pos - 7] == 0 && pos+1 == _info.positionOfDoublePawn);

        if (spaceInFront) {
            moves.addAll(getMoveAndPromotion(pos, isWhite ? pos+8 : pos-8, isPromotion, chooser));
        }

        if (spaceInFront && spaceDoubleInFront && (isWhite ? (pos/8 <= 1) : (pos/8 >= 6))) {
            moves.add(new NormalMove(pos, isWhite ? pos+16 : pos-16));
        }

        int toLeft = isWhite ? pos + 7 : pos - 9;
        int toRight = isWhite ? pos + 9 : pos - 7;

        if (canTakeLeft) {
            moves.addAll(getMoveAndPromotion(pos, toLeft, isPromotion, chooser));
        }

        if (canTakeRight) {
            moves.addAll(getMoveAndPromotion(pos, toRight, isPromotion, chooser));
        }

        if (enPassantLeft) {
            moves.add(new EnPassantMove(pos, toLeft));
        }

        if (enPassantRight) {
            moves.add(new EnPassantMove(pos, toRight));
        }

        return moves;
    }

    private List<Move> getMoveAndPromotion(int from, int to, boolean isPromotion, PawnPromotionChooser chooser) {
        List<Move> output = new ArrayList<>();

        if (!isPromotion) {
            output.add(new NormalMove(from, to));
        } else {
            output.add(new PawnPromotionMove(from , to, (byte) 2));
            output.add(new PawnPromotionMove(from, to, (byte) 3));
            output.add(new PawnPromotionMove(from, to, (byte) 4));
            output.add(new PawnPromotionMove(from, to, (byte) 5));
            if (chooser != null) output.add(new PawnPromotionMove(from, to, chooser));
        }

        return output;
    }

    private List<Move> getLineMoves(int startPos, boolean rook, boolean bishop, boolean knight, int maxDist) {
        List<Move> output = new ArrayList<>();

        // Rook and bishop stuff:
        for (int i = 0; i < 8; i++) {
            int dx = (i > 0 && i < 4) ? 1 : (i > 4) ? -1 : 0;
            int dy = (i > 2 && i < 6) ? -1 : (i == 2 || i ==  6) ? 0 : 1;
            if ((i % 2 == 0 && rook) || (i % 2 == 1 && bishop)) {
                output.addAll(getLineMovesInDirection(startPos, dx, dy, maxDist));
            }
        }

        if (knight) {
            for (int i = 0; i < 8; i++) {
                int dx = (i == 0 || i == 3) ? 1 : (i == 4 || i == 7) ? -1 : (i == 1 || i == 2) ? 2 : -2;
                int dy = (i == 0 || i == 7) ? -2 : (i == 1 || i == 6) ? -1 : (i == 2 || i == 5) ? 1 : 2;
                output.addAll(getLineMovesInDirection(startPos, dx, dy, maxDist));
            }
        }

        return output;
    }

    private List<Move> getLineMovesInDirection(int startPos, int dx, int dy, int maxDist) {
        ArrayList<Move> output = new ArrayList<>();
        int currentX = startPos % 8;
        int currentY = startPos / 8;

        for (int x = 0; x < maxDist; x++) {
            currentX += dx;
            currentY += dy;
            if (currentX > 7 || currentY > 7 || currentX < 0 || currentY < 0) {
                break;
            }

            Move thisMove = new NormalMove(startPos, currentX + 8*currentY);

            // If the spot is empty, add to output and continue.
            if (_layout[currentX + 8*currentY] == 0) {
                output.add(thisMove);
                continue;
            }

            // If the color of this spot is the opposite of the color of the "from" piece, add this move.
            if (_layout[currentX + 8*currentY] > 0 == _layout[startPos] < 0) {
                output.add(thisMove);
            }

            // Quit, since another piece was hit.
            break;
        }

        return output;
    }

    public int pieceId(byte piece) {
        return _layout[piece];
    }
}