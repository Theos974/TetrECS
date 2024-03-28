package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InstructionsScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger(InstructionsScene.class);


    public InstructionsScene(GameWindow gameWindow) {
        super(gameWindow);
    }

    @Override
    public void build() {

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        //Base
        var instructionsPane = new StackPane();
        instructionsPane.setMaxWidth(gameWindow.getWidth());
        instructionsPane.setMaxHeight(gameWindow.getHeight());
        instructionsPane.getStyleClass().add("menu-background3");


        var mainPane = new BorderPane();

        // TOP area

        Text instructionText = new Text("Instructions:");

        HBox topSection = new HBox();
        topSection.setAlignment(Pos.CENTER);

        instructionText.getStyleClass().add("instructions");
        topSection.getChildren().add(instructionText);
        mainPane.setTop(topSection);


        //Center area

        // Creates an image for the instructions
        Image instructionsImage =
            new Image(getClass().getResourceAsStream("/images/Instructions.png"));
        ImageView instructionsImageView = new ImageView(instructionsImage);
        instructionsImageView.setPreserveRatio(true);
        instructionsImageView.setFitWidth(600); // Sets the width or height as needed

        var piecesTitle = new Text("Available Pieces:");
        piecesTitle.getStyleClass().add("instructions-glow");

        var centerBox = new VBox();
        centerBox.setAlignment(Pos.CENTER);
        mainPane.setCenter(centerBox);

        // Creates a GridPane to display PieceBoards
        var pieceGrid = new VBox();
        pieceGrid.setAlignment(Pos.CENTER);
        pieceGrid.setSpacing(10);

        //loop to create rows for the pieces
        for (int i = 0; i < 3; i++) {
            var pieceRow = new HBox();
            pieceRow.setAlignment(Pos.CENTER);
            pieceRow.setSpacing(10);
            pieceGrid.getChildren().add(pieceRow);
            logger.info("rows created");
            // Loop to create and display all 15 pieces
            for (int y = 0; y < 5; y++) {
                GamePiece piece = GamePiece.createPiece(i * 5 + y);
                PieceBoard pieceBoard =
                    new PieceBoard(new Game(3, 3), 50, 40);
                pieceBoard.setPiece(piece);
                pieceRow.getChildren().add(pieceBoard);
            }
        }
        centerBox.getChildren().addAll(instructionsImageView, piecesTitle, pieceGrid);

        root.getChildren().add(instructionsPane);
        instructionsPane.getChildren().add(mainPane);
    }

    @Override
    public void initialise() {

        this.scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                Multimedia.playAudio("transition.wav");
                gameWindow.startMenu();
                logger.info("pressed escape to go back");
            }
        });

    }


}
