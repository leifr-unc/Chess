package game;

public interface Player {
    long getNextMove(long[] options);
    boolean getIsWhite();
    boolean isHuman();
}
