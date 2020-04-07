package main.java.game;

public interface ChessSpotListener {
    void spotClicked(int spot);
    void spotEntered(int spot);
    void spotExited(int spot);
}
