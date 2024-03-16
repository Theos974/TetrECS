package uk.ac.soton.comp1206.game;

import java.util.HashSet;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    protected GamePiece currentPiece;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols, rows);
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        currentPiece = spawnPiece();
    }

    /**
     * Handle what should happen when a particular block is clicked
     *
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        if (grid.canPlayPiece(currentPiece, x, y)) {

            logger.info("Attempting to place piece");

            grid.playPiece(currentPiece, x, y);
            afterPiece();
            nextPiece();
        } else {
            logger.info("Piece cannot be played at the specified at " + x + ", " + y);

        }


    }

    /**
     * Get the grid model inside this game representing the game state of the board
     *
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
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

    /**
     * gets a new piece to place
     *
     * @return
     */
    public GamePiece spawnPiece() {
        Random random = new Random();
        return GamePiece.createPiece(random.nextInt(15));
    }

    /**
     * gets the next piece
     */
    public void nextPiece() {
        currentPiece = spawnPiece();
    }

    /**
     * clears the lines
      */
    public void afterPiece() {

        HashSet<GameBlockCoordinate> blocksToClear = new HashSet<>();

        for (int row = 0; row < getRows(); row++) { //checks for full lines
            boolean fullRow = true;
            for (int col = 0; col < getCols(); col++) {
                if (grid.get(row, col) == 0) {
                    fullRow = false;
                    break;
                }
            }
            if (fullRow) { //if there is a full row it adds the blocks of the line that need to be cleared in the set
                for (int col = 0; col < getCols(); col++) {
                    blocksToClear.add(new GameBlockCoordinate(col, row));
                }
                logger.info("Full row found and will be cleared: " + row);
            }
        }

        for (int col = 0; col < getCols(); col++) { //checks for full lines
            boolean fullCol = true;
            for (int row = 0; row < getRows(); row++) {
                if (grid.get(row, col) == 0) {
                    fullCol = false;
                    break;
                }
            }
            if (fullCol) { //if there is a full column it adds the blocks of the line that need to be cleared in the set
                for (int row = 0; row < getRows(); row++) {
                    blocksToClear.add(new GameBlockCoordinate(col, row));
                }
                logger.info("Full column found and will be cleared: " + col);
            }

        }

        for (GameBlockCoordinate cord : blocksToClear) { //clears the blocks
            grid.set(cord.getX(), cord.getY(), 0);
        }

    }
}
