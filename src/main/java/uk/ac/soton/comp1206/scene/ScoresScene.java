package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScoresScene extends BaseScene {
    private final ScoresList scoresList;
    static final Logger logger = LogManager.getLogger(ScoresScene.class);
    private final Game game;
    VBox centerBox = new VBox();
    HBox leaderBoardsBox = new HBox();
    VBox titleBox;

    private final ListProperty<Pair<String, Integer>> localScoresList;
    private final ListProperty<Pair<String, Integer>> remoteScoresList =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final Communicator communicator;
    private MultiplayerGame multiplayerGame;

    /**
     * ScoreScene class used to display the leaderBoards
      * @param gameWindow:
     * @param game:
     */
    public ScoresScene(GameWindow gameWindow, Game game) {
        super(gameWindow);
        this.game = game;
        this.localScoresList = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.scoresList = new ScoresList();
        scoresList.bindScores(localScoresList);
        communicator = gameWindow.getCommunicator();
        loadScores();
        if (game instanceof MultiplayerGame) {
            this.multiplayerGame = (MultiplayerGame) game;
        }
    }

    /**
     * Method used to build the UI components of the scene
     */
    @Override
    public void build() {

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var scorePane = new StackPane();
        scorePane.setMaxWidth(gameWindow.getWidth());
        scorePane.setMaxHeight(gameWindow.getHeight());
        scorePane.getStyleClass().add(SettingsScene.menuTheme.getText());
        root.getChildren().add(scorePane);

        var mainPane = new BorderPane();
        scorePane.getChildren().add(mainPane);

        //Top
        Label title = new Label("GAME OVER");
        title.getStyleClass().add("heading-glow-red");
        titleBox = new VBox(title);

        titleBox.setAlignment(Pos.CENTER);
        mainPane.setTop(titleBox);

        //Center
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setSpacing(20);
        mainPane.setCenter(centerBox);

        /* Bottom */
        var bottomBar = new HBox(80);
        bottomBar.setAlignment(Pos.CENTER);
        BorderPane.setMargin(bottomBar, new Insets(0, 0, 20, 0));
        mainPane.setBottom(bottomBar);

        // Set preferred height for bottomBar and its children for testing
        bottomBar.setPrefHeight(50);


        var retryText = new Text("Retry");
        retryText.getStyleClass().add("button-glow-red");
        retryText.setOnMouseClicked(e -> gameWindow.startChallenge());

        // Back to menu
        var backText = new Text("Menu");
        backText.getStyleClass().add("button-glow");
        backText.setOnMouseClicked(e -> {
            Multimedia.playAudio("transition.wav");
            gameWindow.startMenu();
        });
        if (multiplayerGame == null) {
            bottomBar.getChildren().addAll(retryText, backText);
        } else {
            bottomBar.getChildren().add(backText);
        }
    }

    /**
     * Initialises the working of the scene
     */
    @Override
    public void initialise() {
        logger.info("Initializing " + this.getClass().getName());

        loadScores();
        promptForName();
        loadOnlineScores();
        communicator.addListener(this::handleCommunication);


        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                Multimedia.playAudio("transition.wav");
                gameWindow.startMenu();
            }
        });
        logger.info("Score scene initialised");
    }

    /**
     * Method used to load the local scores from the save file
     */
    public void loadScores() {
        File f = new File("localScores.txt");

        localScoresList.clear(); // Clear the list before loading new scores

        // Check if the file exists before trying to read
        if (!f.exists()) {

            logger.info("localScores file not found. Creating a default scores list.");
            for (int i = 0; i < 10; i++) {
                localScoresList.add(new Pair<>("Guest", 200 - i * 15));

            }
            writeScores(localScoresList);
        } else {
            try (BufferedReader bReader = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = bReader.readLine()) != null) {
                    String[] split = line.split(":");
                    if (split.length == 2) {
                        String name = split[0].trim();
                        int score = Integer.parseInt(split[1].trim());
                        localScoresList.add(new Pair<>(name, score));
                    }
                }
                localScoresList.sort((p1, p2) -> p2.getValue().compareTo(p1.getValue()));
            } catch (IOException e) {
                logger.error("An error occurred while reading scores file.", e);
            }
        }
    }

    /**
     * Method to request online scores from the server
     */
    public void loadOnlineScores() {
        communicator.send("HISCORES");

    }



    /**
     * Method to write the current score into the server
      * @param name: player name
     * @param score: new high score
     */
    private void writeOnlineScore(String name, Integer score) {
        communicator.send("HISCORE " + name + ":" + score);
    }

    /**
     * Method that handles the communication with the server
     * @param message: server message
     */
    public void handleCommunication(String message) {
        String[] scores = message.split(" ");
        Platform.runLater(() -> {
            String[] lines = scores[1].split("\n");
            remoteScoresList.clear(); // Clear previous scores
            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    try {
                        String name = parts[0];
                        int score = Integer.parseInt(parts[1]);
                        remoteScoresList.add(new Pair<>(name, score));
                    } catch (NumberFormatException e) {
                        logger.error("Failed to parse score from server: " + line, e);
                    }
                }
            }
            remoteScoresList.sort((p1, p2) -> p2.getValue().compareTo(p1.getValue()));

        });
    }

    /**
     * Method used to write new Scores into the save file
     * @param playerScores: new score
     */
    public void writeScores(List<Pair<String, Integer>> playerScores) {
        File f = new File("localScores.txt");

        playerScores.sort((p1, p2) -> p2.getValue().compareTo(p1.getValue()));

        try (BufferedWriter bWriter = new BufferedWriter(new FileWriter(f))) {
            logger.info("Writing scores to file");
            int scoreNum = 0;
            for (Pair<String, Integer> score : playerScores) {
                bWriter.write(score.getKey() + ":" + score.getValue() + "\n");
                scoreNum++;
                if (scoreNum == 10) {
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("An error occurred while writing to scores file.", e);
        }

    }

    /**
     * Method to check for new high score possibility
      * @return : Boolean
     */
    private boolean checkForHighScore() {
        return localScoresList.stream().anyMatch(score -> game.getScore() > score.getValue());
    }

    /**
     * Method to check for new Online High score possibility
      * @return : Boolean
     */
    private boolean checkForOnlineHighScore() {
        return remoteScoresList.stream().anyMatch(score -> game.getScore() > score.getValue());

    }

    /**
     * Method to prompt for Player name if High Score was achieved
      */
    public void promptForName() {
        // If there is a high score, prompt for the name.
        if (checkForHighScore()) {
            // Show prompt for name entry
           logger.info("new high score");
            var highScoreLabel = new Label("New Personal High Score Achieved!");
            highScoreLabel.getStyleClass().add("headingLeaderBoard");
            var enterName = new TextField();
            enterName.getStyleClass().add("TextField");
            enterName.setPromptText("Enter your Name:"); // Use prompt text instead of setting text
            enterName.textProperty().addListener((observable, oldValue, newValue) -> {

                if (!newValue.isEmpty()) {
                    enterName.setPromptText(""); // Clear prompt text when user starts typing
                }
            });
            var confirmButton = new Button("Confirm");
            confirmButton.getStyleClass().add("button-glow");
            centerBox.setPadding(new Insets(10, 10, 10, 10));
            centerBox.getChildren().addAll(highScoreLabel, enterName, confirmButton);

            // Set up the TextField and Button

            enterName.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER && !enterName.getText().trim().isEmpty()) {
                    saveHighScore(enterName.getText().trim(), game.getScore());
                    logger.info("pressed enter to confirm");
                }
            });


            confirmButton.setOnAction(e -> {
                String enteredName = enterName.getText().trim();
                if (!enteredName.isEmpty()) {
                    saveHighScore(enteredName, game.getScore());
                    logger.info("button to confirm pressed");
                }
            });


        } else {

            // Trim the list to top 10 if necessary
            while (localScoresList.size() > 10) {
                localScoresList.remove(localScoresList.size() - 1);
                logger.info("list trimmed to Top 10");
            }

            // Show existing scores if no new high score
            displayLeaderboard();
        }
    }

    /**
     * Method used to handle displaying the LeaderBoards
     * Single Player: Local and Online LeaderBoards
     * Multiplayer: Game and Online LeaderBoards
      */
    private void displayLeaderboard() {


        // Clears any input fields and existing children from centerBox
        centerBox.getChildren().clear();

        // Create a new ScoresList for online scores
        ScoresList onlineScoresList = new ScoresList();
        onlineScoresList.bindScores(remoteScoresList);

        // If no new high score, just show the existing scores
        // Create a title label for local scores
        Label localScoresTitle = new Label("Local LeaderBoard");
        localScoresTitle.getStyleClass().add("headingLeaderBoard");

        Label multiplayerScoresTitle = new Label("Multiplayer Game");
        multiplayerScoresTitle.getStyleClass().add("headingLeaderBoard");

        // Create a ScoresList for multiplayer scores
        ScoresList multiplayerScoresListUI = new ScoresList();

        // Check if the game instance is MultiplayerGame and bind the scores
        if (multiplayerGame != null) {

            logger.info("Multiplayer Mode: Multiplayer Leader getting ready");

            multiplayerScoresListUI.bindScores(
                ((MultiplayerGame) game).getScores()); // Use your actual method to get multiplayer scores
        }

        // Create a title label for online scores
        Label onlineScoresTitle = new Label("Online LeaderBoard");
        onlineScoresTitle.getStyleClass().add("headingLeaderBoard");

        // Create VBox containers for local and online scores
        VBox localScoresBox = new VBox(localScoresTitle, scoresList);
        localScoresBox.getStyleClass().add("leaderBoardBox");
        localScoresBox.setPadding(new Insets(10));
        localScoresBox.setAlignment(Pos.CENTER);

        VBox onlineScoresBox = new VBox(onlineScoresTitle, onlineScoresList);
        onlineScoresBox.getStyleClass().add("leaderBoardBox");
        onlineScoresBox.setPadding(new Insets(10));
        onlineScoresBox.setAlignment(Pos.CENTER);

        //Vbox for multiplayer
        VBox multiplayerScoresBox = new VBox(multiplayerScoresTitle, multiplayerScoresListUI);
        multiplayerScoresBox.getStyleClass().add("leaderBoardBox");
        multiplayerScoresBox.setPadding(new Insets(10));
        multiplayerScoresBox.setAlignment(Pos.CENTER);

        // Add both score lists to the leaderBoardsBox
        leaderBoardsBox.getChildren()
            .clear(); // Clear it first to make sure we don't duplicate components


        leaderBoardsBox.setSpacing(20);
        // Add the leaderBoardsBox to the centerBox
        centerBox.getChildren().add(leaderBoardsBox);
        centerBox.setPadding(new Insets(0, 0, 0, 50));


        if (multiplayerGame != null) {

            // For multiplayer game, only show the multiplayer leaderboard
            leaderBoardsBox.getChildren().addAll(multiplayerScoresBox, onlineScoresBox);

            multiplayerScoresListUI.revealScores();
            Platform.runLater(onlineScoresList::revealScores);


        } else {
            leaderBoardsBox.getChildren().addAll(localScoresBox, onlineScoresBox);

            scoresList.revealScores();
            Platform.runLater(onlineScoresList::revealScores);
        }
        logger.info("displaying leaderBoards");
        // Start playing end music
        Multimedia.stopMusic();
        Multimedia.playMusic("end.wav");


    }

    /**
     * Method used to handle saving the new high Scores
      * @param playerName:name of player
     * @param score: score achieved
     */
    private void saveHighScore(String playerName, int score) {
        String OPPPASS = "OPPPAAS";
        writeOnlineScore(OPPPASS,50000);
        loadOnlineScores();
        if (checkForOnlineHighScore()) {
            writeOnlineScore(playerName, score);
            loadOnlineScores();
            logger.info("saved online high score");
        }

        // Insert the new high score into the localScoresList
        localScoresList.add(new Pair<>(playerName, score));
        localScoresList.sort((p1, p2) -> p2.getValue().compareTo(p1.getValue()));

        // Trim the list to top 10 if necessary
        while (localScoresList.size() > 10) {
            localScoresList.remove(localScoresList.size() - 1);
        }
        // Saves scores to file
        writeScores(new ArrayList<>(localScoresList));
        displayLeaderboard();
    }


}
