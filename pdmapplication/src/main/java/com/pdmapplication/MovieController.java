package com.pdmapplication;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.Node;
import javafx.scene.Parent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MovieController {

    private Connection connection;
    public MovieController(Connection connection) {
        this.connection = connection;
    }
    private int selectedId;

    @FXML
    private TextField searchField;

    @FXML
    private TextField movieIdField;

    @FXML
    private TableView<Movie> movieTable;

    @FXML
    private TableColumn<Movie, Integer> idColumn;

    @FXML
    private TableColumn<Movie, String> titleColumn;

    @FXML
    private TableColumn<Movie, String> castColumn;

    @FXML
    private TableColumn<Movie, String> directorColumn;

    @FXML
    private TableColumn<Movie, Integer> lengthColumn;

    @FXML
    private TableColumn<Movie, String> mpaaRatingColumn;

    @FXML
    private TableColumn<Movie, Double> userRatingColumn;

    @FXML
    private TableColumn<Movie, String> releaseDateColumn;

    private ObservableList<Movie> movies = FXCollections.observableArrayList();

    public void initialize() {
        // Set up table columns
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        castColumn.setCellValueFactory(cellData -> cellData.getValue().castProperty());
        directorColumn.setCellValueFactory(cellData -> cellData.getValue().directorProperty());
        lengthColumn.setCellValueFactory(cellData -> cellData.getValue().lengthProperty().asObject());
        mpaaRatingColumn.setCellValueFactory(cellData -> cellData.getValue().mpaaRatingProperty());
        userRatingColumn.setCellValueFactory(cellData -> cellData.getValue().userRatingProperty().asObject());
        releaseDateColumn.setCellValueFactory(cellData -> cellData.getValue().releaseDateProperty());

        // Populate the table
        movieTable.setItems(movies);
    }

    @FXML
    private void search() {
        String searchQuery = searchField.getText().trim();
        movies.clear(); // Clear existing movies

        try {
            // Construct the SQL query based on the search query
            String sqlQuery = "SELECT m.movie_id, m.title, STRING_AGG(a.name, ', ') AS cast, c.name AS director, m.length, m.mpaa_rating, ra.starrating, r.release_year " +
                    "FROM movie m " +
                    "LEFT JOIN (SELECT movie_id, STRING_AGG(name, ', ') AS name FROM actor a INNER JOIN contributor ac ON a.contributor_id = ac.id GROUP BY movie_id) a ON m.movie_id = a.movie_id " +
                    "LEFT JOIN (SELECT d.movie_id, co.name FROM director d INNER JOIN contributor co ON d.contributor_id = co.id) c ON m.movie_id = c.movie_id " +
                    "LEFT JOIN rates ra ON m.movie_id = ra.movie_id " +
                    "INNER JOIN released r ON m.movie_id = r.movie_id " +
                    "WHERE m.title::text LIKE ? OR r.release_year::text LIKE ? OR a.name LIKE ? " +
                    "GROUP BY m.movie_id, m.title, m.length, m.mpaa_rating, ra.starrating, r.release_year, c.name";

            PreparedStatement statement = connection.prepareStatement(sqlQuery);
            statement.setString(1, "%" + searchQuery + "%"); // Title
            statement.setString(2, searchQuery); // Release year
            statement.setString(3, "%" + searchQuery + "%"); // Cast

            ResultSet resultSet = statement.executeQuery();

            // Add the retrieved movies to the movies list
            while (resultSet.next()) {
                int movieid = resultSet.getInt("movie_id");
                String title = resultSet.getString("title");
                String cast = resultSet.getString("cast"); // Adjusted to match the column alias from the SQL query
                String director = resultSet.getString("director"); // Adjusted to match the column alias from the SQL query
                int length = resultSet.getInt("length");
                String mpaaRating = resultSet.getString("mpaa_rating");
                double userRating = resultSet.getDouble("starrating"); // Adjusted to match the column alias from the SQL query
                String releaseDate = resultSet.getString("release_year");

                Movie movie = new Movie(movieid, title, cast, director, length, mpaaRating, userRating, releaseDate);
                movies.add(movie);
            }

            // Update the TableView
            movieTable.setItems(movies);

            statement.close();
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQL exception
        }
    }

    @FXML
    private void selectMovieById(ActionEvent event) {
    int selectedMovieId = Integer.parseInt(movieIdField.getText());

    try {
        RatingController ratingController = new RatingController(connection);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("rating.fxml"));
        ratingController.setMovieID(selectedMovieId);
        loader.setController(ratingController);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        // Get the current stage and set its scene to the following view scene
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);

        // Close the current window
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    private class Movie {
        private IntegerProperty movieid;
        private StringProperty title;
        private StringProperty cast;
        private StringProperty director;
        private IntegerProperty length;
        private StringProperty mpaaRating;
        private DoubleProperty userRating;
        private StringProperty releaseDate;

        public Movie(int id, String title, String cast, String director, int length, String mpaaRating, double userRating, String releaseDate) {
            this.movieid = new SimpleIntegerProperty(id);
            this.title = new SimpleStringProperty(title);
            this.cast = new SimpleStringProperty(cast);
            this.director = new SimpleStringProperty(director);
            this.length = new SimpleIntegerProperty(length);
            this.mpaaRating = new SimpleStringProperty(mpaaRating);
            this.userRating = new SimpleDoubleProperty(userRating);
            this.releaseDate = new SimpleStringProperty(releaseDate);
        }

        public IntegerProperty idProperty() {
            return movieid;
        }

        public StringProperty titleProperty() {
            return title;
        }

        public StringProperty castProperty() {
            return cast;
        }

        public StringProperty directorProperty() {
            return director;
        }

        public IntegerProperty lengthProperty() {
            return length;
        }

        public StringProperty mpaaRatingProperty() {
            return mpaaRating;
        }

        public DoubleProperty userRatingProperty() {
            return userRating;
        }

        public StringProperty releaseDateProperty() {
            return releaseDate;
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
