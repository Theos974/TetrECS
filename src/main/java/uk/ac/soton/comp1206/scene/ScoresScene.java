package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScoresScene extends BaseScene {
    private ScoresList scoresList;
    static final Logger logger = LogManager.getLogger(ScoresScene.class);
    private Game game;
    VBox centerBox = new VBox();
    VBox titleBox;
    private final Label leaderBoard = new Label("LeaderBoard");

    private ListProperty<Pair<String, Integer>> localScoresList;


    public ScoresScene(GameWindow gameWindow, Game game) {
        super(gameWindow);
        this.game = game;
        this.localScoresList = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.scoresList = new ScoresList();
        scoresList.bindScores(localScoresList);

        loadScores();

    }

    @Override
    public void build() {

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var scorePane = new StackPane();
        scorePane.setMaxWidth(gameWindow.getWidth());
        scorePane.setMaxHeight(gameWindow.getHeight());
        scorePane.getStyleClass().add("menu-background3");
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
        centerBox.setSpacing(10);
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
        retryText.setOnMouseClicked(e -> {
            gameWindow.startChallenge();
        });

        // Back to menu
        var backText = new Text("Menu");
        backText.getStyleClass().add("button-glow");
        backText.setOnMouseClicked(e -> {
            Multimedia.playAudio("transition.wav");
            gameWindow.startMenu();
        });

        bottomBar.getChildren().addAll(retryText, backText);

    }

    @Override
    public void initialise() {
        logger.info("Initializing " + this.getClass().getName());

        loadScores();
        promptForName();

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                Multimedia.playAudio("transition.wav");
                gameWindow.startMenu();
            }
        });
    }

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

    private boolean checkForHighScore() {
        return localScoresList.stream().anyMatch(score -> game.getScore() > score.getValue());
    }

    public void promptForName() {
        // If there is a high score, prompt for the name.
        if (checkForHighScore()) {
            // Show prompt for name entry
            var highScoreLabel = new Label("New High Score Achieved!");
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
                }
            });

            confirmButton.setOnAction(e -> {
                String enteredName = enterName.getText().trim();
                if (!enteredName.isEmpty()) {
                    saveHighScore(enteredName, game.getScore());
                }
            });


        } else {

            // Trim the list to top 10 if necessary
            while (localScoresList.size() > 10) {
                localScoresList.remove(localScoresList.size() - 1);
            }

            // Show existing scores if no new high score
            displayLeaderboard();
        }
    }

    private void displayLeaderboard() {


        // Clears any input fields and existing children from centerBox
        centerBox.getChildren().clear();

        // If no new high score, just show the existing scores
        Multimedia.stopMusic();
        Multimedia.playMusic("end.wav");
        titleBox.getChildren().add(leaderBoard);
        leaderBoard.getStyleClass().add("headingLeaderBoard");
        centerBox.getChildren().add(scoresList);
        scoresList.revealScores();
    }

    private void saveHighScore(String playerName, int score) {
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
