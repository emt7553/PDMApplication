//Author: Alex Tefft

package com.pdmapplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.sql.Connection;

public class SecondaryController {
    private Connection connection;

    public SecondaryController(Connection connection) {
        this.connection = connection;
    }

    @FXML
    private void switchToPrimary(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("primary.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }

    @FXML
    private void switchToFollowing(ActionEvent event) throws IOException {
        FollowingController followingController = new FollowingController(connection);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("following.fxml"));
        loader.setController(followingController);

        Parent root = loader.load();

        Scene scene = new Scene(root);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }

    @FXML
    private void switchToMovie(ActionEvent event) throws IOException {
        MovieController movieController = new MovieController(connection);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("movie.fxml"));
        loader.setController(movieController);

        Parent root = loader.load();

        Scene scene = new Scene(root);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }
    @FXML
    private void switchToCollection(ActionEvent event) throws IOException {
        MovieController movieController = new MovieController(connection);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("collection.fxml"));
        loader.setController(movieController);

        Parent root = loader.load();

        Scene scene = new Scene(root);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }

    @FXML
    private void switchToMovieRecommendation(ActionEvent event) throws IOException {
        MovieRecommendationController movieRecommendationController = new MovieRecommendationController(connection);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MovieRecommendation.fxml"));
        loader.setController(movieRecommendationController);
        Parent root = loader.load();
        
        // Create a new Scene with the loaded root
        Scene scene = new Scene(root);
        
        // Get the current stage from the event source
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        // Set the scene for the stage
        stage.setScene(scene);
    }

}
