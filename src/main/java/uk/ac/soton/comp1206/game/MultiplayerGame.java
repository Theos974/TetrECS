package uk.ac.soton.comp1206.game;

import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.scene.MultiplayerScene;

public class MultiplayerGame extends Game {
    Communicator communicator;
    private ListProperty<Pair<String, Integer>> scores =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private ListProperty<Pair<String, Integer>> livesList =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);
    private IntegerProperty nextPieceValues = new SimpleIntegerProperty();


    private MultiplayerScene multiplayerScene;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public MultiplayerGame(int cols, int rows, Communicator communicator) {
        super(cols, rows);
        this.communicator = communicator;
    }

    public void handleCommunication(String message) {
        // Split the message to determine the command type
        String[] messageParts = message.split(" ", 2);
        String command = messageParts[0]; // The command is always the first part
        String content = messageParts.length > 1 ? messageParts[1] : ""; // Content might be empty

        switch (command) {
            case "MSG":
                String userName = content.substring(0, content.indexOf(':'));
                String chatMessage = content.substring(content.indexOf(':') + 1);
                // Since this is a chat message, we can directly call the method from multiplayerScene
                multiplayerScene.addChatMessage(userName + ": " + chatMessage);
                break;
            case "PIECE":
                nextPieceValues.set(Integer.parseInt(content));
                handlePieces();
                break;
            case "SCORE":
                String playerName = messageParts[1];
                int score = Integer.parseInt(messageParts[2]);
                updatePlayerScore(playerName, score);
                break;
            case "SCORES":
                handleScoresMessage(content);
                break;
            case "BOARD":
                //String boardString =
                message.substring(6); // Assuming the board string starts at index 6
                break;
        }
    }


    //handling PIECES
    @Override
    public GamePiece spawnPiece() {
        logger.info("multiplayer player");

        communicator.send("PIECE");
        return handlePieces();
    }


    public GamePiece handlePieces() {


        AtomicInteger pieceNum = new AtomicInteger();

        pieceNum.set(nextPieceValues.getValue());


        return GamePiece.createPiece(pieceNum.get());

    }

    /**
     * manages each game loop
     */


    //Handling Board
    @Override
    public boolean blockClicked(GameBlock gameBlock) {
        boolean success = super.blockClicked(gameBlock);
        if (success) {
            communicator.send("BOARD " + boardToString());
        }
        return success;
    }

    /**
     * Changes the board state into a string which can be sent to the server in the correct format
     */
    private String boardToString() {
        StringBuilder board = new StringBuilder();

        for (int x = 0; x < cols; ++x) {
            for (int y = 0; y < rows; ++y) {
                int tmp = grid.get(x, y);
                board.append(tmp).append(" ");
            }
        }
        return board.toString().trim();
    }


    //Handling Scores

    private void updatePlayerScore(String playerName, int newScore) {

        // Check if the player already has a score in the list
        Platform.runLater(() -> {
            boolean found = false;
            for (int i = 0; i < scores.size(); i++) {
                Pair<String, Integer> scoreEntry = scores.get(i);
                if (scoreEntry.getKey().equals(playerName)) {
                    // If they do, update their score
                    scores.set(i, new Pair<>(playerName, newScore));
                    found = true;
                    break;
                }
            }
            if (!found) {
                // If the player isn't in the list (new player), add them
                scores.add(new Pair<>(playerName, newScore));
            }
            FXCollections.sort(scores,
                (pair1, pair2) -> pair2.getValue().compareTo(pair1.getValue()));

        });
    }

    public void handleScoresMessage(String content) {

        String[] playerScores =
            content.split("\n"); // Split each player's score into an array element

        for (String playerScore : playerScores) {
            String[] scoreInfo = playerScore.split(":");
            if (scoreInfo.length >= 3) {
                String playerName = scoreInfo[0];
                int score = Integer.parseInt(scoreInfo[1]);
                String livesOrDead = scoreInfo[2];
                updatePlayerScoreAndStatus(playerName, score, livesOrDead);
            }
        }
    }

    private void updatePlayerScoreAndStatus(String playerName, int newScore, String livesOrDead) {
        // Update the scores list
        updatePlayerScore(playerName, newScore);

        // Check if the player is dead or update their lives
        if (livesOrDead.equals("DEAD")) {
            updatePlayerLives(playerName, -1);
        } else {
            int lives = Integer.parseInt(livesOrDead);
            updatePlayerLives(playerName, lives);
        }
    }


    public ListProperty<Pair<String, Integer>> getScores() {
        return scores;
    }

    //Handling Lives
    public void playerLostLife(String playerName, int remainingLives) {
        String notification = playerName + " lost a life. Lives Remaining: " + remainingLives;
        multiplayerScene.addChatMessage(notification);

    }

    public void playerDied(String playerName) {
        String notification = playerName + " died.";
        multiplayerScene.addChatMessage(notification);
    }

    public void setMultiplayerScene(MultiplayerScene scene) {
        this.multiplayerScene = scene;
    }

    private void updatePlayerLives(String playerName, int newLives) {
        // Find or create the player's lives entry
        boolean found = false;
        Pair<String, Integer> livesEntry = null;
        for (int i = 0; i < livesList.size(); i++) {
            livesEntry = livesList.get(i);
            if (livesEntry.getKey().equals(playerName)) {
                livesList.set(i, new Pair<>(playerName, newLives));
                found = true;
                break;
            }
        }
        if (!found) {
            livesList.add(new Pair<>(playerName, newLives));
        } else {
            if (newLives < livesEntry.getValue()) {
                playerLostLife(playerName, newLives);
            }
        }

        // Notify if a player died (lives reached 0)
        if (newLives < 0) {
            playerDied(playerName);
        }
    }


    // Method to retrieve the lives list, which can be used to bind to a UI component
    public ObservableList<Pair<String, Integer>> getLivesList() {
        return livesList;
    }


    @Override
    public void initialiseGame() {
        logger.info("Initialising game");
        for (int i = 0; i < 3; i++) {
            communicator.send("PIECE");
        }

        currentPiece = spawnPiece();
        followingPiece = spawnPiece();
        startGameLoop();
        communicator.addListener(this::handleCommunication);

    }


}
