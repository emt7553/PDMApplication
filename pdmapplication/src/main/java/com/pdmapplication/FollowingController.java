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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class FollowingController {
    @FXML
    private TextField emailTextField;

    @FXML
    private Button addButton;

    @FXML
    private Button removeButton;

    private Connection connection;
    
    public FollowingController(Connection connection) {
        this.connection = connection;
    }


    @FXML
    public void initialize() {
        // Initialize method, you can add initialization code here if needed
    }

    @FXML
private void handleAddButtonAction() {
    String email = emailTextField.getText();
    if (email.isEmpty()) {
        showAlert(Alert.AlertType.ERROR, "Error", "Please enter email");
        return;
    }

    try {
        // Find user by email
        PreparedStatement findUserStatement = connection.prepareStatement("SELECT userid FROM usr WHERE email = ?");
        findUserStatement.setString(1, email);
        ResultSet userResultSet = findUserStatement.executeQuery();

        if (userResultSet.next()) {
            // Current user's ID (assuming you have it available in your controller)
            int currentUserId = PrimaryController.currentUserId; // Change this line according to your implementation

            // Found user's ID
            int foundUserId = userResultSet.getInt("userid");

            // Insert into the friendswith table
            PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO friendswith (frienderid, friendeeid) VALUES (?, ?)");
            insertStatement.setInt(1, currentUserId);
            insertStatement.setInt(2, foundUserId);
            insertStatement.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Followed user!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid email.");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Database Error", "User not found.");
    }
}


@FXML
private void handleRemoveButtonAction() {
    String email = emailTextField.getText();
    if (email.isEmpty()) {
        showAlert(Alert.AlertType.ERROR, "Error", "Please enter email");
        return;
    }

    try {
        // Find user by email
        PreparedStatement findUserStatement = connection.prepareStatement("SELECT userid FROM usr WHERE email = ?");
        findUserStatement.setString(1, email);
        ResultSet userResultSet = findUserStatement.executeQuery();

        if (userResultSet.next()) {
            // Current user's ID (assuming you have it available in your controller)
            int currentUserId = PrimaryController.currentUserId; // Change this line according to your implementation

            // Found user's ID
            int foundUserId = userResultSet.getInt("userid");

            // Remove from the friendswith table
            PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM friendswith WHERE (frienderid = ? AND friendeeid = ?) OR (frienderid = ? AND friendeeid = ?)");
            deleteStatement.setInt(1, currentUserId);
            deleteStatement.setInt(2, foundUserId);
            deleteStatement.setInt(3, foundUserId);
            deleteStatement.setInt(4, currentUserId);
            int deletedRows = deleteStatement.executeUpdate();

            if (deletedRows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Unfollowed user!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No friendship record found with this user.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid email.");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Database Error", "User not found.");
    }
}


    @FXML
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
