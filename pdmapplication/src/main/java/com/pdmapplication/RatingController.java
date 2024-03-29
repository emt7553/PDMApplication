package com.pdmapplication;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Scanner;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class RatingController {
    @FXML
    private TextField ratingFieldText;

    @FXML
    private Button watchMovie;

    @FXML
    private Button watchCollection;

    public Connection connection;
    public int movieID;
    public int collectionID;
    public RatingController(Connection connection) {
        this.connection = connection;
        this.movieID = movieID;
    }

    @FXML
    public void initialize() {
        // Initialize method, you can add initialization code here if needed
    }

    @FXML
    private void handleRateButtonAction() {
    try{
        int rating = Integer.valueOf(ratingFieldText.getText());
    

    try {

        PreparedStatement findMovieStatement = connection.prepareStatement("SELECT movie_id FROM movie WHERE movie_id = ?");
        findMovieStatement.setInt(1, movieID);
        ResultSet movieResultSet = findMovieStatement.executeQuery();

        if (movieResultSet.next()) {
            int currentUserId = PrimaryController.currentUserId; 

            PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO rates (user_id, movie_id, starrating) VALUES (?, ?, ?)");
            insertStatement.setInt(1, currentUserId);
            insertStatement.setInt(2, movieID);
            insertStatement.setInt(3, rating);
            insertStatement.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Rated movie!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Error rating movie!");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Database Error", "Movie not found!");
    }
} catch(NumberFormatException e) {
    showAlert(Alert.AlertType.ERROR, "Error", "Enter valid rating!");
}
}

@FXML
    private void handleWatchCollectionButtonAction() {
        try {
            int currentUserId = PrimaryController.currentUserId;

            // Retrieve movies in the collection from the contains table
            PreparedStatement containsStatement = connection.prepareStatement("SELECT movie_id FROM contains WHERE collection_id = ?");
            containsStatement.setInt(1, collectionID);
            ResultSet resultSet = containsStatement.executeQuery();

            // Iterate through each movie in the collection and mark it as watched
            while (resultSet.next()) {
                int movieID = resultSet.getInt("movie_id");
                markAsWatched(movieID, currentUserId);
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Marked all movies in the collection as watched!");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to mark all movies in the collection as watched!");
        }
    }
    private void markAsWatched(int movieID, int userID) {
        try {
            PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO watches (user_id, movie_id, watchtime) VALUES (?, ?, ?)");
            insertStatement.setInt(1, userID);
            insertStatement.setInt(2, movieID);
            insertStatement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            insertStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
    }



@FXML
private void switchToSecondary(ActionEvent event) throws IOException {
    SecondaryController secondaryController = new SecondaryController(connection);

    FXMLLoader loader = new FXMLLoader(getClass().getResource("secondary.fxml"));
    loader.setController(secondaryController);

    Parent root = loader.load();

    // Create a new scene for the primary view
    Scene scene = new Scene(root);

    // Get the current stage and set its scene to the primary view scene
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.setScene(scene);
}

    @FXML
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
