package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The NextPiece listener is used to handle the event when the next piece is set
 */
public interface NextPieceListener {
    /**
     * handles the next piece
     * @param piece: the next piece that was set
     */
    void nextPiece(GamePiece piece);
}
