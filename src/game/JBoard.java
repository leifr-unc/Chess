package game;

import moves.Move;
import moves.PawnPromotionChooser;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/*
 * JBoard class:
 * -> Is the "View" part of the Model-View-Controller design pattern.
 * -> Acts as a way for the Game to interact with the player.
 * -> Can be added to other UI components.
 */

public class JBoard extends JPanel implements ChessSpotListener {

    private JGridSpot[] _spots;
    private int _selected;

    private boolean _whiteIsBottom;

    private boolean promptingUser;
    private boolean[] spotsMovable = new boolean[64];
    private boolean[][] spotsMovableInto = new boolean[64][64];
    private Move[][] moves = new Move[64][64];
    private Move userChosenMove;

    private PawnPromotionChooser pawnChooser = () -> {
        String[] options = new String[] {"Queen", "Rook", "Bishop", "Knight"};
        int response = JOptionPane.showOptionDialog(null, "Choose new Piece", "Pawn Promotion",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);
        return 5 - response;
    };



    public JBoard(boolean whiteIsBottom) {
        _whiteIsBottom = whiteIsBottom;

        _selected = -1;

        _spots = new JGridSpot[64];
        setLayout(new GridLayout(8, 8));
        for (int i = 0; i < _spots.length; i++) {
            _spots[i] = new JGridSpot(i);
            _spots[i].setPiece(0);
            _spots[i].addChessSpotListener(this);
        }

        // Add components to the grid:
        for (int y = 7; y >= 0; y--) {
            for (int x = 0; x < 8; x++) {
                if (whiteIsBottom) {
                    add(_spots[8 * y + x]);
                } else {
                    add(_spots[8 * (7-y) + (7-x)]);
                }
            }
        }
    }

    public void setSpots(byte[] spots) {
        for (int i = 0; i < spots.length; i++) {
            _spots[i].setPiece(spots[i]);
        }
    }

    public void setSpots(byte[] spots, Move move) {
        for (int i = 0; i < spots.length; i++) {
            _spots[i].setPiece(spots[i]);
            if (i == move.getStart() || i == move.getEnd()) {
                _spots[i].makeBackGroundPartOfMove();
            } else {
                _spots[i].undoBackGroundPartOfMove();
            }
        }
    }

    public PawnPromotionChooser getPawnChooser() {
        return pawnChooser;
    }

    public Move askUserForMove(List<Move> allPossibleChoices) {
        promptingUser = true;
        moves = new Move[64][64];
        spotsMovable = new boolean[64];
        spotsMovableInto = new boolean[64][64];
        for (Move m : allPossibleChoices) {
            if (m.isBotPawnPromotion()) continue;
            spotsMovable[m.getStart()] = true;
            spotsMovableInto[m.getStart()][m.getEnd()] = true;
            moves[m.getStart()][m.getEnd()] = m;
        }

        // Wait for user to input a move
        while (promptingUser && userChosenMove == null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
        }
        Move output = userChosenMove;
        userChosenMove = null;
        return output;
    }

    @Override
    public void spotClicked(int spot) {
        if (_selected == spot) {
            _spots[spot].unhighlight();
            _selected = -1;
        } else if (promptingUser && spotsMovable[spot]) {
            if (_selected != -1) {
                _spots[_selected].unhighlight();
            }
            _selected = spot;
            _spots[spot].highlightSelected();
        } else if (promptingUser && _selected != -1 && spotsMovableInto[_selected][spot]) {
            userChosenMove = moves[_selected][spot];
            promptingUser = false;
            _spots[_selected].unhighlight();
            _spots[spot].unhighlight();
            _selected = -1;
        } else if (_selected != -1) {
            _spots[_selected].unhighlight();
            _selected = -1;
        }
    }

    @Override
    public void spotEntered(int spot) {
        if (promptingUser && spotsMovable[spot] && _selected == -1) {
            _spots[spot].highlightMoveable();
        }

        if (promptingUser && _selected != -1 && spotsMovableInto[_selected][spot]) {
            _spots[spot].highlightSelectable();
        }

        if (promptingUser && _selected != -1 && spotsMovable[spot]) {
            _spots[spot].highlightMoveable();
        }
    }

    @Override
    public void spotExited(int spot) {
        if (_selected != spot) {
            _spots[spot].unhighlight();
        }
    }

}
