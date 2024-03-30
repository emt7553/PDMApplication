//Author: Alex Tefft

package com.pdmapplication;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.scene.control.Button;


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
    }

    @FXML
private void handleAddButtonAction() {
    String email = emailTextField.getText();
    if (email.isEmpty()) {
        showAlert(Alert.AlertType.ERROR, "Error", "Please enter email");
        return;
    }

    try {
        PreparedStatement findUserStatement = connection.prepareStatement("SELECT user_id FROM emails WHERE email = ?");
        findUserStatement.setString(1, email);
        ResultSet userResultSet = findUserStatement.executeQuery();

        if (userResultSet.next()) {
            int currentUserId = PrimaryController.currentUserId; 

            int foundUserId = userResultSet.getInt("user_id");

            PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO friendswith (friender_id, friendee_id) VALUES (?, ?)");
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

        PreparedStatement findUserStatement = connection.prepareStatement("SELECT user_id FROM emails WHERE email = ?");
        findUserStatement.setString(1, email);
        ResultSet userResultSet = findUserStatement.executeQuery();

        if (userResultSet.next()) {
            int currentUserId = PrimaryController.currentUserId; 

            int foundUserId = userResultSet.getInt("user_id");

            PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM friendswith WHERE (friender_id = ? AND friendee_id = ?) OR (friender_id = ? AND friendee_id = ?)");
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
