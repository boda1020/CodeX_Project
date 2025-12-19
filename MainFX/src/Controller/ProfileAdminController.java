package Controller;

import Model.User;
import DAO.UserDAO;
import DAO.Impl.UserDAOImpl;
import Utils.CurrentUser;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class ProfileAdminController {

    @FXML private Button btnBack;
    @FXML private Button btnChangePhoto;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    @FXML private Label adminNameLabel;

    @FXML private TextField fullNameField;
    @FXML private TextField adminIdField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;

    @FXML private PasswordField oldPassField;
    @FXML private PasswordField newPassField;
    @FXML private PasswordField confirmPassField;

    @FXML private Circle profileCircle;

    private User currentUser;
    private final UserDAO userDAO = new UserDAOImpl();

    @FXML
    private void initialize() {
        currentUser = CurrentUser.getCurrentUser();

        if (currentUser == null) {
            showAlert("Error", "Unable to load profile. Please login again.", Alert.AlertType.ERROR);
            return;
        }

        loadProfileData();
    }

    private void loadProfileData() {
        adminNameLabel.setText(currentUser.getFullName());

        fullNameField.setText(currentUser.getFullName());
        adminIdField.setText(currentUser.getUsername());  // username هو admin ID
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");

        // الصورة
        if (currentUser.getProfileImagePath() != null && !currentUser.getProfileImagePath().isEmpty()) {
            try {
                Image image = new Image(new File(currentUser.getProfileImagePath()).toURI().toString());
                profileCircle.setFill(new ImagePattern(image));
            } catch (Exception e) {
                setupDefaultCircle();
            }
        } else {
            setupDefaultCircle();
        }
    }

    private void setupDefaultCircle() {
        profileCircle.setFill(javafx.scene.paint.Color.web("#262451"));
        profileCircle.setStroke(javafx.scene.paint.Color.web("#1a1d3d"));
        profileCircle.setStrokeWidth(5);
    }

    @FXML
    private void handleChangePhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Profile Picture");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) btnChangePhoto.getScene().getWindow();
        File file = chooser.showOpenDialog(stage);

        if (file != null) {
            Image image = new Image(file.toURI().toString());
            profileCircle.setFill(new ImagePattern(image));

            currentUser.setProfileImagePath(file.getAbsolutePath());
            userDAO.updateProfileImage(currentUser.getUserId(), file.getAbsolutePath());

            showAlert("Success", "Profile picture changed successfully!", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleSaveChanges() {
        if (!validateFields()) return;

        currentUser.setFullName(fullNameField.getText().trim());
        currentUser.setEmail(emailField.getText().trim());
        currentUser.setPhone(phoneField.getText().trim());

        if (!newPassField.getText().isEmpty()) {
            currentUser.setPasswordHash(newPassField.getText());  // نص عادي
        }

        boolean success = userDAO.updateUser(currentUser);

        if (success) {
            CurrentUser.setCurrentUser(currentUser);
            loadProfileData();
            clearPasswordFields();
            showAlert("Success", "Profile updated successfully!", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Error", "Failed to update profile.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Changes");
        confirm.setHeaderText("Discard all changes?");
        confirm.setContentText("Are you sure you want to cancel? All changes will be lost.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            loadProfileData();
            clearPasswordFields();
            showAlert("Cancelled", "All changes have been discarded.", Alert.AlertType.INFORMATION);
        }
    }

    private boolean validateFields() {
        if (fullNameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Full name is required.", Alert.AlertType.ERROR);
            return false;
        }

        if (emailField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Email is required.", Alert.AlertType.ERROR);
            return false;
        }

        if (!oldPassField.getText().isEmpty() || !newPassField.getText().isEmpty() || !confirmPassField.getText().isEmpty()) {
            if (oldPassField.getText().isEmpty() || newPassField.getText().isEmpty() || confirmPassField.getText().isEmpty()) {
                showAlert("Validation Error", "All password fields must be filled if changing password.", Alert.AlertType.ERROR);
                return false;
            }

            if (!newPassField.getText().equals(confirmPassField.getText())) {
                showAlert("Validation Error", "New password and confirmation do not match.", Alert.AlertType.ERROR);
                return false;
            }

            if (newPassField.getText().length() < 6) {
                showAlert("Validation Error", "New password must be at least 6 characters.", Alert.AlertType.ERROR);
                return false;
            }

            // تحقق من الباسورد القديم (نص عادي)
            if (!oldPassField.getText().equals(currentUser.getPasswordHash())) {
                showAlert("Validation Error", "Old password is incorrect.", Alert.AlertType.ERROR);
                return false;
            }
        }

        return true;
    }

    private void clearPasswordFields() {
        oldPassField.clear();
        newPassField.clear();
        confirmPassField.clear();
    }

    @FXML
    private void handleBack() {
        loadScene("/View/Admin_dashboard.fxml");
    }

    private void loadScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load page.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}