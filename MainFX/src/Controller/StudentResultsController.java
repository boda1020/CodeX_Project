package Controller;

import Model.User;
import Utils.CurrentUser;
import Database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentResultsController {

    @FXML private Button btnBack;
    @FXML private Label nameValue;
    @FXML private Label idValue;
    @FXML private Label totalCoursesValue;
    @FXML private Label averageGradeValue;
    @FXML private Label gpaValue;
    @FXML private VBox resultsContainer;
    @FXML private Button btnPrintResults;

    private List<CourseResult> courseResults = new ArrayList<>();

    @FXML
    public void initialize() {
        System.out.println("StudentResultsController initialized");
        loadStudentResultsFromDatabase();
    }

    private void loadStudentResultsFromDatabase() {
        User currentUser = CurrentUser.getCurrentUser();
        if (currentUser == null) {
            showAlert("Error", "Please login again.");
            return;
        }

        int studentId = currentUser.getUserId();

        String studentQuery = "SELECT u.full_name, u.username FROM users u WHERE u.user_id = ?";
        String resultsQuery = "SELECT c.course_name, r.degree, r.grade, r.gpa_points " +
                "FROM results r " +
                "JOIN enrollments e ON r.enrollment_id = e.enrollment_id " +
                "JOIN courses c ON e.course_id = c.course_id " +
                "WHERE e.user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(studentQuery)) {
                pstmt.setInt(1, studentId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        nameValue.setText(rs.getString("full_name"));
                        idValue.setText(rs.getString("username"));
                    }
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(resultsQuery)) {
                pstmt.setInt(1, studentId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    courseResults.clear();
                    while (rs.next()) {
                        String courseName = rs.getString("course_name");
                        int degree = rs.getInt("degree");
                        String grade = rs.getString("grade");
                        double gpa = rs.getDouble("gpa_points");
                        courseResults.add(new CourseResult(courseName, degree, grade, gpa));
                    }
                }
            }

            calculateStatistics();
            loadResults();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load results from database.");
        }

        setupButtonActions();
    }

    private void calculateStatistics() {
        totalCoursesValue.setText(String.valueOf(courseResults.size()));

        if (courseResults.isEmpty()) {
            averageGradeValue.setText("N/A");
            gpaValue.setText("0.00");
            return;
        }

        double totalGpaPoints = 0;
        double totalDegree = 0;
        for (CourseResult result : courseResults) {
            totalGpaPoints += result.getGpa();
            totalDegree += result.getDegree();
        }

        double gpa = totalGpaPoints / courseResults.size();
        double avgDegree = totalDegree / courseResults.size();

        gpaValue.setText(String.format("%.2f", gpa));
        averageGradeValue.setText(String.format("%.0f%%", avgDegree));
    }

    private void loadResults() {
        resultsContainer.getChildren().clear();

        for (int i = 0; i < courseResults.size(); i++) {
            CourseResult result = courseResults.get(i);
            HBox row = createResultRow(result, i);
            resultsContainer.getChildren().add(row);
        }
    }

    private HBox createResultRow(CourseResult result, int index) {
        HBox row = new HBox();
        row.setSpacing(0);
        row.setPadding(new javafx.geometry.Insets(20));

        if (index < courseResults.size() - 1) {
            row.setStyle("-fx-border-color: transparent transparent #e2e8f0 transparent; -fx-border-width: 0 0 1 0;");
        }

        Label courseLabel = new Label(result.getCourseName());
        courseLabel.setPrefWidth(400);
        courseLabel.setStyle("-fx-font-size: 15; -fx-text-fill: #262451;");
        courseLabel.maxWidth(Double.MAX_VALUE);
        HBox.setHgrow(courseLabel, javafx.scene.layout.Priority.ALWAYS);

        Label degreeLabel = new Label(String.valueOf(result.getDegree()));
        degreeLabel.setPrefWidth(100);
        degreeLabel.setAlignment(Pos.CENTER);
        degreeLabel.setStyle("-fx-font-size: 15; -fx-text-fill: #262451;");

        Label gradeLabel = new Label(result.getGrade());
        gradeLabel.setPrefWidth(100);
        gradeLabel.setAlignment(Pos.CENTER);
        gradeLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; " + getGradeColor(result.getGrade()));

        Label gpaLabel = new Label(String.format("%.2f", result.getGpa()));
        gpaLabel.setPrefWidth(100);
        gpaLabel.setAlignment(Pos.CENTER_RIGHT);
        gpaLabel.setStyle("-fx-font-size: 15; -fx-text-fill: #262451;");

        row.getChildren().addAll(courseLabel, degreeLabel, gradeLabel, gpaLabel);
        return row;
    }

    private String getGradeColor(String grade) {
        switch (grade.toUpperCase()) {
            case "A": return "-fx-text-fill: #10b981;";
            case "A-": return "-fx-text-fill: #34d399;";
            case "B+": return "-fx-text-fill: #3b82f6;";
            case "B": return "-fx-text-fill: #60a5fa;";
            case "B-": return "-fx-text-fill: #f59e0b;";
            case "C+": return "-fx-text-fill: #fbbf24;";
            case "C": return "-fx-text-fill: #f97316;";
            case "C-": return "-fx-text-fill: #fb923c;";
            case "D": return "-fx-text-fill: #8b5cf6;";
            case "F": return "-fx-text-fill: #ef4444;";
            default: return "-fx-text-fill: #262451;";
        }
    }

    private void setupButtonActions() {
        btnBack.setOnAction(event -> navigateTo("/View/Student_dashboard.fxml", "Student Dashboard"));
        btnPrintResults.setOnAction(event -> printResults());
    }

    private void printResults() {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null && printerJob.showPrintDialog(btnPrintResults.getScene().getWindow())) {
            VBox printContent = createPrintableContent();
            if (printerJob.printPage(printContent)) {
                printerJob.endJob();
                showAlert("Success", "Results printed successfully!", Alert.AlertType.INFORMATION);
            }
        }
    }

    private VBox createPrintableContent() {
        VBox printable = new VBox(20);
        printable.setPadding(new javafx.geometry.Insets(40));
        printable.setStyle("-fx-background-color: white;");

        Label title = new Label("ACADEMIC TRANSCRIPT");
        title.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #262451;");
        title.setAlignment(Pos.CENTER);

        VBox studentInfo = new VBox(10);
        studentInfo.getChildren().addAll(
                new Label("Name: " + nameValue.getText()),
                new Label("Student ID: " + idValue.getText())
        );

        VBox table = new VBox();
        HBox header = new HBox();
        header.setPadding(new javafx.geometry.Insets(10));
        header.setStyle("-fx-background-color: #262451;");

        Label courseHeader = new Label("Course Name");
        courseHeader.setPrefWidth(300);
        courseHeader.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label degreeHeader = new Label("Degree");
        degreeHeader.setPrefWidth(100);
        degreeHeader.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        degreeHeader.setAlignment(Pos.CENTER);

        Label gradeHeader = new Label("Grade");
        gradeHeader.setPrefWidth(100);
        gradeHeader.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        gradeHeader.setAlignment(Pos.CENTER);

        Label gpaHeader = new Label("GPA");
        gpaHeader.setPrefWidth(100);
        gpaHeader.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        gpaHeader.setAlignment(Pos.CENTER);

        header.getChildren().addAll(courseHeader, degreeHeader, gradeHeader, gpaHeader);

        VBox tableBody = new VBox();
        for (CourseResult result : courseResults) {
            HBox row = new HBox();
            row.setPadding(new javafx.geometry.Insets(10));

            Label c = new Label(result.getCourseName());
            c.setPrefWidth(300);

            Label d = new Label(String.valueOf(result.getDegree()));
            d.setPrefWidth(100);
            d.setAlignment(Pos.CENTER);

            Label g = new Label(result.getGrade());
            g.setPrefWidth(100);
            g.setAlignment(Pos.CENTER);

            Label gp = new Label(String.format("%.2f", result.getGpa()));
            gp.setPrefWidth(100);
            gp.setAlignment(Pos.CENTER);

            row.getChildren().addAll(c, d, g, gp);
            tableBody.getChildren().add(row);
        }

        table.getChildren().addAll(header, tableBody);

        VBox summary = new VBox(10);
        summary.getChildren().addAll(
                new Label("Total Courses: " + totalCoursesValue.getText()),
                new Label("Average Grade: " + averageGradeValue.getText()),
                new Label("GPA: " + gpaValue.getText())
        );

        printable.getChildren().addAll(title, studentInfo, table, summary);
        return printable;
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load page.");
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        showAlert(title, message, Alert.AlertType.ERROR);
    }

    private static class CourseResult {
        private String courseName;
        private int degree;
        private String grade;
        private double gpa;

        public CourseResult(String courseName, int degree, String grade, double gpa) {
            this.courseName = courseName;
            this.degree = degree;
            this.grade = grade;
            this.gpa = gpa;
        }

        public String getCourseName() { return courseName; }
        public int getDegree() { return degree; }
        public String getGrade() { return grade; }
        public double getGpa() { return gpa; }
    }
}