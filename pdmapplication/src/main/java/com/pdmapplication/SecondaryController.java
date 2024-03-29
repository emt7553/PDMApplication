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

        // Create a new scene for the primary view
        Scene scene = new Scene(root);

        // Get the current stage and set its scene to the primary view scene
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }

    @FXML
    private void switchToFollowing(ActionEvent event) throws IOException {
        FollowingController followingController = new FollowingController(connection);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("following.fxml"));
        loader.setController(followingController);

        Parent root = loader.load();

        // Create a new scene for the following view
        Scene scene = new Scene(root);

        // Get the current stage and set its scene to the following view scene
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }
}
