package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    private Button start;
    private Button instructionsButton;
    private Button multiplayerButton;

    /**
     * Create a new menu scene
     *
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);


        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background2");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);


        //image title
        var titleImage =
            new ImageView(new Image(getClass().getResourceAsStream("/images/TetrECS.png")));
        titleImage.setPreserveRatio(true);
        titleImage.setFitWidth(600);

        HBox titleBox = new HBox(titleImage);
        titleBox.setAlignment(Pos.CENTER);
        mainPane.setTop(titleBox);
        titleBox.setPadding(new Insets(50,0,0,0));

        //wiggle animation
        RotateTransition rotateTransition = new RotateTransition(Duration.millis(80), titleImage);
        rotateTransition.setByAngle(10);
        rotateTransition.setCycleCount(4);
        rotateTransition.setAutoReverse(true);

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(0), evt -> rotateTransition.play()),
            new KeyFrame(Duration.seconds(2))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        start = new Button("Start");

        start.getStyleClass().add("button-glow");
        //Bind the button action to the startGame method in the menu


        instructionsButton = new Button("How to Play");
        instructionsButton.getStyleClass().add("button-glow");

        multiplayerButton = new Button("Multiplayer");
        multiplayerButton.getStyleClass().add("button-glow");


        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        //centering the buttons
        buttonBox.getChildren().addAll(start,multiplayerButton, instructionsButton);
        mainPane.setCenter(buttonBox);

    }

    /**
     * when start button is clicked the challenge scene is displayed and menu music stops
     * when How to Play button is clicked instruction scene is displayed
     */
    @Override
    public void initialise() {
        start.setOnAction(event -> {
            Multimedia.playAudio("transition.wav");
            gameWindow.startChallenge();
            logger.info("challenge started");
        });

        instructionsButton.setOnAction(event ->{
            Multimedia.playAudio("transition.wav");
            gameWindow.startInstructions();
        });

        multiplayerButton.setOnAction(event -> {
            Multimedia.playAudio("transition.wav");
            gameWindow.startLobbyScene();
        });

    }




}
