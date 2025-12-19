package Controller;

import Model.User;
import DAO.UserDAO;
import DAO.Impl.UserDAOImpl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterAdminsController {

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField departmentField;
    @FXML private TextField adminCodeField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField phoneField;
    @FXML private Button registerBtn;
    @FXML private Hyperlink loginLink;

    private final UserDAO userDAO = new UserDAOImpl();

    private static final String ADMIN_ACCESS_CODE = "ADMIN123";

    @FXML
    private void initialize() {
        System.out.println("RegisterAdminsController initialized");

        if (registerBtn != null) {
            registerBtn.setOnAction(e -> handleRegister());
        } else {
            System.err.println("ERROR: registerBtn is null!");
        }

        if (loginLink != null) {
            loginLink.setOnAction(e -> handleLoginLink());
        } else {
            System.err.println("ERROR: loginLink is null!");
        }
    }

    @FXML
    private void handleRegister() {
        System.out.println("Register button clicked");

        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String department = departmentField.getText().trim();
        String adminCode = adminCodeField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String phone = phoneField.getText().trim();

        // Validation for empty fields
        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || department.isEmpty() ||
                adminCode.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty()) {
            showAlert("Error", "Please fill in all fields!", Alert.AlertType.ERROR);
            return;
        }

        // Check passwords match
        if (!password.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match!", Alert.AlertType.ERROR);
            return;
        }

        // Check admin access code
        if (!ADMIN_ACCESS_CODE.equals(adminCode)) {
            showAlert("Error", "Invalid Admin Access Code!\nUse: ADMIN123", Alert.AlertType.ERROR);
            return;
        }

        // Check email format
        if (!email.contains("@")) {
            showAlert("Error", "Please enter a valid email address!", Alert.AlertType.ERROR);
            return;
        }

        // Check for duplicates in database
        if (userDAO.usernameExists(username)) {
            showAlert("Error", "Username already exists!", Alert.AlertType.ERROR);
            return;
        }

        if (userDAO.emailExists(email)) {
            showAlert("Error", "Email already exists!", Alert.AlertType.ERROR);
            return;
        }

        // Create admin user
        User admin = new User();
        admin.setFullName(fullName);
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPasswordHash(password); // Will be hashed in DAO
        admin.setRole("ADMIN");
        admin.setPhone(phone);

        // Register in database
        if (userDAO.register(admin)) {
            showAlert("Success", "Admin Registered Successfully!\nWelcome " + fullName + "!", Alert.AlertType.INFORMATION);
            loadScene("Login.fxml");
        } else {
            showAlert("Error", "Registration failed. Please try again.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLoginLink() {
        System.out.println("Login link clicked");
        loadScene("Login.fxml");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        try {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error showing alert: " + e.getMessage());
        }
    }

    private void loadScene(String fxmlFile) {
        try {
            System.out.println("Loading scene: " + fxmlFile);

            String[] possiblePaths = {
                    "/View/" + fxmlFile,
                    "View/" + fxmlFile,
                    "../resources.View/" + fxmlFile
            };

            Parent root = null;

            for (String path : possiblePaths) {
                try {
                    System.out.println("Trying path: " + path);
                    root = FXMLLoader.load(getClass().getResource(path));
                    if (root != null) {
                        System.out.println("Successfully loaded from: " + path);
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Failed to load from " + path + ": " + e.getMessage());
                }
            }

            if (root == null) {
                throw new Exception("Could not load FXML from any path");
            }

            Stage currentStage = null;

            if (registerBtn != null && registerBtn.getScene() != null) {
                currentStage = (Stage) registerBtn.getScene().getWindow();
            } else if (loginLink != null && loginLink.getScene() != null) {
                currentStage = (Stage) loginLink.getScene().getWindow();
            } else if (fullNameField != null && fullNameField.getScene() != null) {
                currentStage = (Stage) fullNameField.getScene().getWindow();
            }

            if (currentStage == null) {
                currentStage = new Stage();
            }

            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Login");
            currentStage.centerOnScreen();
            currentStage.show();

            System.out.println("Scene loaded successfully!");

        } catch (Exception e) {
            System.err.println("ERROR in loadScene: " + e.getMessage());
            e.printStackTrace();

            showAlert("Load Error",
                    "Could not load: " + fxmlFile +
                            "\n\nError: " + e.getMessage() +
                            "\n\nMake sure:\n" +
                            "1. File " + fxmlFile + " exists in resources.View folder\n" +
                            "2. File has .fxml extension\n" +
                            "3. FXML file has correct controller reference",
                    Alert.AlertType.ERROR);
        }
    }
}