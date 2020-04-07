package main.java.game;

public interface Player {
    long getNextMove(long[] options);
    boolean isHuman();
}
