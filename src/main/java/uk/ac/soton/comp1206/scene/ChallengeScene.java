package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import javafx.animation.Animation;
import javafx.animation.FillTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    protected GameBoard board;

    protected PieceBoard nextPieceBoard;
    protected PieceBoard followingPieceBoard;
    protected Rectangle timerBar;
    private Timeline timeline;
    protected VBox bottomBox;
    protected TextField chatField;
    protected BorderPane mainPane;
    protected VBox centerBox;
    protected VBox leftSection;
    protected Text title;
    private final IntegerProperty highScore = new SimpleIntegerProperty();

    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);
    protected Game game;

    protected int x, y = 0;

    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
        this.nextPieceBoard = new PieceBoard(new Grid(3, 3), 110, 100);
        this.followingPieceBoard = new PieceBoard(new Grid(3, 3), 75, 75);
    }

    public void buildBoard() {
        board =
            new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {

        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());


        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add(SettingsScene.gameTheme.getText());
        root.getChildren().add(challengePane);

        mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);


        //Center Section
        centerBox = new VBox();
        buildBoard();
        board.getStyleClass().add("gameBox");
        centerBox.getChildren().add(board);
        centerBox.setPadding(new Insets(0,0,0,40));
        centerBox.setAlignment(Pos.CENTER);
        mainPane.setCenter(centerBox);

        //Statistics

        //Boards
        nextPieceBoard.getStyleClass().add("nextPieceBoard");
        nextPieceBoard.blocks[1][1].isCenterBlock();

        //score
        var scoreBox = new VBox();
        scoreBox.setAlignment(Pos.CENTER);
        var scoreText = new Text("Score");
        scoreText.getStyleClass().add("heading");
        var score = new Text();
        score.getStyleClass().add("score");
        score.textProperty().bind(game.scoreProperty().asString());
        scoreBox.getChildren().addAll(scoreText, score);

        //level
        var levelBox = new VBox();
        levelBox.setAlignment(Pos.CENTER);
        var levelText = new Text("Level");
        levelText.getStyleClass().add("heading");
        var level = new Text();
        level.getStyleClass().add("level");
        level.textProperty().bind(game.levelProperty().asString());
        levelBox.getChildren().addAll(levelText, level);

        //Lives

        var livesBox = new VBox();
        livesBox.setAlignment(Pos.CENTER);
        var livesText = new Text("Lives");
        livesText.getStyleClass().add("heading");
        var lives = new Text();
        lives.getStyleClass().add("lives");
        lives.textProperty().bind(game.livesProperty().asString());
        livesBox.getChildren().addAll(livesText, lives);

        //multiplier

        var multiplierBox = new VBox();
        multiplierBox.setAlignment(Pos.CENTER);
        var multiplierText = new Text("Multiplier");
        multiplierText.getStyleClass().add("heading");
        var multiplier = new Text();
        multiplier.getStyleClass().add("multiplier");
        multiplier.textProperty().bind(game.multiplierProperty().asString());
        multiplierBox.getChildren().addAll(multiplierText, multiplier);

        //hiScore
        var hiBox = new VBox();
        hiBox.setAlignment(Pos.CENTER);
        var hiScoreText = new Text("High Score");
        hiScoreText.getStyleClass().add("heading");
        Text highScoreNum = new Text();
        highScoreNum.textProperty().bind(highScore.asString());
        highScoreNum.getStyleClass().add("hi-score");
        hiBox.getChildren().addAll(hiScoreText, highScoreNum);

        //Title
        title = new Text("Challenge Mode");
        title.getStyleClass().add("button-glow");
        var incomingText = new Text("Incoming");
        incomingText.getStyleClass().add("glow-red");

        //Top section
        var topSection = new HBox(140);
        topSection.setPadding(new Insets(10, 0, 0, 0));
        topSection.setAlignment(Pos.CENTER);
        topSection.getChildren().addAll(scoreBox, title, livesBox);
        mainPane.setTop(topSection);

        //Left section

        leftSection = new VBox();


        //Right section
        var rightSection = new VBox();
        rightSection.setPadding(new Insets(0, 15, 0, 0));
        rightSection.setSpacing(20);
        rightSection.setAlignment(Pos.CENTER);
        rightSection.getChildren()
            .addAll(hiBox, levelBox, multiplierBox, incomingText,
                nextPieceBoard, followingPieceBoard);
        mainPane.setRight(rightSection);

        hiBox.setId("hiBox");
        levelBox.setId("levelBox");
        multiplierBox.setId("multiplierBox");

        //Bottom
        bottomBox = new VBox();

        var timerBox = new HBox();
        timerBar = new Rectangle(0, 0, gameWindow.getWidth(), 15); // Assuming a height of 15 pixels
        timerBar.prefHeight(10);
        timerBar.getStyleClass().add("timer-bar");


        timerBox.getChildren().add(timerBar);
        bottomBox.getChildren().add(timerBox);
        mainPane.setBottom(bottomBox);


        BorderPane.setMargin(board, new Insets(0, 0, 0, 30));


    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);

    }

    /**
     * Handle when a block is clicked
     *
     * @param gameBlock the Game Block that was clocked
     */
    private void blockClicked(GameBlock gameBlock) {
        boolean success = game.blockClicked(gameBlock);
        if (success) {
            resetTimerBar();
            game.resetGameLoop();
        }

    }

    /**
     * Method calls the rotateCurrentPiece method in Game class to rotate to the piece
     */
    private void rotate() {
        game.rotateCurrentPiece();
    }

    /**
     * Method sets up the keyboard bindings
     */
    protected void setKeyboard() {
        //manages keyboard bindings
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case Q:
                case Z:
                case OPEN_BRACKET:
                    game.rotateCurrentPiece();
                    break;
                case E:
                case C:
                case CLOSE_BRACKET:
                    game.rotateCurrentPieceCounterClockwise();
                    break;
                case SPACE, R:
                    // Call your method to handle the space key press
                    game.swapCurrentPiece();
                    Multimedia.playAudio("transition.wav");
                    break;
                case ESCAPE:
                    logger.info("escaped pressed");
                    Multimedia.stopMusic();
                    Multimedia.playAudio("transition.wav");
                    game.stopGameLoop();
                    gameWindow.startMenu();

                    break;
                case W:
                case UP:
                    if (y > 0) {
                        y--;
                        board.hover(board.getBlock(x, y));
                        logger.info("up pressed");
                    }
                    break;
                case S:
                case DOWN:
                    if (y < game.getRows() - 1) {
                        y++;
                        board.hover(board.getBlock(x, y));
                        logger.info("down pressed");

                    }
                    break;
                case A:
                case LEFT:
                    if (x > 0) {
                        x--;
                        board.hover(board.getBlock(x, y));
                        logger.info("left pressed");
                    }
                    break;
                case D:
                case RIGHT:
                    if (x < game.getCols() - 1) {
                        x++;
                        board.hover(board.getBlock(x, y));
                        logger.info("right pressed");

                    }
                    break;
                case ENTER, X:
                    dropPiece();
                    logger.info("key pressed to drop piece");
                    break;

            }
        });

    }

    /**
     * Method handles: event clicking (on all grids)
     */
    protected void handleClick() {
        //Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);

        board.setOnRightClick(this::rotate);


        //Handles block on pieceBoard grid being clicked/rotated
        nextPieceBoard.setOnBlockClick(gameBlock -> {
            logger.info("clicked piece board");
            game.rotateCurrentPiece();

        });

        //handles swapping between pieces
        followingPieceBoard.setOnBlockClick(gameBlock -> {
            game.swapCurrentPiece();
            Multimedia.playAudio("transition.wav");
            logger.info("pieces swapped");
        });
    }

    /**
     * method to fade out the blocks
     *
     * @param blocks
     */
    protected void fade(HashSet<GameBlockCoordinate> blocks) {
        board.fadeOut(blocks);
        logger.info("fade activated");
    }

    /**
     * sets and updates the pieces of the boards with listeners
     */
    protected void setPieces() {
        game.setOnNextPieceListener(piece -> {
            // Update the PieceBoard with the new piece
            Platform.runLater(() -> nextPieceBoard.setPiece(piece));
            logger.info("currentListener updates current piece board");
        });

        game.setOnFollowingPieceListener(piece -> {
            // Update the FollowingPieceBoard with the new following piece
            Platform.runLater(() -> followingPieceBoard.setPiece(piece));
            logger.info("followingListener updates following piece board");
        });
    }

    /**
     * method drops the piece on the corresponding block in the board
     */
    protected void dropPiece() {

        boolean success = game.blockClicked(board.getBlock(x, y));
        if (success) {
            resetTimerBar();
            game.resetGameLoop();
        }
        logger.info("dropPiece method for keyBoard used");
    }

    public int getHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader("localScores.txt"))) {
            return reader.lines()
                .map(line -> line.split(":"))
                .filter(split -> split.length == 2)
                .mapToInt(split -> Integer.parseInt(split[1]))
                .max()
                .orElse(0);
        } catch (IOException e) {
            logger.error("Could not read the high scores file");
            return 0;
        }
    }

    public void highScoreSetter() {
        game.scoreProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() > highScore.get()) {
                highScore.set(newValue.intValue());

            }
        });
    }

    protected void updateTimerBar() {
        double fullBarWidth = gameWindow.getWidth();
        double timeInterval = game.getTimerDelay();

        // Update the animation to shrink the timer bar's width over the time interval
        timeline = new Timeline(
            new KeyFrame(
                Duration.ZERO,
                new KeyValue(timerBar.widthProperty(), fullBarWidth),
                new KeyValue(timerBar.fillProperty(), Color.GREEN) // Start with green color
            ),
            new KeyFrame(
                Duration.millis(timeInterval * 0.7), // At 70% of the time interval
                new KeyValue(timerBar.fillProperty(), Color.YELLOW) // Change to yellow color
            ),
            new KeyFrame(
                Duration.millis(timeInterval), // At 100% of the time interval
                new KeyValue(timerBar.widthProperty(), 0),
                new KeyValue(timerBar.fillProperty(), Color.RED) // Change to red color
            )
        );

        // Flashing effect when the time is almost up
        FillTransition fillTransition =
            new FillTransition(Duration.millis(300), timerBar, Color.RED, Color.ORANGE);
        fillTransition.setCycleCount(Animation.INDEFINITE);
        fillTransition.setAutoReverse(true);

        timeline.setOnFinished(e -> fillTransition.play()); // Start flashing when timeline finishes
        timeline.play();
    }


    protected void resetTimerBar() {
        if (timeline != null) {
            timeline.stop(); // Stop the current timeline
        }
        updateTimerBar(); // Create and start a new animation
    }

    protected void gameOver() {
       Platform.runLater(()-> gameWindow.startScoreScene(game));
    }


    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        Multimedia.playMusic("game.wav");
        game.start();
        highScore.set(getHighScore());
        highScoreSetter();
        nextPieceBoard.setPiece(game.getCurrentPiece()); // initialised first piece
        followingPieceBoard.setPiece(game.getFollowingPiece());//initialise following piece
        logger.info("next pieces initialised");
        game.setOnLineClearedListener(this::fade); //sets the listener to clear line
        setPieces();
        handleClick();
        setKeyboard();
        game.setOnGameLoopListener(this::resetTimerBar);
        resetTimerBar();
        game.setGameOverListener(this::gameOver);


    }

}
