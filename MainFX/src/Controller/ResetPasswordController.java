package Controller;

import Utils.CurrentUserSession;
import DAO.UserDAO;
import DAO.Impl.UserDAOImpl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ResetPasswordController {

    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button savePasswordButton;
    @FXML private Button closeButton;
    @FXML private Hyperlink backToLogin;

    private final UserDAO userDAO = new UserDAOImpl();

    @FXML
    private void initialize() {
        System.out.println("ResetPasswordController initialized");

        if (savePasswordButton != null) {
            savePasswordButton.setOnAction(e -> handleSavePassword());
        }

        if (closeButton != null) {
            closeButton.setOnAction(e -> handleClose());
        }

        if (backToLogin != null) {
            backToLogin.setOnAction(e -> handleBackToLogin());
        }

        setupPasswordValidation();
    }

    private void setupPasswordValidation() {
        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            checkPasswordStrength(newVal);
            checkPasswordsMatch();
        });

        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            checkPasswordsMatch();
        });
    }

    private void checkPasswordStrength(String password) {
        if (password.isEmpty()) {
            newPasswordField.setStyle("");
            return;
        }

        boolean hasMinLength = password.length() >= 8;
        boolean hasLetter = password.matches(".*[A-Za-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        int strength = 0;
        if (hasMinLength) strength++;
        if (hasLetter) strength++;
        if (hasDigit) strength++;
        if (hasSpecial) strength++;

        String borderColor;
        if (strength >= 4) {
            borderColor = "#059669"; // Green - Strong
        } else if (strength >= 3) {
            borderColor = "#D97706"; // Orange - Medium
        } else if (strength >= 2) {
            borderColor = "#DC2626"; // Red - Weak
        } else {
            borderColor = "#DC2626"; // Red - Very Weak
        }

        newPasswordField.setStyle("-fx-border-color: " + borderColor + "; -fx-border-radius: 5; -fx-border-width: 2;");
    }

    private void checkPasswordsMatch() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (confirmPassword.isEmpty()) {
            confirmPasswordField.setStyle("");
            return;
        }

        if (newPassword.equals(confirmPassword)) {
            confirmPasswordField.setStyle("-fx-border-color: #059669; -fx-border-radius: 5; -fx-border-width: 2;");
        } else {
            confirmPasswordField.setStyle("-fx-border-color: #DC2626; -fx-border-radius: 5; -fx-border-width: 2;");
        }
    }

    @FXML
    private void handleSavePassword() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!validatePasswords(newPassword, confirmPassword)) {
            return;
        }

        int userId = CurrentUserSession.getUserIdForPasswordReset();

        if (userId == -1) {
            showAlert("Error", "Session expired or invalid. Please request a new code.", Alert.AlertType.ERROR);
            CurrentUserSession.clearPasswordReset();
            loadScene("Login.fxml");
            return;
        }

        // تحديث الباسورد في الداتابيز
        if (userDAO.updatePassword(userId, newPassword)) {
            CurrentUserSession.clearPasswordReset(); // تنظيف الجلسة

            showAlert("Success", "Your password has been reset successfully!\n\nYou can now login with your new password.", Alert.AlertType.INFORMATION);
            loadScene("Login.fxml");
        } else {
            showAlert("Error", "Failed to update password. Please try again.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleClose() {
        CurrentUserSession.clearPasswordReset();
        loadScene("Login.fxml");
    }

    @FXML
    private void handleBackToLogin() {
        CurrentUserSession.clearPasswordReset();
        loadScene("Login.fxml");
    }

    private boolean validatePasswords(String newPassword, String confirmPassword) {
        if (newPassword.isEmpty()) {
            showError("New password is required!", newPasswordField);
            return false;
        }

        if (confirmPassword.isEmpty()) {
            showError("Please confirm your password!", confirmPasswordField);
            return false;
        }

        if (!isStrongPassword(newPassword)) {
            showError("Password must be at least 8 characters long\nand contain letters and numbers!", newPasswordField);
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Passwords do not match!", confirmPasswordField);
            return false;
        }

        return true;
    }

    private boolean isStrongPassword(String password) {
        boolean hasMinLength = password.length() >= 8;
        boolean hasLetter = password.matches(".*[A-Za-z].*");
        boolean hasDigit = password.matches(".*\\d.*");

        return hasMinLength && hasLetter && hasDigit;
    }

    private void showError(String message, Control field) {
        field.setStyle("-fx-border-color: #DC2626; -fx-border-radius: 5; -fx-border-width: 2;");

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

        field.requestFocus();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadScene(String fxmlFile) {
        try {
            String path = "/View/" + fxmlFile;
            Parent root = FXMLLoader.load(getClass().getResource(path));

            Stage currentStage = null;

            if (closeButton != null && closeButton.getScene() != null) {
                currentStage = (Stage) closeButton.getScene().getWindow();
            } else if (savePasswordButton != null && savePasswordButton.getScene() != null) {
                currentStage = (Stage) savePasswordButton.getScene().getWindow();
            } else if (backToLogin != null && backToLogin.getScene() != null) {
                currentStage = (Stage) backToLogin.getScene().getWindow();
            }

            if (currentStage != null) {
                Scene scene = new Scene(root);
                currentStage.setScene(scene);
                currentStage.centerOnScreen();
                currentStage.show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}