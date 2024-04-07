package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

/**
 * PieceBoard class extending GameBoard to draw the next Pieces Boards
 */
public class PieceBoard extends GameBoard {


    /**
     * Constructor: takes from GameBoard
      * @param grid: required to form grid
     * @param width:sets width of grid
     * @param height:sets height of grid
     */
    public PieceBoard(Grid grid, double width, double height) {
        super(grid, width, height);

    }


    /**
     * sets new piece on the new piece boards
     * @param piece: used to draw the correct piece on the grid
     */
    public void setPiece(GamePiece piece) {

        int[][] pieceBlocks = piece.getBlocks();

        clearPieceGrid();

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
     * clears the new piece board
     * Makes all values of the grid 0
     */
    public void clearPieceGrid() {


        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {

                grid.set(col, row, 0);

            }
        }
        logger.info("cleared previous block");
    }


}
