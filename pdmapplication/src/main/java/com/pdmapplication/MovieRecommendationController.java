package com.pdmapplication;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class MovieRecommendationController {

    @FXML
    private TabPane tabPane;

    private VBox top20MostPopularLast90DaysListView;
    private VBox top20MostPopularFollowersListView;
    private VBox top5LatestReleasesListView;
    private VBox recommendationsForYouListView;

    // @FXML
    // private ListView<String> top20MostPopularLast90DaysListView;

    // @FXML
    // private ListView<String> top20MostPopularFollowersListView;

    // @FXML
    // private ListView<String> top5LatestReleasesListView;

    // @FXML
    // private ListView<String> recommendationsForYouListView;

    private Connection connection;

    // Constructor that takes the database connection
    public MovieRecommendationController(Connection connection) {
        this.connection = connection;
    }

    @FXML
    public void initialize() {
        showAlert(Alert.AlertType.ERROR, "ResultSet", "start");
        System.out.println("working");
        loadTop20MostPopularLast90Days();
        loadTop20MostPopularFollowers();
        loadTop5LatestReleases();
        loadRecommendationsForYou();
    }

    private void loadTop20MostPopularLast90Days() {
        
        try {
            // Query for the top 20 most popular movies in the last 90 days
            showAlert(Alert.AlertType.ERROR, "ResultSet", "1");
            String query = "SELECT m.movie_id, m.title, r.release_year " +
                           "FROM movie m JOIN released r ON m.movie_id = r.movie_id " +
                           "ORDER BY r.release_year DESC LIMIT 20;";
            showAlert(Alert.AlertType.ERROR, "ResultSet", "2");
            System.out.println("Print query test 1");
            PreparedStatement statement = connection.prepareStatement(query);
            showAlert(Alert.AlertType.ERROR, "ResultSet", "3");
            ResultSet resultSet = statement.executeQuery();
            showAlert(Alert.AlertType.ERROR, "ResultSet", "4");
            System.out.println("result set: " + resultSet);
            showAlert(Alert.AlertType.ERROR, "ResultSet", "wokign");

            // Display the result in the ListView
            // top20MostPopularLast90DaysListView.getChildren().clear();


            while (resultSet.next()) {
                String title = resultSet.getString("title");
                showAlert(Alert.AlertType.ERROR, "ResultSet", "loop");
                Label movieLabel = new Label(title);
                showAlert(Alert.AlertType.ERROR, "ResultSet", "add");
                top20MostPopularLast90DaysListView.getChildren().add(movieLabel);
                showAlert(Alert.AlertType.ERROR, "ResultSet", "afteradd");
                
                System.out.println(title);
            }

            // statement.close();
            // resultSet.close();     

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
                           "WHERE f.friender_id = 8 " +
                           "GROUP BY m.movie_id, m.title " +
                           "ORDER BY watch_count DESC LIMIT 20;";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            // Display the result in the ListView
            top20MostPopularFollowersListView.getChildren().clear();
            while (resultSet.next()) {
                String title = resultSet.getString("title");
                Label movieLabel = new Label(title);
                top20MostPopularFollowersListView.getChildren().add(movieLabel);
            }

            // statement.close();
            // resultSet.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load top 20 most popular movies among followers.");
        }
    }

    private void loadTop5LatestReleases() {
        try {
            // Query for the top 5 latest releases of the month
            String query = "SELECT m.movie_id, m.title, r.release_year " +
                           "FROM movie m JOIN released r ON m.movie_id = r.movie_id " +
                           "ORDER BY r.release_year DESC LIMIT 5;";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            // Display the result in the ListView
            top5LatestReleasesListView.getChildren().clear();
            while (resultSet.next()) {
                String title = resultSet.getString("title");
                Label movieLabel = new Label(title);
                top5LatestReleasesListView.getChildren().add(movieLabel);
            }

            // statement.close();
            // resultSet.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load top 5 latest releases of the month.");
        }
    }

    private void loadRecommendationsForYou() {
        try {
            // Query for recommendations based on user's play history
            String query = "SELECT m.movie_id, m.title, COUNT(w.user_id) AS watch_count " +
                           "FROM movie m JOIN hasgenre hg ON m.movie_id = hg.movie_id " +
                           "JOIN genre g ON hg.genre_id = g.genre_id " +
                           "JOIN watches w ON m.movie_id = w.movie_id " +
                           "WHERE w.user_id = 1 " +
                           "GROUP BY m.movie_id, m.title " +
                           "HAVING COUNT(w.user_id) > 0 " +
                           "ORDER BY COUNT(w.user_id) DESC LIMIT 5;";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            // Display the result in the ListView
            recommendationsForYouListView.getChildren().clear();
            while (resultSet.next()) {
                String title = resultSet.getString("title");
                Label movieLabel = new Label(title);
                recommendationsForYouListView.getChildren().add(movieLabel);
            }

            // statement.close();
            // resultSet.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load recommendations for you.");
        }

    
    }



    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}