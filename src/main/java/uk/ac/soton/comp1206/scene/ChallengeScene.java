package uk.ac.soton.comp1206.scene;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.*;
import javafx.scene.shape.Box;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
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

    private GameBoard board;

    private PieceBoard nextPieceBoard;
    private PieceBoard followingPieceBoard;

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    protected Game game;
    private IntegerProperty currentAimX = new SimpleIntegerProperty(0);
    private IntegerProperty currentAimY = new SimpleIntegerProperty(0);


    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
        Multimedia.playMusic("game.wav");
        this.nextPieceBoard = new PieceBoard(new Game(3, 3), 110, 110);
        this.followingPieceBoard = new PieceBoard(new Game(3, 3), 75, 75);
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
        challengePane.getStyleClass().add("challenge-background");
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        board =
            new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);
        board.getStyleClass().add("gameBox");

        mainPane.setCenter(board);

        //Statistics

        //Boards
        nextPieceBoard.getStyleClass().add("nextPieceBoard");

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

        //Title
        var title = new Text("Challenge Mode");
        title.getStyleClass().add("button-glow");
        var incomingText = new Text("Incoming");
        incomingText.getStyleClass().add("button-glow-red");

        //Top section
        var topSection = new HBox(140);
        topSection.setPadding(new Insets(10, 0, 0, 0));
        topSection.setAlignment(Pos.CENTER);
        topSection.getChildren().addAll(scoreBox, title, livesBox);
        mainPane.setTop(topSection);

        //Left section
        var leftSection = new VBox(5);
        leftSection.setPadding(new Insets(0, 0, 0, 20));
        leftSection.setAlignment(Pos.CENTER);
        //leftSection.getChildren().add()
        mainPane.setLeft(leftSection);

        //Right section
        var rightSection = new VBox();
        rightSection.setPadding(new Insets(0, 20, 0, 0));
        rightSection.setSpacing(20);
        rightSection.setAlignment(Pos.CENTER);
        rightSection.getChildren()
            .addAll(levelBox, multiplierBox, incomingText, nextPieceBoard, followingPieceBoard);
        mainPane.setRight(rightSection);

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
        game.blockClicked(gameBlock);
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
    private void setKeyboard() {
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
                    gameWindow.startMenu();
                    break;
                case LEFT:
                    currentAimY.set(Math.max(0, currentAimY.get() - 1));
                    break;
                case RIGHT:
                    currentAimY.set(Math.min(game.getRows() - 1, currentAimY.get() + 1));
                    break;
                case UP:
                    currentAimX.set(Math.max(0, currentAimX.get() - 1));
                    break;
                case DOWN:
                    currentAimX.set(Math.min(game.getCols() - 1, currentAimX.get() + 1));
                    break;
                case ENTER, X:
                    dropPiece();
                    break;
            }
        });

        // Center aim when the game starts or resets
        centerAim();

        // Update aim on the board when currentAimX or currentAimY changes
        currentAimX.addListener(
            (obs, oldVal, newVal) -> board.updateAim(newVal.intValue(), currentAimY.get()));
        currentAimY.addListener(
            (obs, oldVal, newVal) -> board.updateAim(currentAimX.get(), newVal.intValue()));
    }

    /**
     * Method handles: event clicking (on all grids)
     */
    private void handleClick() {
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
     * sets and updates the pieces of the boards with listeners
     */
    private void setPieces() {
        game.setOnNextPieceListener(piece -> {
            // Update the PieceBoard with the new piece
            nextPieceBoard.setPiece(piece);
            logger.info("currentListener updates current piece board");
        });

        game.setOnFollowingPieceListener(piece -> {
            // Update the FollowingPieceBoard with the new following piece
            followingPieceBoard.setPiece(piece);
            logger.info("followingListener updates following piece board");
        });
    }

    /**
     * method drops the piece on the corresponding block in the board
     */
    private void dropPiece() {
        int y = currentAimX.get();
        int x = currentAimY.get();
        game.blockClicked(board.getBlock(x, y));
        logger.info("piece was dropped using the keyboard");
    }

    /**
     * method centers the aim when keyboard is used
      */
    private void centerAim() {
        // Set aim to the center of the board
        currentAimX.set(game.getCols() / 2);
        currentAimY.set(game.getRows() / 2);
        board.updateAim(currentAimX.get(), currentAimY.get());
        logger.info("keyboard used/aim is centered");
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");

        game.start();
        nextPieceBoard.setPiece(game.getCurrentPiece()); // initialised first piece
        followingPieceBoard.setPiece(game.getFollowingPiece());//initialise following piece
        logger.info("next pieces initialised");
        setPieces();
        handleClick();
        setKeyboard();


    }

}
