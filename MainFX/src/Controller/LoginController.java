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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ToggleButton studentToggle;
    @FXML private ToggleButton adminToggle;
    @FXML private Button loginButton;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private HBox adminRegisterBox;          // جديد
    @FXML private Hyperlink adminRegisterLink;    // جديد

    private String userType = "Student";

    private final UserDAO userDAO = new UserDAOImpl();

    @FXML
    private void initialize() {
        System.out.println("=== LoginController Initialization ===");

        ToggleGroup toggleGroup = new ToggleGroup();
        studentToggle.setToggleGroup(toggleGroup);
        adminToggle.setToggleGroup(toggleGroup);
        studentToggle.setSelected(true);
        updateToggleStyles(true);

        toggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == studentToggle) {
                userType = "Student";
                updateToggleStyles(true);
                adminRegisterBox.setVisible(false);
                adminRegisterBox.setManaged(false);
            } else if (newToggle == adminToggle) {
                userType = "Administrator";
                updateToggleStyles(false);
                adminRegisterBox.setVisible(true);
                adminRegisterBox.setManaged(true);
            }
        });

        if (loginButton != null) {
            loginButton.setOnAction(e -> handleLogin());
        }

        if (forgotPasswordLink != null) {
            forgotPasswordLink.setOnAction(e -> handleForgotPassword());
        }

        if (adminRegisterLink != null) {
            adminRegisterLink.setOnAction(e -> loadScene("RegisterAdmins.fxml")); // صفحة تسجيل أدمن
        }
    }

    private void updateToggleStyles(boolean isStudentSelected) {
        String selectedStyle = "-fx-background-color: #262451; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 9 18; -fx-font-size: 12;";
        String unselectedStyle = "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 9 18; -fx-font-size: 12;";

        if (isStudentSelected) {
            studentToggle.setStyle(selectedStyle);
            adminToggle.setStyle(unselectedStyle);
        } else {
            adminToggle.setStyle(selectedStyle);
            studentToggle.setStyle(unselectedStyle);
        }

        loginButton.setStyle("-fx-background-color: #262451; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 10; -fx-padding: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(38,36,81,0.1), 5, 0, 0, 2);");
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill in all fields!", Alert.AlertType.ERROR);
            return;
        }

        System.out.println("Login attempt - Username/Email: " + username + ", Type: " + userType);

        User user = userDAO.login(username, password);

        if (user == null) {
            showAlert("Login Failed", "Invalid username/email or password.", Alert.AlertType.ERROR);
            return;
        }

        String userRoleFromDB = user.getRole().trim().toLowerCase();
        String selectedRole = userType.equals("Student") ? "student" : "admin";

        if (!userRoleFromDB.equals(selectedRole)) {
            showAlert("Access Denied", "You are not authorized as " + userType + ".", Alert.AlertType.ERROR);
            return;
        }

        CurrentUser.setCurrentUser(user);

        showAlert("Success", "Welcome back, " + user.getFullName() + "!", Alert.AlertType.INFORMATION);

        String dashboard = userRoleFromDB.equals("admin") ? "Admin_dashboard.fxml" : "Student_dashboard.fxml";
        loadScene(dashboard);
    }

    private void handleForgotPassword() {
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
            System.out.println("\n=== Attempting to load FXML: " + fxmlFile + " ===");

            String path = "/View/" + fxmlFile;
            URL resourceUrl = getClass().getResource(path);

            if (resourceUrl == null) {
                System.err.println("ERROR: File not found at path: " + path);
                showAlert("File Not Found", "Cannot find file: " + fxmlFile, Alert.AlertType.ERROR);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            Stage currentStage = (Stage) loginButton.getScene().getWindow();
            Scene newScene = new Scene(root);

            currentStage.setScene(newScene);
            currentStage.setTitle(fxmlFile.replace(".fxml", ""));
            currentStage.centerOnScreen();
            currentStage.show();

            System.out.println("Scene switched successfully to " + fxmlFile + "\n");

        } catch (Exception e) {
            System.err.println("ERROR loading scene: " + fxmlFile);
            e.printStackTrace();
            showAlert("Load Error", "Failed to load: " + fxmlFile + "\nError: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}