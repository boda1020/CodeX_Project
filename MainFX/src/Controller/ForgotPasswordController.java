package Controller;

import Model.User;
import DAO.UserDAO;
import DAO.PasswordResetDAO;
import DAO.Impl.UserDAOImpl;
import DAO.Impl.PasswordResetDAOImpl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Random;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Button sendCodeButton;
    @FXML private Button closeButton;
    @FXML private Hyperlink backToLogin;

    private final UserDAO userDAO = new UserDAOImpl();
    private final PasswordResetDAO tokenDAO = new PasswordResetDAOImpl();

    @FXML
    private void initialize() {
        System.out.println("ForgotPasswordController initialized");

        if (sendCodeButton != null) {
            sendCodeButton.setOnAction(e -> handleSendCode());
        }

        if (closeButton != null) {
            closeButton.setOnAction(e -> handleClose());
        }

        if (backToLogin != null) {
            backToLogin.setOnAction(e -> handleBackToLogin());
        }

        emailField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !emailField.getText().isEmpty()) {
                validateEmail();
            }
        });
    }

    @FXML
    private void handleSendCode() {
        System.out.println("Send Code button clicked");

        String email = emailField.getText().trim();

        if (!validateEmailInput(email)) {
            return;
        }

        // التحقق إن الإيميل موجود في الداتابيز
        User user = userDAO.findByEmail(email);
        if (user == null) {
            showError("Email not found in the system!", emailField);
            return;
        }

        // توليد كود عشوائي 6 أرقام
        String verificationCode = generateVerificationCode();

        // حفظ الكود في الداتابيز
        if (tokenDAO.createToken(user.getUserId(), verificationCode)) {
            System.out.println("Verification code for " + email + ": " + verificationCode);

            showAlert("Code Sent",
                    "A verification code has been sent to:\n" +
                            email + "\n\n" +
                            "Code: " + verificationCode + "\n\n" +
                            "Note: In production, this code would be sent via email.\n" +
                            "The code is valid for 15 minutes.",
                    Alert.AlertType.INFORMATION);

            // فتح صفحة التحقق
            loadScene("Verify_code.fxml");
        } else {
            showAlert("Error", "Failed to generate code. Please try again.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleClose() {
        System.out.println("Close button clicked");
        loadScene("Login.fxml");
    }

    @FXML
    private void handleBackToLogin() {
        System.out.println("Back to Login clicked");
        loadScene("Login.fxml");
    }

    private boolean validateEmailInput(String email) {
        if (email.isEmpty()) {
            showError("Email is required!", emailField);
            return false;
        }

        if (!isValidEmail(email)) {
            showError("Please enter a valid email address!", emailField);
            return false;
        }

        return true;
    }

    private void validateEmail() {
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !isValidEmail(email)) {
            emailField.setStyle("-fx-border-color: #ff4444; -fx-border-radius: 5px;");
        } else {
            emailField.setStyle("");
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private void showError(String message, Control field) {
        field.setStyle("-fx-border-color: #ff4444; -fx-border-radius: 5px;");

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
            System.out.println("Loading: " + fxmlFile);

            String path = "/View/" + fxmlFile;
            Parent root = FXMLLoader.load(getClass().getResource(path));

            Stage currentStage = null;

            if (sendCodeButton != null && sendCodeButton.getScene() != null) {
                currentStage = (Stage) sendCodeButton.getScene().getWindow();
            } else if (closeButton != null && closeButton.getScene() != null) {
                currentStage = (Stage) closeButton.getScene().getWindow();
            } else if (emailField != null && emailField.getScene() != null) {
                currentStage = (Stage) emailField.getScene().getWindow();
            }

            if (currentStage != null) {
                Scene scene = new Scene(root);
                currentStage.setScene(scene);
                currentStage.centerOnScreen();
                currentStage.show();
                System.out.println("Scene loaded successfully!");
            } else {
                throw new Exception("Could not get current stage");
            }

        } catch (NullPointerException e) {
            showAlert("File Not Found", "Cannot find: " + fxmlFile, Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}