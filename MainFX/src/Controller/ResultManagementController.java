package Controller;

import Model.Result;
import Model.Student;
import Model.Course;
import DAO.ResultDAO;
import DAO.EnrollmentDAO;
import DAO.StudentDAO;
import DAO.CourseDAO;
import DAO.Impl.ResultDAOImpl;
import DAO.Impl.EnrollmentDAOImpl;
import DAO.Impl.StudentDAOImpl;
import DAO.Impl.CourseDAOImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class ResultManagementController {

    @FXML private TableView<Result> courseTable;
    @FXML private TableColumn<Result, String> courseNameCol;
    @FXML private TableColumn<Result, Integer> creditsCol;
    @FXML private TableColumn<Result, String> gradeCol;
    @FXML private TableColumn<Result, Double> gpaCol;
    @FXML private TableColumn<Result, Integer> degreeCol;
    @FXML private TableColumn<Result, String> examDateCol;

    @FXML private Button backButton;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    @FXML private Label studentNameLabel;
    @FXML private Label studentIdLabel;
    @FXML private Label studentProgramLabel;

    @FXML private Label totalCoursesLabel;
    @FXML private Label averageGradeLabel;
    @FXML private Label currentGPALabel;

    @FXML private TextField studentCodeField;
    @FXML private Button searchStudentButton;

    private final ResultDAO resultDAO = new ResultDAOImpl();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAOImpl();
    private final StudentDAO studentDAO = new StudentDAOImpl();
    private final CourseDAO courseDAO = new CourseDAOImpl();

    private ObservableList<Result> results = FXCollections.observableArrayList();

    private int currentStudentId = 0;
    private Student currentStudent = null;

    @FXML
    private void initialize() {
        System.out.println("ResultManagementController initialized");
        setupTable();
        setupButtonStates();
        updateSummary();

        courseTable.setVisible(false);
        totalCoursesLabel.setText("0");
        averageGradeLabel.setText("N/A");
        currentGPALabel.setText("0.0");

        addButton.setDisable(true);
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void setupTable() {
        courseNameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));
        gpaCol.setCellValueFactory(new PropertyValueFactory<>("gpa"));
        degreeCol.setCellValueFactory(new PropertyValueFactory<>("degree"));
        examDateCol.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getExamDate();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.toString() : "N/A");
        });

        courseTable.setItems(results);

        courseTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            boolean hasSelection = newVal != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });
    }

    @FXML
    private void handleSearchStudent() {
        String studentCode = studentCodeField.getText().trim();

        if (studentCode.isEmpty()) {
            showAlert("Error", "Please enter student academic code!", Alert.AlertType.ERROR);
            return;
        }

        Student student = studentDAO.getStudentByCode(studentCode);
        if (student == null) {
            showAlert("Error", "Student not found with code: " + studentCode, Alert.AlertType.ERROR);
            return;
        }

        currentStudentId = student.getStudentId();
        currentStudent = student;
        loadResultsForStudent(currentStudentId);
        setupStudentInfo(student);

        courseTable.setVisible(true);
        addButton.setDisable(false);

        showAlert("Success", "Student found: " + student.getFullName(), Alert.AlertType.INFORMATION);
    }

    private void loadResultsForStudent(int studentId) {
        results.setAll(resultDAO.getResultsByStudentId(studentId));
        updateSummary();
    }

    private void setupStudentInfo(Student student) {
        if (student != null) {
            studentNameLabel.setText("Name: " + student.getFullName());
            studentIdLabel.setText("ID: " + student.getStudentCode());
            studentProgramLabel.setText("Program: " + student.getDepartment());
        }
    }

    private void setupButtonStates() {
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    @FXML
    private void handleBack() {
        loadScene("Admin_dashboard.fxml");
    }

    @FXML
    private void handleAdd() {
        if (currentStudentId == 0) {
            showAlert("Error", "Please search for a student first!", Alert.AlertType.ERROR);
            return;
        }

        Dialog<Result> dialog = createResultDialog(null);
        dialog.showAndWait().ifPresent(newResult -> {
            Course selectedCourse = courseDAO.getCourseByName(newResult.getCourseName());
            if (selectedCourse == null) {
                showAlert("Error", "Course not found.", Alert.AlertType.ERROR);
                return;
            }

            int enrollmentId = enrollmentDAO.getEnrollmentId(currentStudentId, selectedCourse.getCourseId());
            if (enrollmentId == 0) {
                enrollmentId = enrollmentDAO.addEnrollment(currentStudentId, selectedCourse.getCourseId());
                if (enrollmentId == 0) {
                    showAlert("Error", "Failed to enroll student in course.", Alert.AlertType.ERROR);
                    return;
                }
            }

            newResult.setEnrollmentId(enrollmentId);

            // تحديد الـ GPA تلقائيًا بناءً على الدرجة
            double calculatedGPA = calculateGPAFromGrade(newResult.getGrade());
            newResult.setGpa(calculatedGPA);

            if (resultDAO.addResult(newResult)) {
                // تحديث الـ GPA في جدول الطلاب
                updateStudentGPA();

                loadResultsForStudent(currentStudentId);
                showAlert("Success", "Result added successfully!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to add result.", Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    private void handleEdit() {
        Result selected = courseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a result to edit.", Alert.AlertType.WARNING);
            return;
        }

        Dialog<Result> dialog = createResultDialog(selected);
        dialog.showAndWait().ifPresent(updatedResult -> {
            updatedResult.setResultId(selected.getResultId());

            // تحديد الـ GPA تلقائيًا بناءً على الدرجة
            double calculatedGPA = calculateGPAFromGrade(updatedResult.getGrade());
            updatedResult.setGpa(calculatedGPA);

            if (resultDAO.updateResult(updatedResult)) {
                // تحديث الـ GPA في جدول الطلاب
                updateStudentGPA();

                loadResultsForStudent(currentStudentId);
                showAlert("Success", "Result updated successfully!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to update result.", Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    private void handleDelete() {
        Result selected = courseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a result to delete.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setContentText("Are you sure you want to delete result for '" + selected.getCourseName() + "'?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (resultDAO.deleteResult(selected.getResultId())) {
                // تحديث الـ GPA في جدول الطلاب بعد الحذف
                updateStudentGPA();

                loadResultsForStudent(currentStudentId);
                showAlert("Success", "Result deleted successfully!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to delete result.", Alert.AlertType.ERROR);
            }
        }
    }

    private Dialog<Result> createResultDialog(Result result) {
        Dialog<Result> dialog = new Dialog<>();
        dialog.setTitle(result == null ? "Add New Result" : "Edit Result");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        ComboBox<Course> courseCombo = new ComboBox<>();
        courseCombo.setPromptText("Select Course");
        courseCombo.getItems().addAll(courseDAO.getAllCourses());

        courseCombo.setCellFactory(lv -> new ListCell<Course>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                setText(empty || course == null ? null : course.getCourseName() + " (" + course.getCourseCode() + ")");
            }
        });

        courseCombo.setButtonCell(new ListCell<Course>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                setText(empty || course == null ? "Select Course" : course.getCourseName() + " (" + course.getCourseCode() + ")");
            }
        });

        if (result != null) {
            Course currentCourse = courseDAO.getCourseByName(result.getCourseName());
            if (currentCourse != null) {
                courseCombo.setValue(currentCourse);
            }
        }

        TextField credits = new TextField();
        credits.setEditable(false);

        courseCombo.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                credits.setText(String.valueOf(newVal.getCredits()));
            } else {
                credits.clear();
            }
        });

        ComboBox<String> grade = new ComboBox<>(FXCollections.observableArrayList(
                "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D", "F"
        ));
        grade.setValue(result != null ? result.getGrade() : "B");

        TextField degreeField = new TextField();
        degreeField.setPromptText("Enter degree (0-100)");
        if (result != null) {
            degreeField.setText(String.valueOf(result.getDegree()));
        }

        // GPA Field مخفي
        Label gpaLabel = new Label();
        gpaLabel.setText("GPA: Will be calculated automatically");

        // Auto-calculate degree based on grade
        grade.valueProperty().addListener((obs, oldVal, newVal) -> {
            int degree = calculateDegreeFromGrade(newVal);
            degreeField.setText(String.valueOf(degree));
        });

        // Auto-calculate degree on initial load
        if (result != null) {
            int degree = calculateDegreeFromGrade(result.getGrade());
            degreeField.setText(String.valueOf(degree));
        }

        grid.add(new Label("Course:"), 0, 0);
        grid.add(courseCombo, 1, 0);
        grid.add(new Label("Credits:"), 0, 1);
        grid.add(credits, 1, 1);
        grid.add(new Label("Grade:"), 0, 2);
        grid.add(grade, 1, 2);
        grid.add(new Label("Degree (0-100):"), 0, 3);
        grid.add(degreeField, 1, 3);
        grid.add(gpaLabel, 0, 4);
        grid.add(new GridPane(), 1, 4); // placeholder

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                Course selectedCourse = courseCombo.getValue();
                if (selectedCourse == null) {
                    showAlert("Error", "Please select a course.", Alert.AlertType.ERROR);
                    return null;
                }

                try {
                    Result r = new Result();
                    r.setCourseName(selectedCourse.getCourseName());
                    r.setCredits(selectedCourse.getCredits());
                    r.setGrade(grade.getValue());

                    int degreeValue = Integer.parseInt(degreeField.getText().trim());
                    if (degreeValue < 0 || degreeValue > 100) {
                        showAlert("Error", "Degree must be between 0 and 100.", Alert.AlertType.ERROR);
                        return null;
                    }
                    r.setDegree(degreeValue);

                    // GPA سيتم حسابه تلقائيًا لاحقًا
                    r.setGpa(0.0); // قيمة مؤقتة

                    return r;
                } catch (NumberFormatException e) {
                    showAlert("Error", "Please enter valid number for degree.", Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    private int calculateDegreeFromGrade(String grade) {
        switch (grade) {
            case "A": return 95;
            case "A-": return 90;
            case "B+": return 87;
            case "B": return 83;
            case "B-": return 80;
            case "C+": return 77;
            case "C": return 73;
            case "C-": return 70;
            case "D": return 65;
            case "F": return 50;
            default: return 70;
        }
    }

    private double calculateGPAFromGrade(String grade) {
        switch (grade) {
            case "A": return 4.0;
            case "A-": return 3.7;
            case "B+": return 3.3;
            case "B": return 3.0;
            case "B-": return 2.7;
            case "C+": return 2.3;
            case "C": return 2.0;
            case "C-": return 1.7;
            case "D": return 1.0;
            case "F": return 0.0;
            default: return 0.0;
        }
    }

    private void updateStudentGPA() {
        if (currentStudentId == 0) return;

        double totalWeightedGPA = 0;
        int totalCredits = 0;

        for (Result result : results) {
            double courseGPA = calculateGPAFromGrade(result.getGrade());
            totalWeightedGPA += courseGPA * result.getCredits();
            totalCredits += result.getCredits();
        }

        double overallGPA = 0.0;
        if (totalCredits > 0) {
            overallGPA = totalWeightedGPA / totalCredits;
        }

        // تحديث الـ GPA في جدول الطلاب
        studentDAO.updateStudentGPA(currentStudentId, overallGPA);

        // تحديث الـ GPA في الـ label
        currentGPALabel.setText(String.format("%.2f", overallGPA));

        // تحديث الـ GPA في الكائن الحالي
        if (currentStudent != null) {
            currentStudent.setGpa(overallGPA);
        }
    }

    private void updateSummary() {
        int total = results.size();
        totalCoursesLabel.setText(String.valueOf(total));

        if (total > 0) {
            double totalWeightedGPA = 0;
            int totalCredits = 0;
            double totalDegree = 0;

            for (Result r : results) {
                double courseGPA = calculateGPAFromGrade(r.getGrade());
                totalWeightedGPA += courseGPA * r.getCredits();
                totalCredits += r.getCredits();
                totalDegree += r.getDegree();
            }

            double overallGPA = 0.0;
            if (totalCredits > 0) {
                overallGPA = totalWeightedGPA / totalCredits;
            }

            double avgDegree = totalDegree / total;
            currentGPALabel.setText(String.format("%.2f", overallGPA));
            averageGradeLabel.setText(String.format("%.0f/100", avgDegree));
        } else {
            currentGPALabel.setText("0.00");
            averageGradeLabel.setText("0/100");
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
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load page.", Alert.AlertType.ERROR);
        }
    }
}