package game;

import moves.Move;
import moves.PawnPromotionChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
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

    final private static int SIZE = 80; // Pixel width of each square of the board.

    private ImageIcon[] _imageIcons;
    final private static String[] ID_TO_NAME = new String[] {"", "Pawn", "Knight", "Bishop", "Rook", "Queen", "King"};


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

        fillImageIcons();

        _spots = new JGridSpot[64];
        setLayout(new GridLayout(8, 8));
        for (int i = 0; i < _spots.length; i++) {
            _spots[i] = new JGridSpot(i);
            _spots[i].setPiece(0, _imageIcons[6]);
            _spots[i].addChessSpotListener(this);
            _spots[i].setPreferredSize(new Dimension(SIZE, SIZE));
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
            _spots[i].setPiece(spots[i], _imageIcons[spots[i] + 6]);
        }
    }

    public void setSpots(byte[] spots, Move move) {
        for (int i = 0; i < spots.length; i++) {
            _spots[i].setPiece(spots[i], _imageIcons[spots[i] + 6]);
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
                Thread.sleep(25);
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

    private void fillImageIcons() {
        _imageIcons = new ImageIcon[13];
        for (int i = -6; i < 7; i++) {
            ImageIcon imgIcon;
            if (i != 0) {
                imgIcon = new ImageIcon("img/" + (i > 0 ? "White" : "Black") + ID_TO_NAME[(i > 0 ? i : 0 - i)] + ".png");
            } else {
                imgIcon = new ImageIcon("img/Transparent.png");
            }

            BufferedImage bi = new BufferedImage(SIZE, SIZE, BufferedImage.TRANSLUCENT);

            Graphics2D bGr = bi.createGraphics();
            bGr.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            bGr.drawImage(imgIcon.getImage(), 0, 0, SIZE, SIZE, null);
            bGr.dispose();
            _imageIcons[i+6] = new ImageIcon(bi);
        }
    }
}
