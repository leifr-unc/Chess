package moves;

public interface PawnPromotionChooser {
    int getChoice(); // Returns 2 for knight, 3 for bishop, 4 for rook, 5 for queen.  No negatives.
}
