package uk.ac.soton.comp1206.component;

import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

public class LeaderBoard extends ScoresList {
    public LeaderBoard() {
        super();
    }

    public void updateWithNewScores(List<Pair<String, Integer>> newScores) {
        // Clear current scores
        scoresProperty().clear();

        // Add all new scores
        scoresProperty().addAll(newScores);

        // Sort the list
        FXCollections.sort(scoresProperty(), (pair1, pair2) -> pair2.getValue().compareTo(pair1.getValue()));

        // Since ScoresList listens to the property changes, the UI should update automatically
    }



}
