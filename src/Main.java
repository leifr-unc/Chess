import game.ChessWidget;
import game.Game;

public class Main {
    public static void main(String[] args) {
        ChessWidget.makeFrame(Math.random() < 0.5, Game.Type.CHESS);
    }
}
