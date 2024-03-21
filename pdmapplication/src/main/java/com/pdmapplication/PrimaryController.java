package com.pdmapplication;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
    private PasswordField passwordField;
    
    private Connection connection;
    
    //DONT SAVE USERNAME AND PASSWORD TO GITHUB
    private static final String DB_URL = "jdbc:postgresql://127.0.0.1:5432/p320_19";
    public static String DB_USER = "none";
    public static String DB_PASSWORD = "none";
    
    public void initialize() {
        try {
            Scanner scanner = new Scanner(System.in);
        // Prompt the user for username
        System.out.print("Enter username: ");
        DB_USER = scanner.nextLine();

        // Prompt the user for password
        System.out.print("Enter password: ");
        DB_PASSWORD = scanner.nextLine();

        // Close the scanner
        scanner.close();

            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect to the database.");
        }
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
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM USR WHERE username = ? AND password = ?");
            statement.setString(1, username);
            statement.setString(2, password);
            
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                // Login successful
                recordAccessTime(username);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Login successful!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid username or password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while accessing the database.");
        }
    }
    
    private void recordAccessTime(String username) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO user_access(username, access_time) VALUES (?, ?)");
        statement.setString(1, username);
        statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
        statement.executeUpdate();
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
