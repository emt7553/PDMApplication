package com.pdmapplication;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Scanner;

public class PrimaryController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField newUsernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private ListView<String> collectionsListView;

    @FXML
    private Label collectionInfoLabel;

    private Connection connection;

    private static final String DB_HOST = "127.0.0.1";
    private static final int DB_PORT = 5432;
    private static final String SSH_HOST = "starbug.cs.rit.edu";
    private static final int SSH_PORT = 22;
    private static String SSH_USER = "none";
    private static String SSH_PASSWORD = "none";
    private static String DB_USER = "none";
    private static String DB_PASSWORD = "none";

    public void initialize() {
        try {
            Class.forName("org.postgresql.Driver");

            try (Scanner scanner = new Scanner(System.in)) {
                System.out.print("Enter username: ");
                DB_USER = scanner.nextLine();
                SSH_USER = DB_USER;
                System.out.print("Enter password: ");
                DB_PASSWORD = scanner.nextLine();
                SSH_PASSWORD = DB_PASSWORD;
            }

            Session session = openSSHTunnel();
            if (session != null && session.isConnected()) {
                String jdbcUrl = "jdbc:postgresql://127.0.0.1:5432/p320_19";
                connection = DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASSWORD);
            } else {
                throw new SQLException("Failed to establish SSH tunnel.");
            }

            // Populate collections ListView
            populateCollectionsListView();

        } catch (ClassNotFoundException | SQLException | JSchException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect to the database.");
        }
    }

    private Session openSSHTunnel() throws SQLException, JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(SSH_USER, SSH_HOST, SSH_PORT);
        session.setPassword(SSH_PASSWORD);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        session.setPortForwardingL(DB_PORT, DB_HOST, DB_PORT);
        return session;
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
    
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter both username and password.");
            return;
        }
    
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM usr WHERE username = ? AND password = ?");
            statement.setString(1, username);
            statement.setString(2, password);
    
            ResultSet resultSet = statement.executeQuery();
    
            if (resultSet.next()) {
                // Login successful
                recordAccessTime(username);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Login successful!");
    
                // Load the secondary view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("secondary.fxml"));
                Parent root = loader.load();
    
                // Create a new scene for the secondary view
                Scene scene = new Scene(root);
    
                // Get the current stage and set its scene to the secondary view scene
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid username or password.");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "User not found.");
        }
    }

    @FXML
    private void handleRegistration(ActionEvent event) {
        String newUsername = newUsernameField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String newLastName = ""; // Update with the new last name
        String newFirstName = ""; // Update with the new first name
        String newEmail = ""; // Update with the new email
        LocalDateTime currentTime = LocalDateTime.now();

        if (newUsername.isEmpty() || newPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
            return;
        }

        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO usr (userid, lastname, firstname, username, password, email, creation, lastaccess) VALUES (1, ?, ?, ?, ?, ?, ?, ?)");
            statement.setString(1, newLastName);
            statement.setString(2, newFirstName);
            statement.setString(3, newUsername);
            statement.setString(4, newPassword);
            statement.setString(5, newEmail);
            statement.setTimestamp(6, Timestamp.valueOf(currentTime));
            statement.setTimestamp(7, Timestamp.valueOf(currentTime));

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "User registered successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to register user.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to register user.");
        }
    }

      private void recordAccessTime(String username) {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO usr(username, access_time) VALUES (?, ?)")) {
            statement.setString(1, username);
            statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while recording access time.");
        }
    }

    private void populateCollectionsListView() {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT collectionName FROM COLLECTION JOIN createsCollection ON COLLECTION.collectionID = createsCollection.collectionID WHERE createsCollection.userID = ?");
            statement.setString(1, DB_USER);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String collectionName = resultSet.getString("collectionName");
                collectionsListView.getItems().add(collectionName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to fetch collections.");
        }
    }

    @FXML
    private void handleCollectionSelection() {
        String selectedCollection = collectionsListView.getSelectionModel().getSelectedItem();
        if (selectedCollection != null) {
            displayCollectionInfo(selectedCollection);
        }
    }

    private void displayCollectionInfo(String collectionName) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT COUNT(movieID), SUM(length) FROM Contains JOIN COLLECTION ON Contains.collectionID = COLLECTION.collectionID JOIN MOVIE ON Contains.movieID = MOVIE.movieID WHERE COLLECTION.collectionName = ?");
            statement.setString(1, collectionName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int movieCount = resultSet.getInt(1);
                int totalLength = resultSet.getInt(2);
                int hours = totalLength / 60;
                int minutes = totalLength % 60;
                collectionInfoLabel.setText("Number of movies: " + movieCount + "\nTotal length: " + hours + " hours " + minutes + " minutes");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to fetch collection information.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

// package com.pdmapplication;

// import javafx.event.ActionEvent;
// import javafx.fxml.FXML;
// import javafx.scene.control.Alert;
// import javafx.scene.control.Label;
// import javafx.scene.control.ListView;

// public class PrimaryController {

//     @FXML
//     private ListView<String> collectionsListView;

//     @FXML
//     private Label collectionInfoLabel;

//     public void initialize() {
//         // Populate collections ListView with sample data
//         populateCollectionsListView();
//     }

//     // replace sample code with actual data from backend
//     private void populateCollectionsListView() {
//         // Add sample collection names
//         collectionsListView.getItems().addAll("Action Movies", "Comedy Movies", "Drama Movies");
//     }

//     @FXML
//     private void handleCollectionSelection(ActionEvent event) {
//         String selectedCollection = collectionsListView.getSelectionModel().getSelectedItem();
//         if (selectedCollection != null) {
//             displayCollectionInfo(selectedCollection);
//         }
//     }


//     private void displayCollectionInfo(String collectionName) {
//         // Sample implementation to display collection information
//         collectionInfoLabel.setText("This is a sample collection: " + collectionName);
//     }

//     private void showAlert(Alert.AlertType type, String title, String content) {
//         Alert alert = new Alert(type);
//         alert.setTitle(title);
//         alert.setContentText(content);
//         alert.showAndWait();
//     }
// }



// <!-- <AnchorPane maxHeight="-Infinity" maxWidth="-Infinity" minHeight="-Infinity" minWidth="-Infinity" prefHeight="400.0" prefWidth="600.0" xmlns="http://javafx.com/javafx/16" xmlns:fx="http://javafx.com/fxml/1" fx:controller="com.pdmapplication.PrimaryController">
//    <children>
//       <TextField fx:id="usernameField" layoutX="226.0" layoutY="80.0" promptText="Username" />
//       <PasswordField fx:id="passwordField" layoutX="226.0" layoutY="140.0" promptText="Password" />
//       <Button layoutX="274.0" layoutY="200" mnemonicParsing="false" onAction="#handleLogin" text="Login" />

//       <!-- Add ListView for displaying collections -->
//       <ListView fx:id="collectionsListView" layoutX="226.0" layoutY="240.0" prefHeight="100.0" prefWidth="300.0" onMouseClicked="#handleCollectionSelection" />

//       <!-- Add Label for displaying collection information -->
//       <Label fx:id="collectionInfoLabel" layoutX="226.0" layoutY="360.0" prefHeight="30.0" prefWidth="300.0" />
//    </children>
// </AnchorPane> -->