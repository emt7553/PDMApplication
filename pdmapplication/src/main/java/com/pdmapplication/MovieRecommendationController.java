package com.pdmapplication;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import javafx.event.ActionEvent;


public class MovieRecommendationController {

    @FXML
    private TabPane tabPane;

    @FXML
    private ListView<String> top20MostPopularLast90DaysListView;

    @FXML
    private ListView<String> top20MostPopularFollowersListView;

    @FXML
    private ListView<String> top5LatestReleasesListView;

    @FXML
    private ListView<String> recommendationsForYouListView;

    private Connection connection;

    // Constructor that takes the database connection
    public MovieRecommendationController(Connection connection) {
        this.connection = connection;
    }

    @FXML
    public void initialize() {
        loadTop20MostPopularLast90Days();
        loadTop20MostPopularFollowers();
        loadTop5LatestReleases();
        loadRecommendationsForYou();
    }

    private void loadTop20MostPopularLast90Days() {
        try {
            String query = "SELECT m.movie_id, m.title, COUNT(w.user_id) AS watch_count " +
                            "FROM movie m " +
                            "JOIN watches w ON m.movie_id = w.movie_id " +
                            "JOIN released r ON m.movie_id = r.movie_id " +
                            "WHERE w.watchtime >= (CURRENT_DATE - INTERVAL '90 days') " +
                            "AND w.watchtime <= CURRENT_DATE " +
                            "GROUP BY m.movie_id, m.title " +
                            "ORDER BY watch_count DESC " +
                            "LIMIT 20;";
            
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            top20MostPopularLast90DaysListView.getItems().clear();


            while (resultSet.next()) {
                String title = resultSet.getString("title");
                top20MostPopularLast90DaysListView.getItems().add(title);
            }

            statement.close();
            resultSet.close();
                

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load top 20 most popular movies in the last 90 days.");
        }
    }

    

    private void loadTop20MostPopularFollowers() {
        try {
            // Query for the top 20 most popular movies among followers
            String query = "SELECT m.movie_id, m.title, COUNT(w.user_id) AS watch_count " +
                           "FROM movie m JOIN watches w ON m.movie_id = w.movie_id " +
                           "JOIN friendsWith f ON w.user_id = f.friendee_id " +
                           "GROUP BY m.movie_id, m.title " +
                           "ORDER BY watch_count DESC LIMIT 20;";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            // Display the result in the ListView
            top20MostPopularFollowersListView.getItems().clear();
            while (resultSet.next()) {
                String title = resultSet.getString("title");
                top20MostPopularFollowersListView.getItems().add(title);
            }

            statement.close();
            resultSet.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load top 20 most popular movies among followers.");
        }
    }

    private void loadTop5LatestReleases() {
        try {
            // Query for the top 5 most popular releases
            String query = "SELECT m.movie_id, m.title, COUNT(w.user_id) AS watch_count, r.release_year " +
                           "FROM movie m " +
                           "JOIN watches w ON m.movie_id = w.movie_id " +
                           "JOIN released r ON m.movie_id = r.movie_id " +
                           "GROUP BY m.movie_id, m.title, r.release_year " +
                           "ORDER BY watch_count DESC, r.release_year DESC " +
                           "LIMIT 5;";
    
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
    
            // Display the result in the ListView
            top5LatestReleasesListView.getItems().clear();
            while (resultSet.next()) {
                String title = resultSet.getString("title");
                top5LatestReleasesListView.getItems().add(title);
            }
    
            statement.close();
            resultSet.close();
    
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load top 5 most popular releases.");
        }
    }
    

    private void loadRecommendationsForYou() {
        try {
            // Query for recommendations based on user's play history
            String query = "SELECT m.movie_id, m.title, COUNT(w.user_id) AS watch_count " +
                           "FROM movie m JOIN hasgenre hg ON m.movie_id = hg.movie_id " +
                           "JOIN genre g ON hg.genre_id = g.genre_id " +
                           "JOIN watches w ON m.movie_id = w.movie_id " +
                           "WHERE w.user_id = ? " +
                           "GROUP BY m.movie_id, m.title " +
                           "HAVING COUNT(w.user_id) > 0 " +
                           "ORDER BY COUNT(w.user_id) DESC LIMIT 10;";
            PreparedStatement statement = connection.prepareStatement(query);
            
            int currentUserId = PrimaryController.currentUserId; 
            statement.setInt(1, currentUserId);
            ResultSet resultSet = statement.executeQuery();

            // Display the result in the ListView
            recommendationsForYouListView.getItems().clear();
            while (resultSet.next()) {
                String title = resultSet.getString("title");
                recommendationsForYouListView.getItems().add(title);
            }

            statement.close();
            resultSet.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load recommendations for you.");
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
    
        

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
