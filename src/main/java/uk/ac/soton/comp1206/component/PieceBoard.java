package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PieceBoard extends GameBoard {


    public PieceBoard(Game grid, double width, double height) {
        super(grid.getGrid(), width, height);
    }


    /**
     * sets new piece on the new pieces boards
     *
     * @param piece
     */
    public void setPiece(GamePiece piece) {

        int[][] pieceBlocks = piece.getBlocks();

        clearPieceGrid();
        logger.info("cleared previous block");
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {

                if (pieceBlocks[row][col] != 0) {
                    grid.set(col, row, pieceBlocks[row][col]);

                }
            }
        }
        logger.info("new piece displayed");
    }

    /**
     * clears the new pieces board
     */
    public void clearPieceGrid() {


        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {

                grid.set(col, row, 0);

            }
        }

    }


}
