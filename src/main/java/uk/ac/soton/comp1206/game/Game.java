package uk.ac.soton.comp1206.game;

import java.util.HashSet;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.FollowingPieceListener;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.GameOverListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.ui.GameWindow;

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
    protected GamePiece followingPiece;
    private static final int POINTS_PER_LEVEL = 1000;
    private NextPieceListener nextPieceListener;
    private FollowingPieceListener followingPieceListener;
    private LineClearedListener lineClearedListener = null;
    private GameLoopListener gameLoopListener;
    private GameOverListener gameOverListener;

    private boolean swoosh = false;
    GameWindow gameWindow;

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

    private ScheduledExecutorService executorService =
        Executors.newSingleThreadScheduledExecutor();
    private Runnable gameLoop;


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
        followingPiece = spawnPiece();
        startGameLoop();
    }

    /**
     * Handles what should happen when a particular block is clicked
     *
     * @param gameBlock the block that was clicked
     */
    public boolean blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        if (grid.canPlayPiece(currentPiece, x, y)) {

            grid.playPiece(currentPiece, x, y);

            afterPiece(); //checks for full lines after playing the piece
            if (swoosh) {
                Multimedia.playAudio("clear.wav");
            } else {
                Multimedia.playAudio("place.wav");
            }
            nextPiece(); //gets the next piece
            return true;
        } else {
            logger.info("Piece cannot be played at the specified at " + x + ", " + y);
            return false;
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
     * <p>
     * new piece is returned @return
     */
    public GamePiece spawnPiece() {
        Random random = new Random();
        return GamePiece.createPiece(random.nextInt(15));
    }

    /**
     * orders the next pieces in play
     */
    public void nextPiece() {
        // Makes the following piece the current piece
        currentPiece = followingPiece;

        // Get a new piece to become the new following piece
        GamePiece newFollowing = spawnPiece();

        // Update the followingPiece reference to point to the new piece
        followingPiece = newFollowing;

        // Trigger the listener to update the main piece board with the new current piece
        triggerNextPieceListener(currentPiece);

        // Trigger another listener with the new following piece
        triggerFollowingPieceListener(newFollowing);

    }

    /**
     * The method swaps between the current and following pieces
     */
    public void swapCurrentPiece() {
        if (currentPiece != null && followingPiece != null) {
            GamePiece temp = currentPiece;
            currentPiece = followingPiece;
            followingPiece = temp;
            logger.info("swap method called");
            // Update the listeners to reflect the change in the UI
            triggerNextPieceListener(currentPiece);
            triggerFollowingPieceListener(followingPiece);
        }
    }

    /**
     * The method returns the current piece
     *
     * @return currentPiece
     */
    public GamePiece getCurrentPiece() {
        return currentPiece;
    }

    /**
     * returns the next piece after the current one
     *
     * @return followingPiece
     */
    public GamePiece getFollowingPiece() {
        return followingPiece;
    }


    /**
     * rotates current piece clockwise
     */
    public void rotateCurrentPiece() {

        if (currentPiece != null) {
            currentPiece.rotate();
            Multimedia.playAudio("rotate.wav");
            logger.info("piece rotated");
        }
        triggerNextPieceListener(currentPiece);
        logger.info("rotate method called");
    }

    /**
     * rotates current piece counter clockwise
     */
    public void rotateCurrentPieceCounterClockwise() {
        if (currentPiece != null) {

            currentPiece.rotateCounterClockwise();
            Multimedia.playAudio("rotate.wav");
            triggerNextPieceListener(currentPiece);
            logger.info(" counter clockwise rotate method called");
        }
    }

    /**
     * increases the level based on the players score(every 1000 points)
     */
    public void upgradeLevel() {
        int newLevel = getScore() / POINTS_PER_LEVEL; //update level bsaed on the score
        if (newLevel != getLevel()) {
            setLevel(newLevel);
            Multimedia.playAudio("level.wav");
            logger.info("Level updated");
        }
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
        if (lineClearedListener != null) {
            lineClearedListener.clearLines(blocksToClear);
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
            logger.info("Score updated. Lines cleared: " + linesCleared + ", Blocks cleared: " +
                blocksCleared);
            setMultiplier(getMultiplier() + 1);
            logger.info("multiplier increased to " + getMultiplier());

        } else {
            // Reset the multiplier if no lines are cleared
            setMultiplier(1);
            logger.info("Multiplier reset to 1");
        }
        upgradeLevel();

    }


    /**
     * Method to set the current piece listener
     *
     * @param listener
     */
    public void setOnNextPieceListener(NextPieceListener listener) {
        this.nextPieceListener = listener;
    }


    /**
     * triggers the listener to act
     *
     * @param piece
     */
    private void triggerNextPieceListener(GamePiece piece) {
        if (nextPieceListener != null) {
            nextPieceListener.nextPiece(piece);
        }
    }

    /**
     * triggers the listener to act
     *
     * @param piece
     */
    private void triggerFollowingPieceListener(GamePiece piece) {
        if (followingPieceListener != null) {
            followingPieceListener.nextPiece(piece);
        }
    }

    /**
     * Method to set the following piece listener
     *
     * @param listener
     */
    public void setOnFollowingPieceListener(FollowingPieceListener listener) {
        this.followingPieceListener = listener;
    }

    /**
     * sets the lineCleared listener
     *
     * @param listener
     */
    public void setOnLineClearedListener(LineClearedListener listener) {
        this.lineClearedListener = listener;
    }

    /**
     * calculates the delay per level
     *
     * @return the delay
     */
    public int getTimerDelay() {
        int baseDelay = 12000;
        int level = getLevel();
        int delayDecreasePerLevel = 500;
        int minDelay = 2500;
        int delay = baseDelay - (level * delayDecreasePerLevel);

        return Math.max(delay, minDelay); // Ensure we don't go below the minimum delay

    }

    /**
     * starts the game loop
     */
    public void startGameLoop() {
        gameLoop = this::gameLoop;
        scheduleNextLoop(getTimerDelay());
    }

    /**
     * schedules next loop
     *
     * @param delay
     */
    private void scheduleNextLoop(int delay) {
        executorService.schedule(gameLoop, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * manages each game loop
     */
    private void gameLoop() {
        // What happens in each loop
        // Lose a life, discard the current piece, reset multiplier, check if game should end
        setLives(getLives() - 1);
        Multimedia.playAudio("lifelose.wav");
        if (getLives() == 0) {
            logger.info("inside gameLoop 0 lives");
            endGame();
        } else {
            setMultiplier(1);
            nextPiece();
            logger.info("oops timer to 0");
            if (gameLoopListener != null) {
                gameLoopListener.onGameLoop();
            }


            // Reschedules the next loop
            scheduleNextLoop(getTimerDelay());

        }

    }

    public void setOnGameLoopListener(GameLoopListener listener) {
        this.gameLoopListener = listener;
    }
    public void setGameOverListener(GameOverListener listener){
        this.gameOverListener = listener;
    }

    /**
     * method to reset timeLoop when block is placed
     */
    public void resetGameLoop() {
        logger.info("Attempting to reset the game loop");
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow(); // Immediately stop all currently executing tasks
            executorService = Executors.newSingleThreadScheduledExecutor();
            scheduleNextLoop(getTimerDelay()); // Reschedule the loop with the current delay
        } else {
            logger.info("Executor service is null or already shut down.");
        }
    }


    /**
     * method to handle transitioning to scoreScene after game has ended
     */
    public void endGame() {
        // Shutdown the scheduled tasks
        executorService.shutdown();
        // Notify that the game is over
        if (gameOverListener != null) {
            Platform.runLater(() -> gameOverListener.gameOver());
        }
    }


}

