package Controller;

import Model.PasswordResetToken;
import DAO.PasswordResetDAO;
import DAO.Impl.PasswordResetDAOImpl;
import Utils.CurrentUserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class VerifyCodeController {

    @FXML private TextField codeInput;
    @FXML private Label timerLabel;
    @FXML private Button resendButton;
    @FXML private Button confirmButton;
    @FXML private Button closeButton;

    private final PasswordResetDAO tokenDAO = new PasswordResetDAOImpl();

    private int countdown = 30;
    private Timeline timer;

    @FXML
    private void initialize() {
        System.out.println("VerifyCodeController initialized");

        if (closeButton != null) {
            closeButton.setOnAction(e -> handleClose());
        }

        if (confirmButton != null) {
            confirmButton.setOnAction(e -> handleConfirm());
        }

        if (resendButton != null) {
            resendButton.setOnAction(e -> handleResend());
        }

        startTimer();
    }

    private void startTimer() {
        resendButton.setDisable(true);
        countdown = 30;

        timer = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    countdown--;
                    updateTimerLabel();

                    if (countdown <= 0) {
                        timer.stop();
                        resendButton.setDisable(false);
                        timerLabel.setText("You can now resend the code.");
                    }
                })
        );

        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void updateTimerLabel() {
        if (countdown > 0) {
            timerLabel.setText("Resend available in " + countdown + " seconds.");
        }
    }

    @FXML
    private void handleClose() {
        System.out.println("Close button clicked");
        loadScene("Login.fxml");
    }

    @FXML
    private void handleConfirm() {
        if (codeInput == null) return;

        String enteredCode = codeInput.getText().trim();

        if (enteredCode.isEmpty()) {
            showAlert("Error", "Please enter the verification code!", Alert.AlertType.ERROR);
            return;
        }

        if (enteredCode.length() != 6) {
            showAlert("Error", "Code must be 6 digits!", Alert.AlertType.ERROR);
            return;
        }

        // التحقق من الكود في الداتابيز
        PasswordResetToken token = tokenDAO.getTokenByCode(enteredCode);

        if (token == null) {
            showAlert("Error", "Invalid or expired verification code!\n\nPlease check the code or request a new one.", Alert.AlertType.ERROR);
            codeInput.clear();
            codeInput.requestFocus();
            return;
        }

        // لو الكود صح ومش مستخدم
        if (!token.isUsed() && !token.isExpired()) {
            // علم الكود كـ مستخدم
            tokenDAO.markTokenAsUsed(token.getTokenId());

            CurrentUserSession.setUserIdForPasswordReset(token.getUserId());

            showAlert("Success", "Verification successful!\n\nYou can now reset your password.", Alert.AlertType.INFORMATION);
            loadScene("Reset_Password.fxml");
        } else {
            showAlert("Error", "This code has already been used or has expired.", Alert.AlertType.ERROR);
            codeInput.clear();
            codeInput.requestFocus();
        }
    }

    @FXML
    private void handleResend() {
        if (countdown > 0) {
            showAlert("Wait", "Please wait " + countdown + " seconds before resending.", Alert.AlertType.WARNING);
            return;
        }

        // في الإنتاج: هنا هنرجع لصفحة Forgot Password ونطلب الإيميل تاني
        // مؤقتًا: هنرجع لـ Forgot Password
        showAlert("Info", "Returning to email entry to resend code.", Alert.AlertType.INFORMATION);
        loadScene("Forgot_Password.fxml");
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

            Stage stage = null;
            if (closeButton != null && closeButton.getScene() != null) {
                stage = (Stage) closeButton.getScene().getWindow();
            } else if (confirmButton != null && confirmButton.getScene() != null) {
                stage = (Stage) confirmButton.getScene().getWindow();
            }

            if (stage != null) {
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.centerOnScreen();
                stage.show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load: " + fxmlFile, Alert.AlertType.ERROR);
        }
    }
}