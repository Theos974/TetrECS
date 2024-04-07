package uk.ac.soton.comp1206.scene;


import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.LeaderBoard;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;

public class MultiplayerScene extends ChallengeScene {
    public LeaderBoard leaderBoard;
    MultiplayerGame multiplayerGame;
    private TextFlow chatTextFlow;
    protected static Communicator communicator;
    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);
    private Timer timer;
    private boolean isChatActive = false;
    private ScrollPane chatScrollPane;

    /**
     * Create a new Multiplayer Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public MultiplayerScene(GameWindow gameWindow) {
        super(gameWindow);

    }

    @Override
    /**
     * overridden method used to create the gameBoard
      */
    public void buildBoard() {
        board =
            new GameBoard(game.getGrid(), gameWindow.getWidth() / 2.2, gameWindow.getWidth() / 2.2);
    }

    /**
     * method used to build the UI components of the multiplayer scene
     * Uses the challengeScene as a base
     */
    public void build() {
        super.build();

        //TOP Section
        super.title.setText("Multiplayer Mode");

        //Center Section
        chatField = new TextField();
        chatField.setVisible(false);
        chatField.setPromptText("Enter message and press ENTER to chat..");

        chatScrollPane = new ScrollPane();
        chatScrollPane.setContent(chatTextFlow);
        chatScrollPane.setFitToWidth(true); // Ensures the width of the content is used
        chatScrollPane.setPrefHeight(100); // Or another appropriate fixed height
        chatScrollPane.getStyleClass().add("scroller");

        chatTextFlow = new TextFlow();
        chatTextFlow.setTextAlignment(TextAlignment.CENTER);
        chatScrollPane.setContent(chatTextFlow);
        chatScrollPane.setPadding(new Insets(0, 50, 0, 0));
        centerBox.setPadding(new Insets(0, 50, 0, 0));
        centerBox.getChildren().addAll(chatField, chatScrollPane);

        //Left Section
        leftSection.getChildren().clear();
        leftSection.setAlignment(Pos.CENTER);
        leftSection.setPadding(new Insets(0, 20, 10, 20));
        super.mainPane.setLeft(leftSection);
        leaderBoard = new LeaderBoard();
        var versusText = new Text("     VERSUS");
        versusText.getStyleClass().add("heading");


        var chatInfo = new Text("   press T" + "\n to type in the chat");
        chatInfo.setTextAlignment(TextAlignment.CENTER);
        chatInfo.getStyleClass().add("channelItem");
        chatInfo.setFont(Font.font(6));


        var multiplayerBox = new VBox();

        multiplayerBox.getChildren().addAll(versusText, leaderBoard);
        multiplayerBox.setPadding(new Insets(0, 0, 60, 0));
        multiplayerBox.setSpacing(5);
        leftSection.getChildren().addAll(multiplayerBox, chatInfo);
        leftSection.setSpacing(100);

        super.mainPane.setLeft(leftSection);

    }

    /**
     * Method used to refresh the leaderBoard of game
      */
    private void refreshLeaderboard() {
        leaderBoard.bindScores(multiplayerGame.getScores());
        leaderBoard.updateScoreListView();
        leaderBoard.revealScores();
        logger.info("leaderBoard refreshed");
    }


    /**
     * method used to add notifications to the chat UI component
     * @param message: notification added
     */
    public void addChatMessage(String message) {

        Text chatText = new Text(message + "\n");
        chatText.getStyleClass().add("channelItem");

        Platform.runLater(() -> {
            chatTextFlow.getChildren().add(chatText);
            chatScrollPane.applyCss();
            chatScrollPane.layout();
            chatScrollPane.setVvalue(1.0);
        });
        logger.info("notification added");
    }


    /**
     *Method sets up the keyboard bindings
     */
    @Override
    protected void setKeyboard() {
        //manages keyboard bindings
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (!isChatActive) {
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
                        Multimedia.playAudio("transition.wav");
                        stopTimer();
                        game.stopGameLoop();
                        communicator.send("DIE");
                        gameWindow.startMenu();

                        break;
                    case W:
                    case UP:
                        if (y > 0) {
                            y--;
                            board.hover(board.getBlock(x, y));
                            logger.info("up pressed");
                        }
                        break;
                    case S:
                    case DOWN:
                        if (y < game.getRows() - 1) {
                            y++;
                            board.hover(board.getBlock(x, y));
                            logger.info("down pressed");

                        }
                        break;
                    case A:
                    case LEFT:
                        if (x > 0) {
                            x--;
                            board.hover(board.getBlock(x, y));
                            logger.info("left pressed");
                        }
                        break;
                    case D:
                    case RIGHT:
                        if (x < game.getCols() - 1) {
                            x++;
                            board.hover(board.getBlock(x, y));
                            logger.info("right pressed");

                        }
                        break;
                    case ENTER, X:
                        dropPiece();
                        logger.info("key pressed to drop piece");
                        break;
                    case T:
                        toggleChatFieldVisibility();
                        logger.info("pressed T");
                        break;
                }
            }
        });

    }

    /**
     * Method used to handle how the chatFields workings
     */
    public void handleChatField() {
        chatField.setOnKeyPressed(event -> {
            board.getBlock(x, y).removeHover(); //to allow pressing enter without placing piece
            if (event.getCode() == KeyCode.ENTER) {
                logger.info("pressed enter");
                String text = chatField.getText().trim();
                if (!text.isEmpty()) {
                    communicator.send("MSG " + text);
                }
                chatField.clear();
                chatField.setVisible(false);
                isChatActive = false;
            }
        });

    }

    /**
     * Method handles the toggle effect of the chatField
     */
    private void toggleChatFieldVisibility() {
        isChatActive = !chatField.isVisible();
        chatField.setVisible(isChatActive);

        if (isChatActive) {
            Platform.runLater(
                () -> chatField.requestFocus()); // Focus and move caret to the end if needed
        } else {
            chatField.clear();
        }
    }

    @Override
    /**
     * Overridden method to setUP the Game
      */
    public void setupGame() {

        logger.info("inside setUp");
        if (LobbyScene.communicator != null) {
            communicator = LobbyScene.communicator;
            multiplayerGame = new MultiplayerGame(5, 5, communicator);
            game = multiplayerGame;
            multiplayerGame.setMultiplayerScene(this);
            logger.info("comm not null");
        } else {
            logger.error("comm is null");
        }

    }


    /**
     * Method resets the timer to request and send data to server
      */
    private void resetTimer() {
        TimerTask refresh = new TimerTask() {
            @Override
            public void run() {
                communicator.send("SCORE " + game.getScore());
                communicator.send("LIVES " + game.getLives());
                communicator.send("SCORES");
            }
        };
        timer = new Timer();
        timer.schedule(refresh, 0, 3000);
    }

    @Override
    /**
     * Overridden method used to handle when the game is over
      */
    protected void gameOver() {

        stopTimer();
        communicator.send("DIE");
        logger.info("game over");
        Platform.runLater(() -> gameWindow.startScoreScene(multiplayerGame));
    }

    /**
     * Method to stop request timer
      */
    private void stopTimer() {
        if (timer != null) {
            timer.cancel(); // Cancels the timer
            timer.purge(); // Removes all cancelled tasks from this timer's task queue.
        }
    }

    /**
     * method to initialise the workings of the scene
     */
    public void initialise() {
        super.initialise();
        resetTimer();
        refreshLeaderboard();
        handleChatField();

    }


}
