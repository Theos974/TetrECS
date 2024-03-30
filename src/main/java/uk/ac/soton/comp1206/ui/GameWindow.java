package uk.ac.soton.comp1206.ui;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.scene.*;

/**
 * The GameWindow is the single window for the game where everything takes place. To move between screens in the game,
 * we simply change the scene.
 * <p>
 * The GameWindow has methods to launch each of the different parts of the game by switching scenes. You can add more
 * methods here to add more screens to the game.
 */
public class GameWindow {

    private static final Logger logger = LogManager.getLogger(GameWindow.class);

    private final int width;
    private final int height;

    private final Stage stage;

    private BaseScene currentScene;
    private Scene scene;

    final Communicator communicator;


    /**
     * Create a new GameWindow attached to the given stage with the specified width and height
     *
     * @param stage  stage
     * @param width  width
     * @param height height
     */
    public GameWindow(Stage stage, int width, int height) {
        this.width = width;
        this.height = height;

        this.stage = stage;

        //Setup window
        setupStage();

        //Setup resources
        setupResources();

        //Setup default scene
        setupDefaultScene();

        //Setup communicator
        communicator = new Communicator("ws://ofb-labs.soton.ac.uk:9700");

        //initial scene started
        start();

    }

    /**
     * Setup the font and any other resources we need
     */
    private void setupResources() {
        logger.info("Loading resources");

        //We need to load fonts here due to the Font loader bug with spaces in URLs in the CSS files
        Font.loadFont(getClass().getResourceAsStream("/style/Orbitron-Regular.ttf"), 32);
        Font.loadFont(getClass().getResourceAsStream("/style/Orbitron-Bold.ttf"), 32);
        Font.loadFont(getClass().getResourceAsStream("/style/Orbitron-ExtraBold.ttf"), 32);
    }

    /**
     * Display the main menu
     */
    public void startMenu() {
        Multimedia.stopMusic();
        logger.info("Switching to MenuScene");
        Multimedia.playMusic("menu.mp3");
        loadScene(new MenuScene(this));
    }

    /**
     * displays the fade in intro scene
     */
    public void start() {
        // Create an initial Pane for the fade in image
        logger.info("fade in scene");
        Multimedia.playAudio("intro.mp3");
        StackPane splashScreen = new StackPane();
        splashScreen.setStyle("-fx-background-color: black;");
        Image image = new Image(getClass().getResourceAsStream("/images/ECSGames.png"));
        ImageView imageView = new ImageView(image);
        imageView.fitWidthProperty().bind(splashScreen.widthProperty().multiply(0.75));
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        splashScreen.getChildren().add(imageView);

        // Set the initial Scene
        Scene initialScene = new Scene(splashScreen, width, height);
        stage.setScene(initialScene);
        stage.show();

        // Create a fade transition for the image
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(4), imageView);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.setOnFinished(event -> startMenu()); // After fade in, start the menu
        fadeTransition.play();
    }

    /**
     * Display the single player challenge
     */
    public void startChallenge() {

        Multimedia.stopMusic();
        logger.info("menu music stopped");
        loadScene(new ChallengeScene(this));
    }

    /**
     * loads the instruction scene
     */
    public void startInstructions() {
        InstructionsScene instructionsScene = new InstructionsScene(this);
        loadScene(instructionsScene);
    }

    public void startScoreScene(Game game) {

        logger.info("displaying score Scene");
        Multimedia.stopMusic();
        Multimedia.playMusic("end.wav");
        var scoresScene = new ScoresScene(this, game);
        loadScene(scoresScene);


    }


    /**
     * Setup the default settings for the stage itself (the window), such as the title and minimum width and height.
     */
    public void setupStage() {
        stage.setTitle("TetrECS");
        stage.setMinWidth(width);
        stage.setMinHeight(height + 20);
        stage.setOnCloseRequest(ev -> App.getInstance().shutdown());
    }

    /**
     * Load a given scene which extends BaseScene and switch over.
     *
     * @param newScene new scene to load
     */
    public void loadScene(BaseScene newScene) {
        //Cleanup remains of the previous scene
        cleanup();

        //Create the new scene and set it up
        newScene.build();
        currentScene = newScene;
        scene = newScene.setScene();
        stage.setScene(scene);

        //Initialise the scene when ready
        Platform.runLater(() -> currentScene.initialise());
    }

    /**
     * Setup the default scene (an empty black scene) when no scene is loaded
     */
    public void setupDefaultScene() {
        this.scene = new Scene(new Pane(), width, height, Color.BLACK);
        stage.setScene(this.scene);
    }

    /**
     * When switching scenes, perform any cleanup needed, such as removing previous listeners
     */
    public void cleanup() {
        logger.info("Clearing up previous scene");
        communicator.clearListeners();
    }

    /**
     * Get the current scene being displayed
     *
     * @return scene
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Get the width of the Game Window
     *
     * @return width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Get the height of the Game Window
     *
     * @return height
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Get the communicator
     *
     * @return communicator
     */
    public Communicator getCommunicator() {
        return communicator;
    }
}
