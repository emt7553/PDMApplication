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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;


public class MovieController {

    private Connection connection;
    public MovieController(Connection connection) {
        this.connection = connection;
    }

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Movie> movieTable;

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

    @FXML
    private ChoiceBox<String> sortByChoiceBox;

    @FXML
    private ChoiceBox<String> sortOrderChoiceBox;

    private ObservableList<Movie> movies = FXCollections.observableArrayList();


    public void initialize() {
        // Initialize the choice boxes
        sortByChoiceBox.getItems().addAll("Movie Name", "Studio", "Genre", "Released Year");
        sortOrderChoiceBox.getItems().addAll("Ascending", "Descending");
        sortByChoiceBox.setValue("Movie Name");
        sortOrderChoiceBox.setValue("Ascending");

        // Set up table columns
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
        // Execute SQL query based on the search query
        // Add results to the movies list
        // Update the table view
    }

    @FXML
    private void sort() {
        String sortBy = sortByChoiceBox.getValue();
        String sortOrder = sortOrderChoiceBox.getValue();
        // Implement sorting logic based on the selected criteria
    }

    private class Movie {
        private StringProperty title;
        private StringProperty cast;
        private StringProperty director;
        private IntegerProperty length;
        private StringProperty mpaaRating;
        private DoubleProperty userRating;
        private StringProperty releaseDate;

        public Movie(String title, String cast, String director, int length, String mpaaRating, double userRating, String releaseDate) {
            this.title = new SimpleStringProperty(title);
            this.cast = new SimpleStringProperty(cast);
            this.director = new SimpleStringProperty(director);
            this.length = new SimpleIntegerProperty(length);
            this.mpaaRating = new SimpleStringProperty(mpaaRating);
            this.userRating = new SimpleDoubleProperty(userRating);
            this.releaseDate = new SimpleStringProperty(releaseDate);
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
}
