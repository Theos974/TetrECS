package uk.ac.soton.comp1206.component;

import java.util.List;
import javafx.animation.FadeTransition;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScoresList extends VBox {
    static final Logger logger = LogManager.getLogger(ScoresList.class);


    private SimpleListProperty<Pair<String, Integer>> scoresProperty;

    public ScoresList() {
        scoresProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.setAlignment(Pos.CENTER);
        this.setSpacing(5);

        // Listen for changes in the scores list and update UI accordingly
        scoresProperty.addListener((ListChangeListener.Change<? extends Pair<String, Integer>> c) -> {
            updateScoreListView();
        });
    }

    public SimpleListProperty<Pair<String, Integer>> scoresProperty() {
        return scoresProperty;
    }
    public void bindScores(ListProperty<Pair<String, Integer>> externalScores) {
        this.scoresProperty.bind(externalScores); // Correct binding
    }

    public void updateScoreListView() {

        this.getChildren().clear();
        for (Pair<String, Integer> score : scoresProperty) {
            Label scoreLabel = new Label(score.getKey() + ": " + score.getValue());
            scoreLabel.getStyleClass().add("menuItem");
            this.getChildren().add(scoreLabel);
        }

    }


    public void revealScores() {
        logger.info("Revealing scores");
        int delay = 0;
        for (Node child : this.getChildren()) {
            FadeTransition ft = new FadeTransition(Duration.seconds(0.5), child);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.setDelay(Duration.millis(delay));
            ft.play();
            delay += 100; // Increments delay for each score to create a cascade effect
        }
    }


}
