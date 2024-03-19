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
import javafx.scene.layout.VBox;
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


        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button-glow-red");
        backButton.setOnAction(e -> gameWindow.startMenu());

        // alligns button top left
        HBox backButtonBox = new HBox(backButton);
        backButtonBox.setAlignment(Pos.TOP_LEFT);


        // Creates an image for the instructions
        Image instructionsImage =
            new Image(getClass().getResourceAsStream("/images/Instructions.png"));
        ImageView instructionsImageView = new ImageView(instructionsImage);
        instructionsImageView.setPreserveRatio(true);

        instructionsImageView.setFitWidth(600); // Sets the width or height as needed
        //places image centered
        HBox imageBox = new HBox(instructionsImageView);
        imageBox.setAlignment(Pos.TOP_CENTER);


        //pieces label
        Label piecesTitle = new Label("Available Pieces:");
        piecesTitle.getStyleClass().add("button-glow");

        // Creates a GridPane to display PieceBoards
        GridPane pieceGrid = new GridPane();
        pieceGrid.setVgap(8);
        pieceGrid.setHgap(8);
        pieceGrid.setAlignment(Pos.CENTER);


        // Loop to create and display all 15 pieces
        for (int i = 0; i < 15; i++) {
            GamePiece piece = GamePiece.createPiece(i);
            PieceBoard pieceBoard =
                new PieceBoard(new Game(3, 3), 50, 40);

            pieceBoard.setPiece(piece);
            pieceGrid.add(pieceBoard, i % 8, i / 8); // Arrange in a grid 5 wide

        }
        logger.info("displaying all 15 pieces");

        VBox piecesLayout = new VBox(10); // Spacing between elements
        piecesLayout.setAlignment(Pos.CENTER);
        piecesLayout.getChildren().addAll(piecesTitle, pieceGrid);

        BorderPane mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("menu-background3");

        //Now sets the combined HBox in Vbox/container for both
        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(backButtonBox, imageBox);

        // BorderPane's top set with the container holding both elements
        mainLayout.setTop(topContainer);

        // Adding the pieces layout in the center of the BorderPane
        mainLayout.setCenter(piecesLayout);

        // Sets the main layout as the root of the scene
        root.getChildren().add(mainLayout);
    }

    @Override
    public void initialise() {

        this.scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                gameWindow.startMenu();
            }
        });
    }


}
