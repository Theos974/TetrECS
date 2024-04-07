package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class SettingsScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger(SettingsScene.class);


    /**
     * The background image for the menu
     */
    public static Text menuTheme = new Text("menu-background6");

    /**
     * The background image for the game
     */
    public static Text gameTheme = new Text("challenge-background");

    /**
     * Sliders to control volumes of music and audio
     */
    private Slider musicSlider, audioSlider;

    /**
     * button to select between game theme or menu theme
     */
    private Button menuButton, gameButton;

    /**
     * flag for menu theme
     */
    private Boolean selectMenu = false;
    /**
     * flag for game Theme
     */
    private Boolean selectGame = true;

    /**
     * Default volume for music and audio
     */
    public static double musicVolume = 50, audioVolume = 50;
    Text themeText;

    public SettingsScene(GameWindow gameWindow) {
        super(gameWindow);
    }


    /**
     * method writes/saves confiqs to setting.txt
     */
    public static void saveSettings() {

        File f = new File("settings.txt");


        try (BufferedWriter bWriter = new BufferedWriter(new FileWriter(f))) {
            logger.info("Writing settings to file");
            bWriter.write(musicVolume + "\n");
            bWriter.write(audioVolume + "\n");
            bWriter.write(menuTheme.getText() + "\n");
            bWriter.write(gameTheme.getText() + "\n");
            logger.info("logged new settings");
        } catch (IOException e) {
            logger.error("An error occurred while writing to scores file.", e);
        }

    }

    /**
     * method loads settings from settings.txt file
     */
    public static void loadSettings() {

        File f = new File("settings.txt");

        // Check if the file exists before trying to read
        if (!f.exists()) {

            logger.info("settings file not found. Creating a default settings.");
            saveSettings();

        } else {
            try (BufferedReader bReader = new BufferedReader(new FileReader(f))) {
                String line;
                int count = 0;
                while ((line = bReader.readLine()) != null) {
                    if (count == 0) {
                        musicVolume = Double.parseDouble(line);
                        count++;
                    } else if (count == 1) {
                        audioVolume = Double.parseDouble(line);
                        count++;
                    } else if (count == 2) {
                        menuTheme.setText(line);
                        count++;
                    } else {
                        gameTheme.setText(line);
                    }

                }
            } catch (IOException e) {
                logger.error("An error occurred while reading scores file.", e);
            }
        }


    }

    /**
     * Save settings and return to menu
     */
    public void quit() {
        saveSettings();
        Multimedia.playAudio("transition.wav");
        gameWindow.startMenu();
    }
    public void initialiseButtons(){

        Platform.runLater(() -> {
            gameButton.setOnAction(event -> {
                selectMenu = false;
                selectGame = true;
                themeText.setText("Select Game Theme");
            });
            menuButton.setOnAction(event -> {
                selectMenu = true;
                selectGame = false;
                themeText.setText("Select Menu Theme");
            });
        });

    }

    @Override
    public void initialise() {


        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                quit();
            }
        });
        initialiseButtons();
    }

    @Override
    /**
     * Method to build UI components and controls
     */
    public void build() {

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        //BASE
        var settingsPane = new StackPane();
        settingsPane.setMaxWidth(gameWindow.getWidth());
        settingsPane.setMaxHeight(gameWindow.getHeight());
        settingsPane.getStyleClass().add(SettingsScene.menuTheme.getText());
        root.getChildren().add(settingsPane);

        var mainPane = new BorderPane();
        settingsPane.getChildren().add(mainPane);

        //TOP
        var topSection = new HBox();
        topSection.setAlignment(Pos.CENTER);
        mainPane.setTop(topSection);

        //Title
        var title = new Text("SETTINGS");
        title.getStyleClass().add("title");
        topSection.getChildren().add(title);

        //Center
        var centerBox = new VBox();
        centerBox.setAlignment(Pos.CENTER);
        mainPane.setCenter(centerBox);


        //Selection Buttons
        var buttonBox = new HBox();
        menuButton = new Button("Menu Select");
        gameButton = new Button("Game Select");
        buttonBox.getChildren().addAll(menuButton, gameButton);
        buttonBox.setAlignment(Pos.CENTER);
        menuButton.getStyleClass().add("glow-red");
        gameButton.getStyleClass().add("glow-red");
        buttonBox.setSpacing(10);

        //Music and Audio Sliders

        var soundsBox = new HBox();
        soundsBox.setAlignment(Pos.CENTER);
        soundsBox.setSpacing(10);

        var musicBox = new VBox();
        var musicText = new Text("Music");
        musicText.getStyleClass().add("heading");
        musicSlider = new Slider(0, 100, musicVolume);
        musicSlider.setPrefSize(300, 20);
        musicSlider.setShowTickMarks(true);
        musicSlider.setMajorTickUnit(25);

        musicSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            musicVolume = (int) musicSlider.getValue();
            Multimedia.musicPlayer.setVolume(musicVolume / 100);
        });
        musicBox.getChildren().addAll(musicText, musicSlider);

        var audioBox = new VBox();
        var audioText = new Text("Audio");
        audioText.getStyleClass().add("heading");
        audioSlider = new Slider(0, 100, audioVolume);
        audioSlider.setPrefSize(300, 20);
        audioSlider.setShowTickMarks(true);
        audioSlider.setMajorTickUnit(25);

        audioSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            audioVolume = (int) audioSlider.getValue();
            Multimedia.audioPlayer.setVolume(audioVolume / 100);
        });
        audioBox.getChildren().addAll(audioText, audioSlider);

        soundsBox.getChildren().addAll(musicBox, audioBox);

        //Themes
        themeText = new Text("Select Game Theme");
        themeText.getStyleClass().add("heading");
        themeText.setTextAlignment(TextAlignment.CENTER);

        var grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setAlignment(Pos.CENTER);


        //All themes
        ImageView one = new ImageView(Multimedia.getImage("1.jpg"));
        grid.add(one, 0, 1);
        one.setFitWidth(240);
        one.setPreserveRatio(true);
        one.setOnMouseClicked(e -> {
            Multimedia.playAudio("pling.wav");
            if (selectMenu) {
                menuTheme = new Text("menu-background");
                logger.info("Set menu theme to 1");
            }else {
                gameTheme = new Text("menu-background");
                logger.info("Set game theme to 1");
            }

        });
        ImageView two = new ImageView(Multimedia.getImage("2.jpg"));
        grid.add(two, 1, 1);
        two.setFitWidth(240);
        two.setPreserveRatio(true);
        two.setOnMouseClicked(e -> {
            Multimedia.playAudio("pling.wav");
            if (selectMenu) {
                menuTheme = new Text("menu-background2");
                logger.info("Set menu theme to 2");
            }else {
                gameTheme = new Text("menu-background2");
                logger.info("Set game to 2");
            }

        });
        ImageView three = new ImageView(Multimedia.getImage("3.jpg"));
        grid.add(three, 2, 1);
        three.setFitWidth(240);
        three.setPreserveRatio(true);
        three.setOnMouseClicked(e -> {
            Multimedia.playAudio("pling.wav");
            if (selectMenu) {
                menuTheme = new Text("menu-background3");
                logger.info("Set menu theme to 3");
            }else {
                gameTheme = new Text("menu-background3");
                logger.info("Set game theme to 3");
            }

        });
        ImageView four = new ImageView(Multimedia.getImage("4.jpg"));
        grid.add(four, 0, 2);
        four.setFitWidth(240);
        four.setPreserveRatio(true);
        four.setOnMouseClicked(e -> {
            Multimedia.playAudio("pling.wav");
            if (selectMenu) {
                menuTheme = new Text("menu-background4");
                logger.info("Set menu theme to 4");
            }else {
                gameTheme = new Text("menu-background4");
                logger.info("Set game theme to 4");
            }

        });
        ImageView five = new ImageView(Multimedia.getImage("5.jpg"));
        grid.add(five, 1, 2);
        five.setFitWidth(240);
        five.setPreserveRatio(true);
        five.setOnMouseClicked(e -> {
            Multimedia.playAudio("pling.wav");
            if (selectMenu) {
                menuTheme = new Text("menu-background5");
                logger.info("Set menu theme to 5");
            }else {
                gameTheme = new Text("menu-background5");
                logger.info("Set game theme to 5");
            }

        });
        ImageView six = new ImageView(Multimedia.getImage("6.jpg"));
        grid.add(six, 2, 2);
        six.setFitWidth(240);
        six.setPreserveRatio(true);
        six.setOnMouseClicked(e -> {
            Multimedia.playAudio("pling.wav");
            if (selectMenu) {
                menuTheme = new Text("menu-background6");
                logger.info("Set menu theme to 6");

            }else {
                gameTheme = new Text("menu-background6");
                logger.info("Set game theme to 6");

            }
        });

        centerBox.getChildren().addAll(soundsBox,buttonBox,themeText,grid);

        //Bottom
        var bottomBar = new HBox();
        bottomBar.setAlignment(Pos.CENTER);
        BorderPane.setMargin(bottomBar, new Insets(0, 0, 20, 0));
        mainPane.setBottom(bottomBar);

        // Save button
        var saveText = new Text("Save");
        saveText.getStyleClass().add("button-glow");
        saveText.setOnMouseClicked(e -> quit());
        bottomBar.getChildren().add(saveText);
    }


}
