package com.pdmapplication;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class CollectionController {

    @FXML
    private ListView<String> collectionsListView;

    @FXML
    private ListView<String> moviesListView;

    @FXML
    private TextField searchField;

    @FXML
    private ChoiceBox<String> sortByChoiceBox;

    private Connection connection;

    // Getter and setter for connection
    public Connection getConnection() {
        return connection;
    }

    public CollectionController(Connection connection) {
        this.connection = connection;
    }

    @FXML
    public void initialize() {
        populateCollectionsListView();
        
    }

    private void populateCollectionsListView() {
        collectionsListView.getItems().clear(); // Clear the list view before populating

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT c.collectionID, c.collectionName, COUNT(cm.movieID) AS movieCount, SEC_TO_TIME(SUM(m.length * 60)) AS totalLength " +
                    "FROM COLLECTION c LEFT JOIN Collection_Movie cm ON c.collectionID = cm.collectionID " +
                    "LEFT JOIN Movie m ON cm.movieID = m.movieID GROUP BY c.collectionID ORDER BY c.collectionName");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String collectionID = resultSet.getString("collectionID");
                String collectionName = resultSet.getString("collectionName");
                int movieCount = resultSet.getInt("movieCount");
                String totalLength = resultSet.getString("totalLength");
                collectionsListView.getItems().add(collectionID + ": " + collectionName + " | Movies: " + movieCount + " | Total Length: " + totalLength);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to fetch collections.");
        }
    }

    @FXML
    private void createCollection() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Collection");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter collection name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(collectionName -> {
            try {
                // Insert the new collection into the database
                PreparedStatement statement = connection.prepareStatement("INSERT INTO COLLECTION (collectionName) VALUES (?)");
                statement.setString(1, collectionName);
                int rowsInserted = statement.executeUpdate();

                if (rowsInserted > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Collection created successfully!");
                    populateCollectionsListView(); // Refresh the list view
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to create collection.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to create collection.");
            }
        });
    }

    @FXML
    private void deleteCollection() {
        String selectedCollection = collectionsListView.getSelectionModel().getSelectedItem();
        if (selectedCollection != null) {
            String[] parts = selectedCollection.split(": ");
            String collectionID = parts[0];

            try {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM COLLECTION WHERE collectionID = ?");
                statement.setString(1, collectionID);
                int rowsDeleted = statement.executeUpdate();

                if (rowsDeleted > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Collection deleted successfully!");
                    populateCollectionsListView(); // Refresh the list view
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete collection.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete collection.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a collection to delete.");
        }
    }

    // Helper method to get the movie ID from the selected movie in the moviesListView
    private String getSelectedMovieID(ListView<String> moviesListView) {
        String selectedMovie = moviesListView.getSelectionModel().getSelectedItem();
        if (selectedMovie != null) {
            String[] parts = selectedMovie.split(" | ");
            return parts[0]; // Assuming movie ID is the first part
        } else {
            return null;
        }
    }

    @FXML
    private void addMovieToCollection() {
        String selectedCollection = collectionsListView.getSelectionModel().getSelectedItem();
        String selectedMovieID = getSelectedMovieID(moviesListView);
        if (selectedCollection != null && selectedMovieID != null) {
            String[] parts = selectedCollection.split(": ");
            String collectionID = parts[0];

            try {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO Collection_Movie (collectionID, movieID) VALUES (?, ?)");
                statement.setString(1, collectionID);
                statement.setString(2, selectedMovieID);
                int rowsInserted = statement.executeUpdate();

                if (rowsInserted > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Movie added to collection successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add movie to collection.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add movie to collection.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a collection and a movie to add.");
        }
    }

    @FXML
    private void removeMovieFromCollection() {
        String selectedCollection = collectionsListView.getSelectionModel().getSelectedItem();
        String selectedMovieID = getSelectedMovieID(moviesListView);
        if (selectedCollection != null && selectedMovieID != null) {
            String[] parts = selectedCollection.split(": ");
            String collectionID = parts[0];

            try {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM Collection_Movie WHERE collectionID = ? AND movieID = ?");
                statement.setString(1, collectionID);
                statement.setString(2, selectedMovieID);
                int rowsDeleted = statement.executeUpdate();

                if (rowsDeleted > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Movie removed from collection successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to remove movie from collection.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to remove movie from collection.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a collection and a movie to remove.");
        }
    }

    @FXML
    private void modifyCollectionName() {
        String selectedCollection = collectionsListView.getSelectionModel().getSelectedItem();
        if (selectedCollection != null) {
            String[] parts = selectedCollection.split(": ");
            String collectionID = parts[0];

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Modify Collection Name");
            dialog.setHeaderText(null);
            dialog.setContentText("Enter new collection name:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newCollectionName -> {
                try {
                    PreparedStatement statement = connection.prepareStatement("UPDATE COLLECTION SET collectionName = ? WHERE collectionID = ?");
                    statement.setString(1, newCollectionName);
                    statement.setString(2, collectionID);
                    int rowsUpdated = statement.executeUpdate();

                    if (rowsUpdated > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Collection name updated successfully!");
                        populateCollectionsListView(); // Refresh the list view
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to update collection name.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update collection name.");
                }
            });
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a collection to modify its name.");
        }
    }

    // Method to populate movies list view based on search criteria and sorting
    @FXML
    private void populateMoviesListView() {
        moviesListView.getItems().clear(); // Clear the list view before populating

        try {
            // Construct the SQL query based on the selected sorting criteria
            String query = "SELECT * FROM Movie ORDER BY";
            switch (sortByChoiceBox.getValue()) {
                case "Movie Name":
                    query += " movieName";
                    break;
                case "Studio":
                    query += " studio";
                    break;
                case "Genre":
                    query += " genre";
                    break;
                case "Release Year (Ascending)":
                    query += " releaseYear ASC";
                    break;
                case "Release Year (Descending)":
                    query += " releaseYear DESC";
                    break;
                default:
                    query += " movieName"; // Default sorting by movie name
            }

            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String movieName = resultSet.getString("movieName");
                String castMembers = resultSet.getString("castMembers");
                String director = resultSet.getString("director");
                int length = resultSet.getInt("length");
                String mpaaRating = resultSet.getString("mpaaRating");
                double userRating = resultSet.getDouble("userRating");

                // Construct the string representation of movie information
                String movieInfo = String.format("%s | Cast: %s | Director: %s | Length: %d min | MPAA Rating: %s | User Rating: %.1f",
                        movieName, castMembers, director, length, mpaaRating, userRating);
                moviesListView.getItems().add(movieInfo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to fetch movies.");
        }
    }

    @FXML
    private void searchMovies() {
        String searchTerm = searchField.getText().trim();

        if (searchTerm.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a search term.");
            return;
        }

        moviesListView.getItems().clear(); // Clear the list view before populating

        try {
            String query = "SELECT * FROM Movie WHERE movieName LIKE ? OR releaseYear LIKE ? OR castMembers LIKE ? OR studio LIKE ? OR genre LIKE ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, "%" + searchTerm + "%"); // Movie Name
            statement.setString(2, "%" + searchTerm + "%"); // Release Year
            statement.setString(3, "%" + searchTerm + "%"); // Cast Members
            statement.setString(4, "%" + searchTerm + "%"); // Studio
            statement.setString(5, "%" + searchTerm + "%"); // Genre

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String movieName = resultSet.getString("movieName");
                String castMembers = resultSet.getString("castMembers");
                String director = resultSet.getString("director");
                int length = resultSet.getInt("length");
                String mpaaRating = resultSet.getString("mpaaRating");
                double userRating = resultSet.getDouble("userRating");

                // Construct the string representation of movie information
                String movieInfo = String.format("%s | Cast: %s | Director: %s | Length: %d min | MPAA Rating: %s | User Rating: %.1f",
                        movieName, castMembers, director, length, mpaaRating, userRating);
                moviesListView.getItems().add(movieInfo);
            }

            if (moviesListView.getItems().isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Results", "No movies found matching the search criteria.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to search for movies.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
