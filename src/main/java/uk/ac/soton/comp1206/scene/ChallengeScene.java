package uk.ac.soton.comp1206.scene;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.*;
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
    private final Label scoreLabel = new Label("Score: 0");
    private final Label levelLabel = new Label("Level: 0");
    private final Label livesLabel = new Label("Lives: 3");
    private final Label multiplierLabel = new Label("Multiplier: 1x");
    private final VBox stats = new VBox(10);

    private PieceBoard nextPieceBoard;


    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    protected Game game;

    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
        Multimedia.playMusic("game.wav");
        this.nextPieceBoard = new PieceBoard(new Game(3, 3), 150, 150);
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

        var board =
            new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);
        mainPane.setCenter(board);
        mainPane.setRight(stats);


        //Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);


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
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);

        scoreLabel.textProperty().bind(game.scoreProperty().asString("Score: %d"));
        levelLabel.textProperty().bind(game.levelProperty().asString("Level: %d"));
        livesLabel.textProperty().bind(game.livesProperty().asString("Lives: %d"));
        multiplierLabel.textProperty().bind(game.multiplierProperty().asString("Multiplier: x%d"));

        // Style the stats labels
        scoreLabel.getStyleClass().add("button-glow");
        levelLabel.getStyleClass().add("button-glow");
        livesLabel.getStyleClass().add("button-glow-red");
        multiplierLabel.getStyleClass().add("button-glow-red");

        // Configure the stats VBox
        stats.setAlignment(Pos.TOP_RIGHT); // Align to the top right of the VBox
        stats.setPadding(new Insets(20)); // Add some padding around the VBox
        stats.setSpacing(10); // Add spacing between elements in the VBox

        logger.info("Game stats UI elements bound to game properties");
        stats.getChildren()
            .addAll(scoreLabel, levelLabel, livesLabel, multiplierLabel, nextPieceBoard);

    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");

        game.start();
        nextPieceBoard.setPiece(game.getCurrentPiece());
        game.setOnNextPieceListener(piece -> {
            // Update the PieceBoard with the new piece
            nextPieceBoard.setPiece(piece);
        });
    }

}
