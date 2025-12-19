package Controller;

import Model.Student;
import Model.User;
import DAO.StudentDAO;
import DAO.UserDAO;
import DAO.Impl.StudentDAOImpl;
import DAO.Impl.UserDAOImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentManagementController {

    @FXML private Button btnBack;
    @FXML private TextField searchField;
    @FXML private Button btnSearch;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Button btnRefresh;
    @FXML private Button btnAddStudent;
    @FXML private Button btnEditStudent;
    @FXML private Button btnDeleteStudent;
    @FXML private Button btnExport;
    @FXML private TableView<Student> studentsTable;
    @FXML private TableColumn<Student, String> colStudentId;
    @FXML private TableColumn<Student, String> colFullName;
    @FXML private TableColumn<Student, String> colEmail;
    @FXML private TableColumn<Student, String> colPhone;
    @FXML private TableColumn<Student, String> colLevel;
    @FXML private TableColumn<Student, String> colDepartment;
    @FXML private TableColumn<Student, Double> colGPA;
    @FXML private TableColumn<Student, String> colStatus;
    @FXML private Label lblTotalStudents;

    private final StudentDAO studentDAO = new StudentDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();

    private ObservableList<Student> studentList;
    private ObservableList<Student> filteredList;

    @FXML
    private void initialize() {
        setupTableColumns();
        setupFilterComboBox();
        setupButtonActions();
        loadStudentsFromDatabase();
        setupTableSelection();
    }

    private void setupTableColumns() {
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("studentCode"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colLevel.setCellValueFactory(new PropertyValueFactory<>("level"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));
        colGPA.setCellValueFactory(new PropertyValueFactory<>("gpa"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colGPA.setCellFactory(column -> new TableCell<Student, Double>() {
            @Override
            protected void updateItem(Double gpa, boolean empty) {
                super.updateItem(gpa, empty);
                if (empty || gpa == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", gpa));
                }
            }
        });
    }

    private void setupFilterComboBox() {
        filterComboBox.getItems().addAll("All Levels", "Level 1", "Level 2", "Level 3", "Level 4");
        filterComboBox.setValue("All Levels");
        filterComboBox.valueProperty().addListener((obs, old, newVal) -> applyFilter());
    }

    private void setupButtonActions() {
        btnBack.setOnAction(e -> loadScene("Admin_dashboard.fxml"));
        btnSearch.setOnAction(e -> handleSearch());
        btnRefresh.setOnAction(e -> loadStudentsFromDatabase());
        btnAddStudent.setOnAction(e -> handleAddStudent());
        btnEditStudent.setOnAction(e -> handleEditStudent());
        btnDeleteStudent.setOnAction(e -> handleDeleteStudent());
        btnExport.setOnAction(e -> handleExport());
    }

    private void setupTableSelection() {
        studentsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            boolean isSelected = newVal != null;
            btnEditStudent.setDisable(!isSelected);
            btnDeleteStudent.setDisable(!isSelected);
        });
        btnEditStudent.setDisable(true);
        btnDeleteStudent.setDisable(true);
    }

    private void loadStudentsFromDatabase() {
        studentList = FXCollections.observableArrayList(studentDAO.getAllStudents());
        filteredList = FXCollections.observableArrayList(studentList);
        studentsTable.setItems(filteredList);
        updateStatistics();
    }

    private void applyFilter() {
        String selectedLevel = filterComboBox.getValue();
        if (selectedLevel == null || selectedLevel.equals("All Levels")) {
            filteredList.setAll(studentList);
        } else {
            filteredList.setAll(studentList.filtered(s -> s.getLevel() != null && s.getLevel().equals(selectedLevel)));
        }
        studentsTable.setItems(filteredList);
        updateStatistics();
    }

    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            filteredList.setAll(studentList);
        } else {
            filteredList.setAll(studentDAO.searchStudents(query));
        }
        studentsTable.setItems(filteredList);
        updateStatistics();
    }

    // التعديل الرئيسي: إضافة طالب جديد مع باسورد افتراضي
    private void handleAddStudent() {
        Dialog<Student> dialog = createStudentDialog(null, "Add New Student");
        Optional<Student> result = dialog.showAndWait();

        if (result.isPresent()) {
            Student newStudent = result.get();

            // الرقم الأكاديمي هو username وجزء من الإيميل
            String academicId = newStudent.getStudentCode().trim();
            String username = academicId;
            String email = academicId + "@codex.com";
            String defaultPassword = "12345678";

            // إنشاء حساب اليوزر أولاً
            User user = new User();
            user.setUsername(username);
            user.setFullName(newStudent.getFullName());
            user.setEmail(email);
            user.setPasswordHash(defaultPassword);  // نص عادي
            user.setRole("Student");
            user.setPhone(newStudent.getPhone());

            if (!userDAO.register(user)) {
                showAlert("Error", "Failed to create account. Username or email may already exist.", Alert.AlertType.ERROR);
                return;
            }

            // جلب الـ user الجديد عشان نأخد user_id
            User createdUser = userDAO.findByEmail(email);
            if (createdUser == null) {
                showAlert("Error", "Account created but not found in database.", Alert.AlertType.ERROR);
                return;
            }

            // إضافة بيانات الطالب في جدول students
            newStudent.setStudentId(createdUser.getUserId());

            if (studentDAO.addStudent(newStudent)) {
                loadStudentsFromDatabase();

                // رسالة نجاح مع بيانات اللوجن
                String message = "Student added successfully!\n\n" +
                        "Login Details for the Student:\n" +
                        "Username: " + username + "\n" +
                        "Email: " + email + "\n" +
                        "Default Password: " + defaultPassword + "\n\n" +
                        "The student must change the password after first login.";

                showAlert("Success", message, Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to save student details.", Alert.AlertType.ERROR);
            }
        }
    }

    private void handleEditStudent() {
        Student selected = studentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a student to edit.", Alert.AlertType.WARNING);
            return;
        }

        Dialog<Student> dialog = createStudentDialog(selected, "Edit Student");
        dialog.showAndWait().ifPresent(updatedStudent -> {
            if (studentDAO.updateStudent(updatedStudent)) {
                loadStudentsFromDatabase();
                showAlert("Success", "Student updated successfully!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to update student.", Alert.AlertType.ERROR);
            }
        });
    }

    private void handleDeleteStudent() {
        Student selected = studentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a student to delete.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Student");
        confirm.setContentText("Are you sure you want to delete " + selected.getFullName() + "? This cannot be undone.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (studentDAO.deleteStudent(selected.getStudentId())) {
                loadStudentsFromDatabase();
                showAlert("Success", "Student deleted successfully!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to delete student.", Alert.AlertType.ERROR);
            }
        }
    }

    // إنشاء دالة createStudentDialog المفقودة
    private Dialog<Student> createStudentDialog(Student student, String title) {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText("Enter student details:");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField studentCodeField = new TextField();
        TextField fullNameField = new TextField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();
        ComboBox<String> levelComboBox = new ComboBox<>();
        TextField departmentField = new TextField();
        TextField gpaField = new TextField();
        ComboBox<String> statusComboBox = new ComboBox<>();

        levelComboBox.getItems().addAll("Level 1", "Level 2", "Level 3", "Level 4");
        statusComboBox.getItems().addAll("Active", "Inactive", "Graduated", "Suspended");

        if (student != null) {
            studentCodeField.setText(student.getStudentCode());
            fullNameField.setText(student.getFullName());
            emailField.setText(student.getEmail());
            phoneField.setText(student.getPhone());
            levelComboBox.setValue(student.getLevel());
            departmentField.setText(student.getDepartment());
            gpaField.setText(String.valueOf(student.getGpa()));
            statusComboBox.setValue(student.getStatus());
        }

        grid.add(new Label("Student Code:"), 0, 0);
        grid.add(studentCodeField, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(fullNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Level:"), 0, 4);
        grid.add(levelComboBox, 1, 4);
        grid.add(new Label("Department:"), 0, 5);
        grid.add(departmentField, 1, 5);
        grid.add(new Label("GPA:"), 0, 6);
        grid.add(gpaField, 1, 6);
        grid.add(new Label("Status:"), 0, 7);
        grid.add(statusComboBox, 1, 7);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Student resultStudent = new Student();
                resultStudent.setStudentCode(studentCodeField.getText());
                resultStudent.setFullName(fullNameField.getText());
                resultStudent.setEmail(emailField.getText());
                resultStudent.setPhone(phoneField.getText());
                resultStudent.setLevel(levelComboBox.getValue());
                resultStudent.setDepartment(departmentField.getText());

                try {
                    resultStudent.setGpa(Double.parseDouble(gpaField.getText()));
                } catch (NumberFormatException e) {
                    resultStudent.setGpa(0.0);
                }

                resultStudent.setStatus(statusComboBox.getValue());

                if (student != null) {
                    resultStudent.setStudentId(student.getStudentId());
                }

                return resultStudent;
            }
            return null;
        });

        return dialog;
    }

    // في handleExport() بدل الكلام الفاضي
    @FXML
    private void handleExport() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("CSV", "PDF", "CSV");
        dialog.setTitle("Export Students");
        dialog.setHeaderText("Select export format");
        dialog.setContentText("Format:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(format -> {
            // Get current filtered students
            List<Student> studentsToExport = new ArrayList<>(filteredList);

            if (studentsToExport.isEmpty()) {
                showAlert("Error", "No students to export.", Alert.AlertType.ERROR);
                return;
            }

            // Ask for save location
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Students File");

            if (format.equals("CSV")) {
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("CSV Files", "*.csv")
                );
                fileChooser.setInitialFileName("students_" + LocalDate.now() + ".csv");
            } else if (format.equals("PDF")) {
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
                );
                fileChooser.setInitialFileName("students_" + LocalDate.now() + ".pdf");
            }

            File file = fileChooser.showSaveDialog(btnExport.getScene().getWindow());
            if (file != null) {
                exportStudentsToFile(studentsToExport, file, format);
            }
        });
    }

    private void exportStudentsToFile(List<Student> students, File file, String format) {
        try {
            switch (format) {
                case "PDF":
                    exportToPDF(students, file);
                    break;
                case "CSV":
                    exportToCSV(students, file);
                    break;
                default:
                    exportToCSV(students, file);
                    break;
            }
            showAlert("Success", "Students exported successfully to:\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to export file: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void exportToCSV(List<Student> students, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(file)) {
            // Write header
            writer.println("Student Code,Full Name,Email,Phone,Level,Department,GPA,Status");

            // Write data
            for (Student student : students) {
                writer.printf("%s,\"%s\",%s,%s,%s,%s,%.2f,%s%n",
                        student.getStudentCode(),
                        student.getFullName(),
                        student.getEmail(),
                        student.getPhone(),
                        student.getLevel(),
                        student.getDepartment(),
                        student.getGpa(),
                        student.getStatus());
            }
        }
    }

    private void exportToPDF(List<Student> students, File file) throws IOException {
        // Create a simple text file as PDF (simplified version)
        // Note: For real PDF creation, you need a library like iText
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("STUDENTS LIST");
            writer.println("Generated: " + LocalDate.now());
            writer.println("=".repeat(50));
            writer.println();

            for (Student student : students) {
                writer.printf("Student Code: %s%n", student.getStudentCode());
                writer.printf("Full Name: %s%n", student.getFullName());
                writer.printf("Email: %s%n", student.getEmail());
                writer.printf("Phone: %s%n", student.getPhone());
                writer.printf("Level: %s%n", student.getLevel());
                writer.printf("Department: %s%n", student.getDepartment());
                writer.printf("GPA: %.2f%n", student.getGpa());
                writer.printf("Status: %s%n", student.getStatus());
                writer.println("-".repeat(30));
            }
        }
    }

    private void updateStatistics() {
        lblTotalStudents.setText("Total Students: " + filteredList.size());
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