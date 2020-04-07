package main.java.game;

import main.java.board.Board;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.html.ImageView;

/*
 * JGridSpot class:
 * -> Is part of a JBoard, the only outside object this is allowed to work with.
 * -> The JBoard manages everything to do with this, including hovering behavior and onClick actions.
 * -> Displays a single square on a chess board.
 */

public class JGridSpot extends JPanel implements MouseListener {

    final private static Color SELECTABLE_COLOR   = new Color(143/255f, 188/255f, 143/255f);
    final private static Color SELECTED_COLOR     = new Color(143/255f, 188/255f, 143/255f);
    final private static Color MOVEABLE_COLOR     = new Color(143/255f, 188/255f, 143/255f);
    final private static Color LIGHT_BG           = new Color(240/255f, 217/255f, 181/255f);
    final private static Color DARK_BG            = new Color(181/255f, 136/255f, 99/255f);
    final private static Color PART_OF_MOVE_DARK  = new Color(170/255f, 162/255f, 58/255f);
    final private static Color PART_OF_MOVE_LIGHT = new Color(205/255f, 210/255f, 106/255f);

    private int _position;
    private Color _backGroundColor, _moveBackGroundColor;
    private boolean _useMoveBackGround;
    private Color _active_highlight;
    private int _piece;
    private JLabel _icon;

    private ArrayList<ChessSpotListener> _listeners = new ArrayList<>();

    public JGridSpot(int position) {
        _backGroundColor = (position%2 + (position/8)%2 == 1) ? LIGHT_BG : DARK_BG;
        _moveBackGroundColor = (position%2 + (position/8)%2 == 1) ? PART_OF_MOVE_LIGHT : PART_OF_MOVE_DARK;
        _position = position;
        setBackground(_backGroundColor);

        // For handling the images
        setLayout(new BorderLayout());
        _icon = new JLabel();
        add(_icon, BorderLayout.CENTER);

        addMouseListener(this);
    }

    public int getPosition() {
        return _position;
    }

    public void highlightSelectable() {
        _active_highlight = SELECTABLE_COLOR;
        trigger_update();
    }

    public void highlightSelected() {
        setBackground(SELECTED_COLOR);
        trigger_update();
    }

    public void highlightMoveable() {
        _active_highlight = MOVEABLE_COLOR;
        trigger_update();
    }

    public void unhighlight() {
        _active_highlight = null;
        setBackground(_useMoveBackGround ? _moveBackGroundColor : _backGroundColor);
        trigger_update();
    }

    public int getPiece() {
        return _piece;
    }

    public void setPiece(int piece, ImageIcon imageIcon) {
        if (_piece == piece) return;

        _piece = piece;

        _icon.setIcon(imageIcon);
        _icon.setHorizontalAlignment(SwingConstants.CENTER);
        _icon.setVerticalAlignment(SwingConstants.CENTER);

        trigger_update();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        if (_active_highlight != null) {
            g2d.setColor(_active_highlight);
            g2d.setStroke(new BasicStroke(4));
            g2d.drawRect(0, 0, getWidth(), getHeight());
        }
    }

    private void trigger_update() {
        repaint();

        new Thread(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
            repaint();
        }).start();

    }

    public void addChessSpotListener(ChessSpotListener c) {
        _listeners.add(c);
    }

    public void removeChessSpotListener(ChessSpotListener c) {
        _listeners.remove(c);
    }

    public void makeBackGroundPartOfMove() {
        if (_useMoveBackGround) return;
        setBackground(_moveBackGroundColor);
        _useMoveBackGround = true;
        trigger_update();
    }

    public void undoBackGroundPartOfMove() {
        if (!_useMoveBackGround) return;
        setBackground(_backGroundColor);
        _useMoveBackGround = false;
        trigger_update();
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        for (ChessSpotListener c : _listeners) {
            c.spotClicked(_position);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {
        for (ChessSpotListener c : _listeners) {
            c.spotEntered(_position);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        for (ChessSpotListener c : _listeners) {
            c.spotExited(_position);
        }
    }
}