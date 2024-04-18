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
        
        int numberOfCollections = getNumberOfCollections();
        int numberOfFollowedUsers = getNumberOfFollowedUsers();
        int numberOfFollowingUsers = getNumberOfFollowingUsers();
        collectionsLabel.setText(""+numberOfCollections);
        followedUsersLabel.setText(""+numberOfFollowedUsers);
        followingUsersLabel.setText(""+numberOfFollowingUsers);
        findTopMovies(null);
    }

    private int getNumberOfCollections() {
        int numberOfCollections = 0;
        try {
            String query = "SELECT COUNT(*) FROM createscollection WHERE user_id = ?";
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

    private int getNumberOfFollowedUsers() {
        int numberOfFollowedUsers = 0;
        try {
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

    private int getNumberOfFollowingUsers() {
        int numberOfFollowingUsers = 0;
        try {
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
        sortingCriteriaChoiceBox.setOnAction(this::findTopMovies);
        String sortingCriteria = sortingCriteriaChoiceBox.getValue();
        if (sortingCriteria == null) {
            return;
        }

        try {
            String query = "SELECT m.title FROM movie m " +
                           "JOIN rates r ON m.movie_id = r.movie_id " +
                           "JOIN watches w ON m.movie_id = w.movie_id " +
                           "WHERE r.user_id = ? AND w.user_id = ? " +
                           "GROUP BY m.title " + 
                           "ORDER BY ";
            switch (sortingCriteria) {
                case "Highest Rating":
                    query += "MAX(r.starrating) DESC"; 
                    break;
                case "Most Plays":
                    query += "COUNT(w.*) DESC";
                    break;
                case "Combination":
                    query += "MAX(r.starrating) DESC, COUNT(w.*) DESC"; 
                    break;
            }
            query += " LIMIT 10";


            PreparedStatement statement = connection.prepareStatement(query);
            int currentUserId = PrimaryController.currentUserId;
            statement.setInt(1, currentUserId);
            statement.setInt(2, currentUserId);

            ResultSet resultSet = statement.executeQuery();

            topMoviesContainer.getChildren().clear();
            while (resultSet.next()) {
                String movieTitle = resultSet.getString("title");
                Label movieLabel = new Label(movieTitle);
                topMoviesContainer.getChildren().add(movieLabel);
            }
            sortingCriteriaChoiceBox.setItems(FXCollections.observableArrayList(
                "Highest Rating", "Most Plays", "Combination"
            ));
            sortingCriteriaChoiceBox.setValue(sortingCriteria);

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
