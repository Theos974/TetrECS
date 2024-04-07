package uk.ac.soton.comp1206.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ScoreList Class extending Vbox
 * used to display Scores with animation
 */
public class ScoresList extends VBox {
    static final Logger logger = LogManager.getLogger(ScoresList.class);


    /**
     * SimpleListProperty used to bind with the Scores
      */
    private final SimpleListProperty<Pair<String, Integer>> scoresProperty;

    public ScoresList() {
        scoresProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.setAlignment(Pos.CENTER);
        this.setSpacing(5);


        /**
         * Listen for changes in the scores list and update UI accordingly
         */
        scoresProperty.addListener((ListChangeListener.Change<? extends Pair<String, Integer>> c) -> updateScoreListView());
    }


    /**
     * Method used to bind the List so update the UI
     * @param externalScores: external list that is binded
     */
    public void bindScores(ListProperty<Pair<String, Integer>> externalScores) {
        this.scoresProperty.bind(externalScores); // Correct binding
    }

    /**
     * Method used to place scores in Label and colour them
     */
    public void updateScoreListView() {
        this.getChildren().clear();

        logger.info("update list view");

        // Defines and populates the list of colors
        List<String> colors = new ArrayList<>();
        colors.add("#FF6347"); // tomato
        colors.add("#1E90FF"); // dodgerblue
        colors.add("#32CD32"); // limegreen
        colors.add("#FFD700"); // gold
        colors.add("#FF69B4"); // hotpink
        colors.add("#6A5ACD"); // slateblue


        // Shuffles the list of colors
        Collections.shuffle(colors);

        // Index to iterate through the shuffled list of colors
        int colorIndex = 0;

        for (Pair<String, Integer> score : scoresProperty) {
            Label scoreLabel = new Label(score.getKey() + ": " + score.getValue());
            scoreLabel.getStyleClass().add("score-list");

            // Apply color from the shuffled list, and increment the color index
            scoreLabel.setStyle("-fx-text-fill: " + colors.get(colorIndex) + "; -fx-font-weight: bold;");

            // Makes sure the index wraps around if it exceeds the number of colors
            colorIndex = (colorIndex + 1) % colors.size();

            this.getChildren().add(scoreLabel);
        }
    }


    /**
     * Method used to animate the List being revealed
      */
    public void revealScores() {

        logger.info("Revealing scores");
        int delay = 0;
        for (Node child : this.getChildren()) {
            FadeTransition ft = new FadeTransition(Duration.seconds(1.5), child);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.setDelay(Duration.millis(delay));
            ft.play();
            delay += 300; // Increments delay for each score to create a cascade effect
        }
    }


}
