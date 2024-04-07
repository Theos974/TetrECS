package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The FollowingPiece listener is used to handle the event when a new following piece is set
 *
 */
public interface FollowingPieceListener {
    /**
     * handles the following piece
     * @param piece: the following piece that was set
     */
    void nextPiece(GamePiece piece);
}
