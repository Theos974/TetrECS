package uk.ac.soton.comp1206.scene;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LobbyScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(LobbyScene.class);
    private Communicator communicator;
    private ObservableList<String> userList = FXCollections.observableArrayList();
    private ListProperty<String> channelList;
    private Timeline channelRequestTimer;
    private ListView<String> channelListView = new ListView<>();
    private Button createGameButton;
    private VBox bottomBox;
    private VBox leftBox;
    private TextField enterName;
    private Button confirmButton;
    private Button cancel;
    private HBox confirmationBox = new HBox();
    private TextField messageToSend;
    private ScrollPane scroller;
    private TextFlow messages;
    private VBox centreBox;
    private Button startGameButton;
    private BorderPane mainPane;
    private Button leaveButton;
    private HBox userDisplayBox;
    private boolean host = false;

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        this.communicator = gameWindow.getCommunicator();
        this.channelList = new SimpleListProperty<>(FXCollections.observableArrayList());
        channelListView.itemsProperty().bind(channelList);
        startChannelRequestTimer();
        communicator.addListener(this::handleCommunication);
    }


    // Method to start the repeating timer
    public void startChannelRequestTimer() {
        // Defines the action to be taken each time the timer ticks
        EventHandler<ActionEvent> onTimerTick = e -> requestChannelList();

        // Creates a new Timeline that loops indefinitely
        channelRequestTimer = new Timeline(
            new KeyFrame(
                Duration.seconds(2), // Adjust the duration to how often you want to poll the server
                onTimerTick
            )
        );
        channelRequestTimer.setCycleCount(Timeline.INDEFINITE); // Indefinite loop
        channelRequestTimer.play(); // Start the timeline
    }

    // Method that sends the LIST command to the server
    private void requestChannelList() {
        if (communicator != null) {
            communicator.send("LIST");

        } else {
            // Handle the case where the communicator is not initialized
            logger.error("Communicator is not initialized.");
        }
    }

    // Method to stop the timer (optional, based on your application's flow)
    public void stopChannelRequestTimer() {
        if (channelRequestTimer != null) {
            channelRequestTimer.stop();
        }
    }

    @Override
    public void initialise() {
        communicator.addListener(this::handleCommunication);

        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                handleEscapePressed();
                event.consume(); // Optional, prevents further handling
            }
        });


        // Prompt for channel name when the create game button is clicked
        createGameButton.setOnAction(event -> {
            // Show the TextField and Confirm button when Create Game is clicked
            confirmationBox.setVisible(true); // Make sure this box is set to visible
            enterName.setVisible(true);
            confirmButton.setVisible(true);
            cancel.setVisible(true);
            bottomBox.getChildren()
                .addAll(enterName, confirmationBox); // Add TextField and button to the layout
            enterName.requestFocus(); // Focus on the TextField
        });


        confirmButton.setOnAction(event -> {
            createChannel(enterName);
        });


        cancel.setOnAction(event -> {
            enterName.clear(); // Clear the text field
            bottomBox.getChildren().removeAll(enterName, confirmationBox);
            confirmationBox.setVisible(false);
        });

        leaveButton.setOnAction(event -> {
            communicator.send("PART"); // Send the command to leave the channel
            messageToSend.clear();
            Platform.runLater(() -> {
                startGameButton.setVisible(false);
                messages.getChildren().clear(); // Clear the chat messages
                centreBox.setVisible(false); // Optionally, hide the chat interface
                host = false; // Reset host status

            });
        });


        startGameButton.setOnAction(event -> {

            if (host == true) {
                communicator.send("START");
            } else {
                logger.info("you are not host");
            }
        });

        channelListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null); // Clear any custom graphics like VBox
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item);
                    setStyle("-fx-background-color: transparent;");
                    // Add padding to the cell for spacing
                    setPadding(new Insets(10, 10, 10, 10)); // Adjust the values to your liking
                    getStyleClass().add("channelItem");
                    setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2) { // Double-click to join channel
                            userRequestsJoin(item);

                        }
                    });
                }
            }
        });


    }

    private void handleEscapePressed() {
        Multimedia.playAudio("transition.wav");
        stopChannelRequestTimer();
       // communicator.send("QUIT");
        gameWindow.startMenu();
        logger.info("Pressed escape to go back");
    }
    public void handleCommunication(String message) {
        // Split the message to determine the command type
        String[] messageParts = message.split(" ", 2);
        String command = messageParts[0]; // The command is always the first part
        String content = messageParts.length > 1 ? messageParts[1] : ""; // Content might be empty

        switch (command) {
            case "CHANNELS":
                String[] channels = content.split("\n");
                updateChannelList(channels);
                break;
            case "JOIN":
                handleJoin(content);
                break;
            case "ERROR":
                handleError(content);
                break;
            case "USERS":
                handleUsers(content);
                break;
            case "HOST":
                handleHost(); // Now it will correctly handle "HOST" without additional content
                break;
            case "QUIT":
                handleQuit();
                break;
            case "MSG":
                handleChatMessage(content);
                break;
            default:
                logger.error("Received unknown command: " + command);
                break;
        }
    }


    private void updateChannelList(String[] newChannels) {
        Platform.runLater(() -> {
            ObservableList<String> currentItems = channelListView.getItems();
            HashSet<String> newChannelsSet = new HashSet<>(Arrays.asList(newChannels));
            HashSet<String> currentItemsSet = new HashSet<>(currentItems);

            // Remove channels that no longer exist
            currentItems.removeIf(channel -> !newChannelsSet.contains(channel));

            // Add new channels
            for (String channel : newChannelsSet) {
                if (!currentItemsSet.contains(channel)) {
                    currentItems.add(channel);
                }
            }
        });
    }


    private void handleJoin(String content) {

        Platform.runLater(() -> {
            if (!channelList.contains(content)) {
                channelList.add(content);
                channelListView.getSelectionModel().select(content);
                openChatForChannel(content); // Open chat view for this channel
            }
            logger.info("Joined channel: " + content);
        });
    }

    private void userRequestsJoin(String channel) {
        communicator.send("JOIN " + channel);
        logger.info("Requested to join channel: " + channel);
        openChatForChannel(channel);
    }

    private void openChatForChannel(String channel) {

        // Show the chat UI components
        centreBox.setVisible(true);


        messageToSend.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String text = messageToSend.getText();
                if (text.startsWith("/nick ")) {
                    String newNick = text.substring(6); // Assuming command is "/nick newNickname"
                    communicator.send("NICK " + newNick);
                    messageToSend.clear();
                } else {
                    sendChatMessage(text, channel);
                }
            }
        });


    }

    private void sendChatMessage(String message, String channel) {
        communicator.send("MSG " + message);

        // Clear the input field ready for a new message
        messageToSend.clear();
    }


    private void handleChatMessage(String content) {
        Platform.runLater(() -> {
            // Assuming content format is "username: messageContent"
            String[] parts = content.split(":", 2);
            if (parts.length == 2) {
                String username = parts[0].trim();
                String message = parts[1].trim();

                // Format the current time
                LocalTime now = LocalTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                String formattedTime = now.format(formatter);


                Text text = new Text("[" + formattedTime + "] " + username + ": " + message + "\n");
                text.getStyleClass().add("channelItem");

                // Add the text node to the messages container (e.g., a VBox or TextFlow)
                messages.getChildren().add(text);


                scroller.setVvalue(1.0);
            }
        });
    }



    private void handleError(String content) {
        Platform.runLater(() -> {
            // Display an error dialog with the content received
            showAlert("Error", content);
        });
    }


    // Call this method when you receive a QUIT command from the server
    private void handleQuit() {
        Platform.runLater(() -> {
            // Close the connection and clean up resources


            // Navigate back to the main menu or exit
            gameWindow.startMenu();
            communicator.send("QUIT");
            // Inform the user
            showAlert("Disconnected", "You have been disconnected from the server.");
        });
    }

    private void handleHost() {
        Platform.runLater(() -> {
            host = true; // Set the host flag to true
            // Update UI to reflect host status
            startGameButton.setVisible(host);
            logger.info("handled host");
        });
    }



    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void handleUsers(String content) {
        Platform.runLater(() -> {
            userDisplayBox.getChildren().clear(); // Clear previous user names

            String[] users = content.split("\\n");
            for (String user : users) {
                Label userLabel = new Label(user);
                userLabel.getStyleClass().add("channelItem");
                userDisplayBox.getChildren().add(userLabel);
            }
        });
    }


    @Override
    public void build() {
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background3");
        root.getChildren().add(menuPane);

        mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);


        //Top
        Text title = new Text("Multiplayer");
        mainPane.setTop(title);
        title.getStyleClass().add("button-glow");


        //Left
        leftBox = new VBox(10);
        mainPane.setLeft(leftBox);
        createGameButton = new Button("Create Game");
        createGameButton.getStyleClass().add("button-glow");


        leftBox.getChildren().addAll(channelListView, createGameButton);

        //Bottom
        bottomBox = new VBox(10); // Spacing between children
        bottomBox.setAlignment(Pos.CENTER);

        // Button and TextField for creating new game

        enterName = new TextField();
        enterName.getStyleClass().add("TextField");
        enterName.setPromptText("Enter Channel Name:");
        enterName.setVisible(false); // Initially hidden


        // Create a confirm button to trigger the channel creation
        confirmButton = new Button("Confirm");
        confirmButton.getStyleClass().add("button-glow");

        //Create a cancel button
        cancel = new Button("Cancel");
        cancel.getStyleClass().add("button-glow-red");

        // Initially hidden
        confirmButton.setVisible(false);
        cancel.setVisible(false);
        confirmationBox.getChildren().addAll(cancel, confirmButton);
        confirmationBox.setAlignment(Pos.CENTER);

        mainPane.setBottom(bottomBox);

        //Center
        centreBox = new VBox();
        //Create a horizontal bar with a text box and send button
        messageToSend = new TextField();

        messageToSend.setPromptText("Enter message");
        startGameButton = new Button("Start Game");
        leaveButton = new Button("Leave Channel");

        startGameButton.setVisible(false);
        leaveButton.getStyleClass().add("glow-red");
        startGameButton.getStyleClass().add("button-glow");
        HBox sendMessageBar = new HBox();
        sendMessageBar.getChildren().add(messageToSend);
        HBox.setHgrow(messageToSend, Priority.ALWAYS);
        VBox chatButtons = new VBox();
        chatButtons.getChildren().addAll(startGameButton, leaveButton);
        chatButtons.setAlignment(Pos.CENTER_RIGHT);

        //Create a textflow to hold all messages
        messages = new TextFlow();
        messages.setPrefSize(300, 300);
        messages.getStyleClass().add("messages-flow");

        //Users and Channel

        var serverBox = new VBox();
        Text channelText = new Text("Players:");
        Text commands = new Text("type /nick username to change your userName");
        commands.getStyleClass().add("channelItem");
        channelText.getStyleClass().add("channelItem");
        userDisplayBox = new HBox(5); // Use a spacing argument to add space between labels
        userDisplayBox.setAlignment(Pos.CENTER_LEFT);
        serverBox.getChildren().addAll(channelText, userDisplayBox, commands);

        //Add a scrollpane
        scroller = new ScrollPane();
        scroller.setFitToWidth(true);
        scroller.setPrefViewportHeight(300);
        scroller.getStyleClass().add("scroller");
        scroller.setContent(messages);
        scroller.setFitToWidth(true);
        centreBox.getChildren().addAll(serverBox, scroller, sendMessageBar, chatButtons);
        centreBox.setVisible(false);
        mainPane.setCenter(centreBox);
        centreBox.getStyleClass().add("scroller");
        centreBox.setSpacing(10);

    }


    private void createChannel(TextField enterName) {
        String channelName = enterName.getText().trim();
        if (!channelName.isEmpty()) {
            communicator.send("CREATE " + channelName);
            // Clear the text field and hide the confirmation box after sending the command
            enterName.clear();
            confirmationBox.setVisible(false);
            bottomBox.getChildren().removeAll(enterName, confirmationBox);

        }
    }

}
