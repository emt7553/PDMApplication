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
import javafx.scene.control.ListView;

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
    private TextField newEmailField;

    @FXML
    private ListView<String> collectionsListView;

    @FXML
    private Label collectionInfoLabel;

    public Connection connection;
    public static int currentUserId;
    // DONT SAVE USERNAME AND PASSWORD TO GITHUB
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
            // Load the PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");

            try (Scanner scanner = new Scanner(System.in)) {
                // Prompt the user for username
                System.out.print("Enter username: ");
                DB_USER = scanner.nextLine();
                SSH_USER = DB_USER;
                // Prompt the user for password
                System.out.print("Enter password: ");
                DB_PASSWORD = scanner.nextLine();
                SSH_PASSWORD = DB_PASSWORD;
            }

            // Establish SSH tunnel
            Session session = openSSHTunnel();
            if (session != null && session.isConnected()) {
                // Use the forwarded local port from the SSH session for the JDBC URL
                String jdbcUrl = "jdbc:postgresql://127.0.0.1:5432/p320_19";
                connection = DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASSWORD);
            } else {
                throw new SQLException("Failed to establish SSH tunnel.");
            }
            
            
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
            currentUserId = resultSet.getInt("userid");
            recordAccessTime(username);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Login successful!");

            // Load the secondary view
            SecondaryController secondaryController = new SecondaryController(connection);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("secondary.fxml"));
            loader.setController(secondaryController);
            
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
    String newEmail = newEmailField.getText().trim(); // Update with the new email
    LocalDateTime currentTime = LocalDateTime.now();

    if (newUsername.isEmpty() || newPassword.isEmpty()) {
        showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
        return;
    }

    try {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO usr (userid, lastname, firstname, username, password, email, creation, lastaccess) VALUES (3, ?, ?, ?, ?, ?, ?, ?)");

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
        try (PreparedStatement statement = connection.prepareStatement("UPDATE usr SET lastaccess = ? WHERE username = ?")) {
            statement.setString(2, username);
            statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while recording access time.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
  