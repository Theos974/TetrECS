package uk.ac.soton.comp1206.event;

import java.util.HashSet;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;

/**
 * Listener used to handle the event when a Line in the grid should be cleared
 */
public interface LineClearedListener {

    /**
     * triggers the line clearing
     * @param coordinates: the blocks that need to be cleared
     */
    public void clearLines(HashSet<GameBlockCoordinate> coordinates);
}
