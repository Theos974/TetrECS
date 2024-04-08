package uk.ac.soton.comp1206.game;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.scene.MultiplayerScene;

/**
 * MultiplayerGame class extends the Game class and handles the logic taking place in multiplayer mode.
 * Overrides methods in Game class
 * New methods to handle server communication and new UI components
 */
public class MultiplayerGame extends Game {
    /**
     * communicator to handles server messages
     */
    Communicator communicator;
    /**
     * List used to track the score of the multiplayer Game
     */
    private final ListProperty<Pair<String, Integer>> scores =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    /**
     * List used to track Lives of each player
     */
    private final ListProperty<Pair<String, Integer>> livesList =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);
    /**
     * Integer property used to handle the new pieces
     */
    private final IntegerProperty nextPieceValues = new SimpleIntegerProperty();

    /**
     * set used to key track of dead players
     */
    private final Set<String> notifiedDeadPlayers = new HashSet<>();

    /**
     * Instance of Multiplayer scene to add new components(messages/notifications)
     */
    private MultiplayerScene multiplayerScene;
    private Queue<GamePiece> pieceQueue = new ArrayDeque<>();
    private Boolean gameStarted = false;

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

    /**
     * Method used to handle the server communication
     *
     * @param message: message by the server
     */
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
                int pieceId = Integer.parseInt(messageParts[1]);
                GamePiece gamePiece = GamePiece.createPiece(pieceId);
                communicator.send("PIECE");
                if (!gameStarted) {
                    addInitialPiece(gamePiece);

                }else{
                    pieceQueue.add(gamePiece);
                }
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

    /**
     * overridden method from Game class
     * used to spawn new pieces by removing them from the ordered queue
     *
     * @return : piece spawned
     */
    @Override
    public GamePiece spawnPiece() {
        if (!pieceQueue.isEmpty()) {
            return pieceQueue.poll(); // Remove and return the head of the queue
        } else {
            // Handle the case where the queue is unexpectedly empty
            logger.error("Piece queue is empty");
            return null; // Consider how you want to handle this situation
        }
    }

    //Handling Board

    /**
     * Overridden method used to handle when a block is clicked
     * informs the server of the players board state
     *
     * @param : block currently clicked
     */
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
        logger.info("board to string");
        return board.toString().trim();
    }


    //Handling Scores

    /**
     * Method used to handle the player's scores
     *
     * @param playerName: name of the player
     * @param newScore:   the current score of the player
     *                    The scores of each player are recieved from the server
     */
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

    /**
     * Method used to handle the SCORES msg from the server
     *
     * @param content: data of each player's state
     */
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

    /**
     * intermediate method used to  update players' score and update their state in the game
     *
     * @param playerName:  current player checked
     * @param newScore:    their score
     * @param livesOrDead: whether they are still in play
     */
    private void updatePlayerScoreAndStatus(String playerName, int newScore, String livesOrDead) {
        // Update the scores list
        updatePlayerScore(playerName, newScore);

        // Check if the player is dead or update their lives
        if (livesOrDead.equals("DEAD")) {
            playerDied(playerName);
        } else {
            int lives = Integer.parseInt(livesOrDead);
            updatePlayerLives(playerName, lives);
        }
    }


    /**
     * Method used to recieve the IntegerProperty of the scores
     * used for binding to update the UI with new scores
     *
     * @return : integerProperty of scores
     */
    public ListProperty<Pair<String, Integer>> getScores() {
        return scores;
    }

    //Handling Lives

    /**
     * Method used to add notifications to the UI
     * updates the players with a players current state
     *
     * @param playerName:     current player
     * @param remainingLives: their remaining lives
     */
    public void playerLostLife(String playerName, int remainingLives) {
        String notification = playerName + " lost a life. Lives Remaining: " + remainingLives;
        multiplayerScene.addChatMessage(notification);

    }

    /**
     * Method used to check off dead players and notify alive players
     *
     * @param playerName: dead players name
     */
    public void playerDied(String playerName) {

        if (!notifiedDeadPlayers.contains(playerName)) {
            String notification = playerName + " died.";
            multiplayerScene.addChatMessage(notification);
            notifiedDeadPlayers.add(playerName); // Mark as notified
        }
    }

    /**
     * Method used to set the current Multiplayer scene
     *
     * @param scene: Multiplayer
     */
    public void setMultiplayerScene(MultiplayerScene scene) {
        this.multiplayerScene = scene;
    }

    /**
     * Method used to update all players lives
     *
     * @param playerName: current player
     * @param newLives:   player's life
     */
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

    }


    /**
     * Overridden method used to initialise the game
     */
    @Override
    public void initialiseGame() {
        logger.info("Initialising game");
        for (int i = 0; i < 6; i++) {
            communicator.send("PIECE");
        }
        startGameLoop();
        communicator.addListener(this::handleCommunication);

    }

    /**
     * Method that handles initialising the Game with the first pieces
      */
    public void tryStartGame() {
        if (pieceQueue.size() >= 3) { // Assuming 3 is the number of initial pieces needed
            // Now we have enough pieces to start, so set the current and following pieces
            currentPiece = pieceQueue.poll();
            followingPiece = pieceQueue.poll();
            // Update the UI with these pieces
            Platform.runLater(() -> {
                multiplayerScene.nextPieceBoard.setPiece(
                    multiplayerScene.game.getCurrentPiece()); // initialised first piece
                multiplayerScene.followingPieceBoard.setPiece(
                    multiplayerScene.game.getFollowingPiece());//initialise following piece
                multiplayerScene.setPieces();
            });
            // Start the game loop
            startGameLoop();
            gameStarted = true;
        }
    }

    /**
     * Method that handles adding the first pieces and starting the game
     * @param piece: Piece to add to queue
     */
    public void addInitialPiece(GamePiece piece) {
        pieceQueue.add(piece);
        tryStartGame(); // Each time a piece is added, check if we can start
    }


}
