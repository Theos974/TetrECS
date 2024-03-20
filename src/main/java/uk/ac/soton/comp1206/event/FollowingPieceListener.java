package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

public interface FollowingPieceListener {
    void nextPiece(GamePiece piece);
}
