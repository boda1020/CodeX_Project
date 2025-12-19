package Controller;

import Database.DatabaseConnection;
import Model.User;
import DAO.CourseDAO;
import DAO.Impl.CourseDAOImpl;
import Utils.CurrentUser;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class StudentDashboardController {

    // KPI Labels
    @FXML private Label enrolledCoursesLabel;
    @FXML private Label completedLecturesLabel;
    @FXML private Label currentGpaLabel;
    @FXML private Label bestCourseLabel;

    // Charts
    @FXML private LineChart<String, Number> gpaProgressChart;
    @FXML private BarChart<String, Number> courseGradesChart;

    private final CourseDAO courseDAO = new CourseDAOImpl();

    @FXML
    private void initialize() {
        System.out.println("StudentDashboardController - initialize() called");
        loadStudentDashboard();
    }

    private void loadStudentDashboard() {
        User currentUser = CurrentUser.getCurrentUser();
        if (currentUser == null) {
            System.out.println("ERROR: No current user found!");
            setDefaultData();
            return;
        }

        System.out.println("Current user ID: " + currentUser.getUserId());

        // Load all data
        loadKPIData(currentUser.getUserId());
        loadCharts(currentUser.getUserId());
    }

    private void loadKPIData(int userId) {
        // 1. عدد الكورسات المسجلة
        int enrolledCoursesCount = getEnrolledCoursesCount(userId);
        enrolledCoursesLabel.setText(String.valueOf(enrolledCoursesCount));

        // 2. عدد المحاضرات المكتملة
        int completedLecturesCount = getCompletedLecturesCount(userId);
        completedLecturesLabel.setText(String.valueOf(completedLecturesCount));

        // 3. المعدل التراكمي الحالي
        double currentGPA = getCurrentGPA(userId);
        currentGpaLabel.setText(String.format("%.2f", currentGPA));

        // 4. أفضل كورس
        String bestCourse = getBestCourse(userId);
        bestCourseLabel.setText(bestCourse != null ? bestCourse : "N/A");
    }

    private void loadCharts(int userId) {
        double currentGPA = getCurrentGPA(userId);
        String studentCode = getStudentCode(userId);

        // 5. تقدم المعدل التراكمي
        loadGPAProgressChart(currentGPA);

        // 6. الدرجات حسب الكورس
        if (studentCode != null) {
            loadCourseGradesChart(studentCode);
        } else {
            loadSampleCourseGrades();
        }
    }

    private void setDefaultData() {
        enrolledCoursesLabel.setText("4");
        completedLecturesLabel.setText("18");
        currentGpaLabel.setText("3.68");
        bestCourseLabel.setText("JAVA Programming");
        loadSampleCharts();
    }

    private int getEnrolledCoursesCount(int userId) {
        String sql = "SELECT COUNT(*) as count FROM enrollments WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getCompletedLecturesCount(int userId) {
        String sql = """
            SELECT COUNT(*) as count 
            FROM enrollments e
            JOIN lectures l ON e.course_id = l.course_id
            WHERE e.user_id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private double getCurrentGPA(int userId) {
        String sql = "SELECT gpa FROM students WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double gpa = rs.getDouble("gpa");
                return rs.wasNull() ? 0.0 : gpa;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private String getBestCourse(int userId) {
        String studentCode = getStudentCode(userId);
        if (studentCode == null) return null;

        // Try multiple sources
        String bestCourse = getBestCourseFromStudentResults(studentCode);
        if (bestCourse == null) {
            bestCourse = getBestCourseFromResultsTable(studentCode);
        }
        if (bestCourse == null) {
            bestCourse = getBestCourseFromEnrollments(studentCode);
        }

        return bestCourse;
    }

    private String getBestCourseFromStudentResults(String studentCode) {
        String sql = """
            SELECT c.course_name, sr.degree
            FROM student_results sr
            JOIN courses c ON sr.course_id = c.course_id
            WHERE sr.student_id = ?
            ORDER BY sr.degree DESC
            LIMIT 1
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentCode);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String courseName = rs.getString("course_name");
                int degree = rs.getInt("degree");
                String grade = getGradeFromDegree(degree);
                return courseName + " (" + grade + ")";
            }
        } catch (SQLException e) {
            System.out.println("Error in getBestCourseFromStudentResults: " + e.getMessage());
        }
        return null;
    }

    private String getBestCourseFromResultsTable(String studentCode) {
        String sql = """
            SELECT c.course_name, r.degree
            FROM results r
            JOIN enrollments e ON r.enrollment_id = e.enrollment_id
            JOIN students s ON e.user_id = s.student_id
            JOIN courses c ON e.course_id = c.course_id
            WHERE s.student_code = ?
            ORDER BY r.degree DESC
            LIMIT 1
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentCode);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String courseName = rs.getString("course_name");
                int degree = rs.getInt("degree");
                String grade = getGradeFromDegree(degree);
                return courseName + " (" + grade + ")";
            }
        } catch (SQLException e) {
            System.out.println("Error in getBestCourseFromResultsTable: " + e.getMessage());
        }
        return null;
    }

    private String getBestCourseFromEnrollments(String studentCode) {
        String sql = """
            SELECT c.course_name, MAX(e.progress_percentage) as max_progress
            FROM enrollments e
            JOIN students s ON e.user_id = s.student_id
            JOIN courses c ON e.course_id = c.course_id
            WHERE s.student_code = ?
            GROUP BY c.course_name
            ORDER BY max_progress DESC
            LIMIT 1
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentCode);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String courseName = rs.getString("course_name");
                double progress = rs.getDouble("max_progress");
                // Simulate grade from progress
                int simulatedGrade = (int) (progress * 0.8 + 60);
                String grade = getGradeFromDegree(simulatedGrade);
                return courseName + " (" + grade + ")";
            }
        } catch (SQLException e) {
            System.out.println("Error in getBestCourseFromEnrollments: " + e.getMessage());
        }
        return null;
    }

    private String getStudentCode(int userId) {
        String sql = "SELECT student_code FROM students WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("student_code");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getGradeFromDegree(int degree) {
        if (degree >= 90) return "A";
        else if (degree >= 85) return "A-";
        else if (degree >= 80) return "B+";
        else if (degree >= 75) return "B";
        else if (degree >= 70) return "B-";
        else if (degree >= 65) return "C+";
        else if (degree >= 60) return "C";
        else if (degree >= 55) return "C-";
        else if (degree >= 50) return "D";
        else return "F";
    }

    private void loadGPAProgressChart(double currentGPA) {
        gpaProgressChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Your GPA");

        // Generate progressive GPA
        double startGPA = Math.max(2.5, currentGPA - 1.2);
        series.getData().add(new XYChart.Data<>("Jan", startGPA));
        series.getData().add(new XYChart.Data<>("Feb", startGPA + 0.3));
        series.getData().add(new XYChart.Data<>("Mar", startGPA + 0.6));
        series.getData().add(new XYChart.Data<>("Apr", startGPA + 0.9));
        series.getData().add(new XYChart.Data<>("May", currentGPA));

        gpaProgressChart.getData().add(series);

        // Style
        gpaProgressChart.setLegendVisible(false);
        gpaProgressChart.setCreateSymbols(true);
    }

    private void loadCourseGradesChart(String studentCode) {
        courseGradesChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Grade (%)");

        // Try to get real data
        boolean hasData = false;

        // Try from student_results
        hasData = loadCourseDataFromStudentResults(studentCode, series);

        if (!hasData) {
            // Try from results table
            hasData = loadCourseDataFromResultsTable(studentCode, series);
        }

        if (!hasData) {
            // Try from enrollments
            hasData = loadCourseDataFromEnrollments(studentCode, series);
        }

        if (!hasData) {
            // Load sample data
            loadSampleCourseGrades(series);
        }

        courseGradesChart.getData().add(series);

        // Style
        courseGradesChart.setLegendVisible(false);
        courseGradesChart.setCategoryGap(20); // زيادة المسافة بين الأعمدة
    }

    private boolean loadCourseDataFromStudentResults(String studentCode, XYChart.Series<String, Number> series) {
        String sql = """
            SELECT c.course_name, sr.degree
            FROM student_results sr
            JOIN courses c ON sr.course_id = c.course_id
            WHERE sr.student_id = ?
            ORDER BY c.course_name
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentCode);
            ResultSet rs = pstmt.executeQuery();

            boolean hasData = false;
            while (rs.next()) {
                String courseName = rs.getString("course_name");
                int degree = rs.getInt("degree");

                // Shorten long names
                String displayName = formatCourseName(courseName);
                series.getData().add(new XYChart.Data<>(displayName, degree));
                hasData = true;
            }
            return hasData;

        } catch (SQLException e) {
            System.out.println("Error loading from student_results: " + e.getMessage());
            return false;
        }
    }

    private boolean loadCourseDataFromResultsTable(String studentCode, XYChart.Series<String, Number> series) {
        String sql = """
            SELECT c.course_name, r.degree
            FROM results r
            JOIN enrollments e ON r.enrollment_id = e.enrollment_id
            JOIN students s ON e.user_id = s.student_id
            JOIN courses c ON e.course_id = c.course_id
            WHERE s.student_code = ?
            ORDER BY c.course_name
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentCode);
            ResultSet rs = pstmt.executeQuery();

            boolean hasData = false;
            while (rs.next()) {
                String courseName = rs.getString("course_name");
                int degree = rs.getInt("degree");

                String displayName = formatCourseName(courseName);
                series.getData().add(new XYChart.Data<>(displayName, degree));
                hasData = true;
            }
            return hasData;

        } catch (SQLException e) {
            System.out.println("Error loading from results table: " + e.getMessage());
            return false;
        }
    }

    private boolean loadCourseDataFromEnrollments(String studentCode, XYChart.Series<String, Number> series) {
        String sql = """
            SELECT c.course_name, e.progress_percentage
            FROM enrollments e
            JOIN students s ON e.user_id = s.student_id
            JOIN courses c ON e.course_id = c.course_id
            WHERE s.student_code = ?
            ORDER BY c.course_name
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentCode);
            ResultSet rs = pstmt.executeQuery();

            boolean hasData = false;
            while (rs.next()) {
                String courseName = rs.getString("course_name");
                double progress = rs.getDouble("progress_percentage");

                // Convert progress to simulated grade
                int simulatedGrade = (int) (progress * 0.8 + 60);
                simulatedGrade = Math.min(100, Math.max(0, simulatedGrade));

                String displayName = formatCourseName(courseName);
                series.getData().add(new XYChart.Data<>(displayName, simulatedGrade));
                hasData = true;
            }
            return hasData;

        } catch (SQLException e) {
            System.out.println("Error loading from enrollments: " + e.getMessage());
            return false;
        }
    }

    private String formatCourseName(String courseName) {
        // Shorten long course names
        if (courseName.length() > 12) {
            // Keep important words
            String[] words = courseName.split(" ");
            if (words.length > 2) {
                return words[0] + " " + words[1];
            } else {
                return courseName.substring(0, 10) + "...";
            }
        }
        return courseName;
    }

    private void loadSampleCourseGrades() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Grade (%)");

        series.getData().add(new XYChart.Data<>("JAVA", 95));
        series.getData().add(new XYChart.Data<>("Data Struct", 88));
        series.getData().add(new XYChart.Data<>("Database", 92));
        series.getData().add(new XYChart.Data<>("IT", 85));

        courseGradesChart.getData().clear();
        courseGradesChart.getData().add(series);

        // Style
        courseGradesChart.setLegendVisible(false);
        courseGradesChart.setCategoryGap(20);
    }

    private void loadSampleCourseGrades(XYChart.Series<String, Number> series) {
        series.getData().add(new XYChart.Data<>("JAVA", 95));
        series.getData().add(new XYChart.Data<>("Data Struct", 88));
        series.getData().add(new XYChart.Data<>("Database", 92));
        series.getData().add(new XYChart.Data<>("IT", 85));
    }

    private void loadSampleCharts() {
        // GPA Chart
        XYChart.Series<String, Number> gpaSeries = new XYChart.Series<>();
        gpaSeries.setName("Your GPA");
        gpaSeries.getData().addAll(
                new XYChart.Data<>("Jan", 3.2),
                new XYChart.Data<>("Feb", 3.4),
                new XYChart.Data<>("Mar", 3.5),
                new XYChart.Data<>("Apr", 3.6),
                new XYChart.Data<>("May", 3.68)
        );

        gpaProgressChart.getData().clear();
        gpaProgressChart.getData().add(gpaSeries);

        // Grades Chart
        loadSampleCourseGrades();

        // Style
        gpaProgressChart.setLegendVisible(false);
        gpaProgressChart.setCreateSymbols(true);
    }

    // Sidebar buttons
    @FXML
    private void handleDashboard() {
        loadStudentDashboard();
    }

    @FXML
    private void handleCourses() {
        loadScene("/View/StudentCourses.fxml", "My Courses");
    }

    @FXML
    private void handleResults() {
        loadScene("/View/Student_results.fxml", "Results");
    }

    @FXML
    private void handleProfile() {
        loadScene("/View/profileStudent.fxml", "Profile");
    }

    @FXML
    private void handleLogout() {
        CurrentUser.setCurrentUser(null);
        loadScene("/View/Login.fxml", "Login");
    }

    private void loadScene(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) enrolledCoursesLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load page: " + fxmlPath, Alert.AlertType.ERROR);
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