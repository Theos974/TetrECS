package uk.ac.soton.comp1206.game;

import java.util.HashSet;
import java.util.Random;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.NextPieceListener;

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
    private static final int POINTS_PER_LEVEL = 1000;
    private NextPieceListener nextPieceListener;
    private boolean swoosh = false;

    private final IntegerProperty score = new SimpleIntegerProperty(0);
    private final IntegerProperty level = new SimpleIntegerProperty(0);
    private final IntegerProperty lives = new SimpleIntegerProperty(3);
    private final IntegerProperty multiplier = new SimpleIntegerProperty(1);

    public final int getScore() {
        return score.get();
    }

    public final void setScore(int value) {
        score.set(value);
    }

    public IntegerProperty scoreProperty() {
        return score;
    }

    public final int getLevel() {
        return level.get();
    }

    public final void setLevel(int value) {
        level.set(value);
    }

    public IntegerProperty levelProperty() {
        return level;
    }

    public final int getLives() {
        return lives.get();
    }

    public final void setLives(int value) {
        lives.set(value);
    }

    public IntegerProperty livesProperty() {
        return lives;
    }

    public final int getMultiplier() {
        return multiplier.get();
    }

    public final void setMultiplier(int value) {
        multiplier.set(value);
    }

    public IntegerProperty multiplierProperty() {
        return multiplier;
    }


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

            afterPiece(); //checks for full lines after playing the piece
            if (swoosh){
                Multimedia.playAudio("clear.wav");
            }else {
                Multimedia.playAudio("place.wav");
            }
            nextPiece(); //gets the next piece
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
     * new piece is returned @return
     */
    public GamePiece spawnPiece() {
        Random random = new Random();
        return GamePiece.createPiece(random.nextInt(15));
    }

    /**
     * gets the next piece
     */
    public void nextPiece() {
        GamePiece next = spawnPiece(); // Spawn the next piece
        triggerNextPieceListener(next); // Trigger the listener to handle the new piece
        currentPiece = next; // Set the current piece to the next piece    }
    }

    public GamePiece getCurrentPiece(){
        return currentPiece;
    }

    /**
     * checks for full lines (either vertical or horizontal)  and clears them if they exist
     */
    public void afterPiece() {

        swoosh = false;
        HashSet<GameBlockCoordinate> blocksToClear = new HashSet<>();
        int numLines = 0;

        for (int row = 0; row < getRows(); row++) { //checks for full lines
            boolean fullRow = true;
            for (int col = 0; col < getCols(); col++) {
                if (grid.get(col, row) == 0) {
                    fullRow = false;
                    break;
                }
            }
            if (fullRow) { //if there is a full row it adds the blocks of the line that need to be cleared in the set
                for (int col = 0; col < getCols(); col++) {
                    blocksToClear.add(new GameBlockCoordinate(col, row));
                }
                logger.info("Full row found and will be cleared: " + row);
                numLines++;
            }
        }

        for (int col = 0; col < getCols(); col++) { //checks for full lines
            boolean fullCol = true;
            for (int row = 0; row < getRows(); row++) {
                if (grid.get(col, row) == 0) {
                    fullCol = false;
                    break;
                }
            }
            if (fullCol) { //if there is a full column it adds the blocks of the line that need to be cleared in the set
                for (int row = 0; row < getRows(); row++) {
                    blocksToClear.add(new GameBlockCoordinate(col, row));
                }
                logger.info("Full column found and will be cleared: " + col);
                numLines++;
            }

        }
        if (!blocksToClear.isEmpty()) {
            // swoosh flag becomes true
          swoosh = true;
        }

        for (GameBlockCoordinate cord : blocksToClear) { //clears the blocks
            grid.set(cord.getX(), cord.getY(), 0);
        }
        addScore(numLines, blocksToClear.size());

    }

    /**
     * if any lines are cleared the new score is calculated
     * multiplier is also handled based on whether the lines are cleared or not
     * updating the level is also handled based on the points
     *
     * @param linesCleared
     * @param blocksCleared
     */
    public void addScore(int linesCleared, int blocksCleared) {

        int points;

        if (linesCleared > 0) {

            points = linesCleared * blocksCleared * 10 * getMultiplier(); //calculate points gained
            setScore(getScore() + points); //update score
            logger.info("Score updated. Lines cleared: " + linesCleared + ", Blocks cleared: " + blocksCleared);
            setMultiplier(getMultiplier() + 1);
            logger.info("multiplier increased to " + getMultiplier());

        }else {
            // Reset the multiplier if no lines are cleared
            setMultiplier(1);
            logger.info("Multiplier reset to 1");
        }

        int newLevel = getScore() / POINTS_PER_LEVEL; //update level bsaed on the score
        if (newLevel != getLevel()) {
            setLevel(newLevel);
            Multimedia.playAudio("level.wav");
            logger.info("Level updated");
        }


    }
    public void setOnNextPieceListener(NextPieceListener listener) {
        this.nextPieceListener = listener;
    }


    private void triggerNextPieceListener(GamePiece piece) {
        if(nextPieceListener != null) {
            nextPieceListener.nextPiece(piece);
        }
    }

}
