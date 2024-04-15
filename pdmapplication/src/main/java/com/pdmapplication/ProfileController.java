//Author: Alex Tefft

package com.pdmapplication;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfileController {
    @FXML
    private Label collectionsLabel;

    @FXML
    private Label followedUsersLabel;

    @FXML
    private Label followingUsersLabel;

    @FXML
    private VBox topMoviesContainer;

    @FXML
    private ChoiceBox<String> sortingCriteriaChoiceBox;

    private Connection connection;

    public ProfileController(Connection connection) {
        this.connection = connection;
    }

    @FXML
    public void initialize() {
        sortingCriteriaChoiceBox.setItems(FXCollections.observableArrayList(
                "Highest Rating", "Most Plays", "Combination"
        ));
        
        int numberOfCollections = getNumberOfCollections(); // Call a method to get the number of collections
        collectionsLabel.setText("Collections: " + numberOfCollections);
    }

    private int getNumberOfCollections() {
        int numberOfCollections = 0;
        try {
            // Execute a query to get the number of collections for the user
            String query = "SELECT COUNT(*) FROM collections WHERE user_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            int currentUserId = PrimaryController.currentUserId; 
            statement.setInt(1, currentUserId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                numberOfCollections = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return numberOfCollections;
    }

    @FXML
    private int getNumberOfFollowedUsers() {
        int numberOfFollowedUsers = 0;
        try {
            // Execute a query to get the number of followed users for the user
            String query = "SELECT COUNT(*) FROM friendswith WHERE friender_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            int currentUserId = PrimaryController.currentUserId; 
            statement.setInt(1, currentUserId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                numberOfFollowedUsers = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return numberOfFollowedUsers;
    }

    @FXML
    private int getNumberOfFollowingUsers() {
        int numberOfFollowingUsers = 0;
        try {
            // Execute a query to get the number of following users for the user
            String query = "SELECT COUNT(*) FROM friendswith WHERE friendee_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            int currentUserId = PrimaryController.currentUserId; 
            statement.setInt(1, currentUserId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                numberOfFollowingUsers = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return numberOfFollowingUsers;
    }

    @FXML
    private void findTopMovies(ActionEvent event) {
        String sortingCriteria = sortingCriteriaChoiceBox.getValue();
        if (sortingCriteria == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a sorting criteria");
            return;
        }

        try {
            // Execute a query to get the user's top 10 movies based on the sorting criteria
            String query = "SELECT * FROM movies WHERE user_id = ? ORDER BY ";
            switch (sortingCriteria) {
                case "Highest Rating":
                    query += "rating DESC";
                    break;
                case "Most Plays":
                    query += "plays DESC";
                    break;
                case "Combination":
                    query += "rating DESC, plays DESC";
                    break;
            }
            query += " LIMIT 10";

            PreparedStatement statement = connection.prepareStatement(query);
            int currentUserId = PrimaryController.currentUserId;
            statement.setInt(1, currentUserId);
            ResultSet resultSet = statement.executeQuery();

            // Clear the existing top movies container
            topMoviesContainer.getChildren().clear();

            // Populate the top movies container with the retrieved movies
            while (resultSet.next()) {
                String movieTitle = resultSet.getString("title");
                Label movieLabel = new Label(movieTitle);
                topMoviesContainer.getChildren().add(movieLabel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void switchToSecondary(ActionEvent event) throws IOException {
        SecondaryController secondaryController = new SecondaryController(connection);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("secondary.fxml"));
        loader.setController(secondaryController);
        Parent root = loader.load();
        Scene scene = new Scene(root);
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
