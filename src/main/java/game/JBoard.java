package game;

import moves.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    private boolean promptingUser;
    private boolean[] spotsMovable = new boolean[64];
    private boolean[][] spotsMovableInto = new boolean[64][64];
    private long[][] moves = new long[64][64];
    private long userChosenMove = -1;

    private Game _game = null;

    final private static int SIZE = 80; // Pixel width of each square of the board.

    private ImageIcon[] _imageIcons;
    final private static String[] ID_TO_NAME = new String[] {"", "Pawn", "Knight", "Bishop", "Rook", "Queen", "King"};




    public JBoard(boolean whiteIsBottom) {

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

    public void setSpots(int[] spots) {
        for (int i = 0; i < spots.length; i++) {
            _spots[i].setPiece(spots[i], _imageIcons[spots[i] + 6]);
        }
    }

    public void setSpots(int[] spots, long move) {
        for (int i = 0; i < spots.length; i++) {
            _spots[i].setPiece(spots[i], _imageIcons[spots[i] + 6]);
            if (i == MoveUtils.getStart(move) || i == MoveUtils.getEnd(move)) {
                _spots[i].makeBackGroundPartOfMove();
            } else {
                _spots[i].undoBackGroundPartOfMove();
            }
        }
    }

    public long askUserForMove(List<Long> allPossibleChoices) {
        promptingUser = true;
        moves = new long[64][64];
        spotsMovable = new boolean[64];
        spotsMovableInto = new boolean[64][64];
        for (long m : allPossibleChoices) {
            spotsMovable[MoveUtils.getStart(m)] = true;
            spotsMovableInto[MoveUtils.getStart(m)][MoveUtils.getEnd(m)] = true;
            moves[MoveUtils.getStart(m)][MoveUtils.getEnd(m)] = m;
        }

        // Wait for user to input a move
        while (promptingUser && userChosenMove == -1L && !_game._movesUndone) {
            try {
                Thread.sleep(25);
            } catch (InterruptedException ignored) {}
        }

        if (_game._movesUndone) {
            promptingUser = false;
            return 0L;
        }

        long output = userChosenMove;
        userChosenMove = -1l;
        return output;
    }

    public void removeAllHighlighting() {
        for (int i = 0; i < 64; i++) {
            _spots[i].unhighlight();
            _spots[i].undoBackGroundPartOfMove();
        }
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

    private byte[] readIntoByteArray(String path) throws IOException  {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(path);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[8192];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    private void fillImageIcons() {
        _imageIcons = new ImageIcon[13];
        for (int i = -6; i < 7; i++) {
            String path;
            if (i != 0) {
                path = "img/" + (i > 0 ? "White" : "Black") + ID_TO_NAME[(i > 0 ? i : 0 - i)] + ".png";
            } else {
                path = "img/Transparent.png";
            }
//            try {
                ImageIcon imgIcon = new ImageIcon(path);
                BufferedImage bi = new BufferedImage(SIZE, SIZE, BufferedImage.TRANSLUCENT);

                Graphics2D bGr = bi.createGraphics();
                bGr.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                bGr.drawImage(imgIcon.getImage(), 0, 0, SIZE, SIZE, null);
                bGr.dispose();
                _imageIcons[i + 6] = new ImageIcon(bi);
//            } catch (IOException e) {
//                System.out.println("File not found: " + path);
//                e.printStackTrace();
//            }
        }
    }

    public void setGame(Game game) {
        _game = game;
    }
}
