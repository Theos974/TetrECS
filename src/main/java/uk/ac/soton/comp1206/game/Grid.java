package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 * <p>
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 * <p>
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 * <p>
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for (var y = 0; y < rows; y++) {
            for (var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }


    /**
     * iterates through the pieces grid to see whether the corresponding places in the games grid are occupied to check if it can place the piece
     * @param piece
     * @param centerX
     * @param centerY
     * @return
     */
    public boolean canPlayPiece(GamePiece piece, int centerX, int centerY) {
        int[][] blocks = piece.getBlocks();
        int centerBlockX = blocks[0].length / 2; //center x value of piece
        int centerBlockY = blocks.length / 2; //centre y value of piece

        for (int row = 0; row < blocks.length; row++) {
            for (int col = 0; col < blocks[row].length; col++) {
                if (blocks[row][col] != 0) {
                    int gridX = centerX + (col - centerBlockX); // finds the positions of the current piece elements in the grid
                    int gridY = centerY + (row - centerBlockY);

                    if (gridX < 0 || gridX >= getCols() || gridY < 0 || gridY >= getRows() || get(gridX, gridY) != 0) { //makes sure the piece is not out of bounds
                        return false;
                    }
                }
            }
        }
        return true;
    }



    /**
     * if the piece can be played it changes the grid to also git the values of the new piece
     * @param piece
     * @param centerX
     * @param centerY
     */
    public void playPiece(GamePiece piece, int centerX, int centerY) {
        if (canPlayPiece(piece, centerX, centerY)) {
            int[][] blocks = piece.getBlocks();
            int centerBlockX = blocks[0].length / 2;
            int centerBlockY = blocks.length / 2;

            for (int row = 0; row < blocks.length; row++) {
                for (int col = 0; col < blocks[row].length; col++) {
                    if (blocks[row][col] != 0) {
                        int gridX = centerX + (col - centerBlockX);
                        int gridY = centerY + (row - centerBlockY);

                        set(gridX, gridY, blocks[row][col]);
                    }
                }
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     *
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     *
     * @param x     column
     * @param y     row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     *
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * Get the number of columns in this game
     *
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     *
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

}