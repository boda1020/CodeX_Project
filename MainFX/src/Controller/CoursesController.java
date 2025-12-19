package Controller;

import Model.Course;
import DAO.CourseDAO;
import DAO.Impl.CourseDAOImpl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class CoursesController {

    @FXML private Button btnBack;
    @FXML private Button btnChooseFile;
    @FXML private Button btnAdd;
    @FXML private Button btnClear;

    @FXML private TextField txtCourseId;
    @FXML private TextField txtCourseCode;
    @FXML private TextField txtCourseName;
    @FXML private TextField txtCreditHours;
    @FXML private TextField txtDepartment;
    @FXML private TextField txtInstructorName;
    @FXML private TextField txtRoom;
    @FXML private TextField txtSchedule;
    @FXML private ComboBox<String> cmbLevel;
    @FXML private Label lblFilePath;

    private final CourseDAO courseDAO = new CourseDAOImpl();
    private File selectedFile = null;

    @FXML
    private void initialize() {
        System.out.println("CoursesController initialized");

        try {
            // Initialize combo box
            if (cmbLevel != null) {
                cmbLevel.getItems().addAll("Level 1", "Level 2", "Level 3", "Level 4");
                cmbLevel.setValue("Level 3");
            }

            // Initialize file label
            if (lblFilePath != null) {
                lblFilePath.setText("No file selected");
            }

            // Set button actions manually
            setupButtonActions();

            System.out.println("CoursesController setup completed");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Initialization Error", "Failed to initialize CoursesController: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupButtonActions() {
        // Set up button actions manually
        if (btnAdd != null) {
            btnAdd.setOnAction(event -> handleAdd());
        }
        if (btnClear != null) {
            btnClear.setOnAction(event -> handleClear());
        }
        if (btnChooseFile != null) {
            btnChooseFile.setOnAction(event -> handleChooseFile());
        }
        if (btnBack != null) {
            btnBack.setOnAction(event -> handleBack());
        }
    }

    @FXML
    private void handleBack() {
        loadScene("Admin_dashboard.fxml");
    }

    @FXML
    private void handleChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Course Material");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Word Documents", "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(btnChooseFile.getScene().getWindow());
        if (file != null) {
            selectedFile = file;
            System.out.println("Selected file: " + file.getName());

            // Update the label with file name
            if (lblFilePath != null) {
                lblFilePath.setText(file.getName());
            }
        }
    }

    @FXML
    private void handleAdd() {
        System.out.println("Add button clicked!");

        if (!validateCourseForm()) {
            System.out.println("Validation failed");
            return;
        }

        Course course = createCourseFromInputs();
        if (course == null) {
            System.out.println("Failed to create course from inputs");
            return;
        }

        System.out.println("=== ADDING COURSE ===");
        System.out.println("Course Code: " + course.getCourseCode());
        System.out.println("Course Name: " + course.getCourseName());
        System.out.println("Credits: " + course.getCredits());
        System.out.println("Department: " + course.getDepartment());

        // Handle file upload if selected
        if (selectedFile != null) {
            String filePath = uploadFile(selectedFile);
            if (filePath != null) {
                course.setPdfPath(filePath);
                System.out.println("File saved to: " + filePath);
            }
        }

        boolean success = courseDAO.addCourse(course);
        System.out.println("Add course result: " + success);

        if (success) {
            showAlert("Success", "Course '" + course.getCourseName() + "' added successfully!", Alert.AlertType.INFORMATION);

            // Show debug info
            showDatabaseDebug();

            clearFields();
        } else {
            showAlert("Error", "Failed to add course. Course code might already exist.", Alert.AlertType.ERROR);
            System.out.println("=== COURSE ADD FAILED ===");
        }
    }

    @FXML
    private void handleClear() {
        System.out.println("Clear button clicked!");
        clearFields();
    }

    private void clearFields() {
        if (txtCourseId != null) txtCourseId.clear();
        if (txtCourseCode != null) txtCourseCode.clear();
        if (txtCourseName != null) txtCourseName.clear();
        if (txtCreditHours != null) txtCreditHours.clear();
        if (cmbLevel != null) cmbLevel.setValue("Level 1");
        if (txtDepartment != null) txtDepartment.clear();
        if (txtInstructorName != null) txtInstructorName.clear();
        if (txtRoom != null) txtRoom.clear();
        if (txtSchedule != null) txtSchedule.clear();
        selectedFile = null;

        // Reset file label
        if (lblFilePath != null) {
            lblFilePath.setText("No file selected");
        }

        System.out.println("Fields cleared");
    }

    private Course createCourseFromInputs() {
        try {
            Course course = new Course();
            if (txtCourseCode != null) {
                String courseCode = txtCourseCode.getText().trim();
                if (courseCode.isEmpty()) {
                    showAlert("Error", "Course Code is required!", Alert.AlertType.ERROR);
                    return null;
                }
                course.setCourseCode(courseCode);
            }

            if (txtCourseName != null) {
                String courseName = txtCourseName.getText().trim();
                if (courseName.isEmpty()) {
                    showAlert("Error", "Course Name is required!", Alert.AlertType.ERROR);
                    return null;
                }
                course.setCourseName(courseName);
            }

            if (txtCreditHours != null && !txtCreditHours.getText().trim().isEmpty()) {
                course.setCredits(Integer.parseInt(txtCreditHours.getText().trim()));
            } else {
                course.setCredits(3);
            }

            if (cmbLevel != null) course.setLevel(cmbLevel.getValue());
            if (txtDepartment != null) course.setDepartment(txtDepartment.getText().trim());
            if (txtInstructorName != null) course.setInstructorName(txtInstructorName.getText().trim());
            if (txtRoom != null) course.setRoom(txtRoom.getText().trim());
            if (txtSchedule != null) course.setSchedule(txtSchedule.getText().trim());
            course.setStatus("Active");
            return course;
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Invalid credits value. Must be a number.", Alert.AlertType.ERROR);
            return null;
        }
    }

    private boolean validateCourseForm() {
        if (txtCourseCode == null || txtCourseCode.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Course Code is required!", Alert.AlertType.ERROR);
            return false;
        }
        if (txtCourseName == null || txtCourseName.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Course Name is required!", Alert.AlertType.ERROR);
            return false;
        }
        try {
            if (txtCreditHours == null || txtCreditHours.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Credit Hours is required!", Alert.AlertType.ERROR);
                return false;
            }
            int credits = Integer.parseInt(txtCreditHours.getText().trim());
            if (credits <= 0 || credits > 6) {
                showAlert("Validation Error", "Credits must be between 1 and 6!", Alert.AlertType.ERROR);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Credits must be a number!", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private void showDatabaseDebug() {
        try {
            System.out.println("=== DEBUG DATABASE INFO ===");

            // Check courses table
            List<Course> allCourses = courseDAO.getAllCourses();
            System.out.println("Total courses in database: " + allCourses.size());

            for (Course c : allCourses) {
                System.out.println("Course: " + c.getCourseCode() + " - " + c.getCourseName() +
                        " - Credits: " + c.getCredits() + " - ID: " + c.getCourseId());
            }

            System.out.println("=== END DEBUG ===");
        } catch (Exception e) {
            System.out.println("Debug error: " + e.getMessage());
        }
    }

    private String uploadFile(File file) {
        try {
            // Create uploads directory if it doesn't exist
            File uploadsDir = new File("uploads/courses");
            if (!uploadsDir.exists()) {
                boolean created = uploadsDir.mkdirs();
                System.out.println("Created uploads directory: " + created);
            }

            // Generate unique filename
            String fileName = System.currentTimeMillis() + "_" + file.getName();
            File destFile = new File(uploadsDir, fileName);

            // Copy file
            Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            System.out.println("File uploaded to: " + destFile.getAbsolutePath());
            return destFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("File Upload Error", "Failed to upload file: " + e.getMessage(), Alert.AlertType.ERROR);
            return null;
        }
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
            Parent root = FXMLLoader.load(getClass().getResource("/View/" + fxmlFile));
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load page: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}