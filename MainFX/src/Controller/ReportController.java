package Controller;

import Database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;


public class ReportController {

    @FXML private Button btnBack;
    @FXML private Button btnGenerate;
    @FXML private Button btnPrint;
    @FXML private Button btnExportPDF;
    @FXML private TextField searchField;

    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private ComboBox<String> semesterCombo;

    @FXML private TextArea reportTextArea;

    @FXML private Label totalStudentsLabel;
    @FXML private Label averageGPALabel;
    @FXML private Label successRateLabel;

    @FXML private Button btnStudentTranscript;
    @FXML private Button btnClassPerformance;
    @FXML private Button btnDepartmentStats;

    @FXML
    private void initialize() {
        System.out.println("ReportController initialized");
        setupComboBoxes();
        updateStatistics();
    }

    private void setupComboBoxes() {
        reportTypeCombo.getItems().addAll("Academic Report", "Financial Report",
                "Attendance Report", "Performance Report",
                "Enrollment Report", "Student Transcript",
                "Class Performance", "Department Statistics");
        reportTypeCombo.setValue("Academic Report");

        semesterCombo.getItems().addAll("Fall 2024", "Spring 2024", "Summer 2024",
                "Fall 2023", "Spring 2023", "Summer 2023");
        semesterCombo.setValue("Fall 2024");
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/Admin_dashboard.fxml"));
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load dashboard.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleGenerateReport() {
        String type = reportTypeCombo.getValue();
        String semester = semesterCombo.getValue();

        if (type == null || type.isEmpty()) {
            showAlert("Error", "Please select a report type.", Alert.AlertType.ERROR);
            return;
        }

        String reportContent = generateDynamicReportContent(type, semester);
        reportTextArea.setText(reportContent);

        // Save report to database (optional)
        // saveReportToDatabase(type, semester, reportContent);
    }

    @FXML
    private void handleExportReport() {

        if (reportTextArea.getText().isEmpty()) {
            showAlert("Error", "No report to export.", Alert.AlertType.ERROR);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV File", "*.csv")
        );
        fileChooser.setInitialFileName("report.csv");

        File file = fileChooser.showSaveDialog(btnExportPDF.getScene().getWindow());
        if (file == null) return;

        try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {

            String[] lines = reportTextArea.getText().split("\n");
            for (String line : lines) {
                writer.println(line);
            }

            showAlert("Success", "Report exported successfully.", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to export report.", Alert.AlertType.ERROR);
        }
    }



    @FXML
    private void handlePrint() {

        if (reportTextArea.getText().isEmpty()) {
            showAlert("Error", "No report to print.", Alert.AlertType.ERROR);
            return;
        }

        javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();

        if (job != null && job.showPrintDialog(btnPrint.getScene().getWindow())) {
            boolean success = job.printPage(reportTextArea);
            if (success) {
                job.endJob();
                showAlert("Success", "Printing completed.", Alert.AlertType.INFORMATION);
            }
        }
    }



    private String generateDynamicReportContent(String type, String semester) {
        StringBuilder content = new StringBuilder();

        content.append("=".repeat(60)).append("\n");
        content.append("UNIVERSITY ACADEMIC REPORT\n");
        content.append("=".repeat(60)).append("\n\n");
        content.append("Report Type: ").append(type).append("\n");
        content.append("Semester: ").append(semester).append("\n");
        content.append("Generation Date: ").append(LocalDate.now()).append("\n");
        content.append("Generated By: Admin\n\n");

        switch (type) {
            case "Academic Report":
                content.append(generateDynamicAcademicReport());
                break;
            case "Financial Report":
                content.append(generateDynamicFinancialReport());
                break;
            case "Attendance Report":
                content.append(generateDynamicAttendanceReport());
                break;
            case "Performance Report":
                content.append(generateDynamicPerformanceReport());
                break;
            case "Enrollment Report":
                content.append(generateDynamicEnrollmentReport());
                break;
            case "Student Transcript":
                content.append(generateDynamicStudentTranscript());
                break;
            case "Class Performance":
                content.append(generateDynamicClassPerformance());
                break;
            case "Department Statistics":
                content.append(generateDynamicDepartmentStats());
                break;
            default:
                content.append("Report content for ").append(type).append("\n");
        }

        return content.toString();
    }

    private String generateDynamicAcademicReport() {
        StringBuilder content = new StringBuilder();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql1 = "SELECT COUNT(*) as total FROM students WHERE status = 'Active'";
            try (PreparedStatement pstmt = conn.prepareStatement(sql1);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int totalStudents = rs.getInt("total");
                    content.append("TOTAL STUDENTS: ").append(totalStudents).append("\n\n");
                }
            }

            String sql2 = "SELECT AVG(gpa) as avg_gpa FROM students WHERE gpa > 0";
            try (PreparedStatement pstmt = conn.prepareStatement(sql2);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double avgGPA = rs.getDouble("avg_gpa");
                    content.append("AVERAGE GPA: ").append(String.format("%.2f", avgGPA)).append("\n\n");
                }
            }

            String sql3 = """
                SELECT department, COUNT(*) as student_count, AVG(gpa) as avg_gpa
                FROM students 
                WHERE status = 'Active' AND gpa > 0
                GROUP BY department
                ORDER BY avg_gpa DESC
            """;
            content.append("DEPARTMENT STATISTICS:\n");
            content.append("-".repeat(40)).append("\n");

            try (PreparedStatement pstmt = conn.prepareStatement(sql3);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String dept = rs.getString("department");
                    int count = rs.getInt("student_count");
                    double gpa = rs.getDouble("avg_gpa");
                    content.append(String.format("%-25s %-10d %.2f%n", dept, count, gpa));
                }
            }

            String sql4 = """
                SELECT s.student_code, u.full_name, s.gpa, s.department
                FROM students s
                JOIN users u ON s.student_id = u.user_id
                WHERE s.gpa > 0
                ORDER BY s.gpa DESC
                LIMIT 5
            """;
            content.append("\nTOP PERFORMING STUDENTS:\n");
            content.append("-".repeat(60)).append("\n");

            try (PreparedStatement pstmt = conn.prepareStatement(sql4);
                 ResultSet rs = pstmt.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    String code = rs.getString("student_code");
                    String name = rs.getString("full_name");
                    double gpa = rs.getDouble("gpa");
                    String dept = rs.getString("department");
                    content.append(String.format("%d. %-15s %-25s %-20s GPA: %.2f%n",
                            rank++, code, name, dept, gpa));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            content.append("Error retrieving data: ").append(e.getMessage());
        }

        return content.toString();
    }

    private String generateDynamicFinancialReport() {
        StringBuilder content = new StringBuilder();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) as total_students FROM students WHERE status = 'Active'";
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int totalStudents = rs.getInt("total_students");
                    double tuitionPerStudent = 5000.0;
                    double totalIncome = totalStudents * tuitionPerStudent;

                    content.append("FINANCIAL SUMMARY\n");
                    content.append("-".repeat(40)).append("\n");
                    content.append("Total Active Students: ").append(totalStudents).append("\n");
                    content.append("Tuition per Student: $").append(String.format("%.0f", tuitionPerStudent)).append("\n");
                    content.append("Estimated Annual Income: $").append(String.format("%.0f", totalIncome)).append("\n");

                    double facultyExpense = totalIncome * 0.40;
                    double facilityExpense = totalIncome * 0.20;
                    double adminExpense = totalIncome * 0.15;
                    double otherExpense = totalIncome * 0.10;
                    double totalExpense = facultyExpense + facilityExpense + adminExpense + otherExpense;
                    double netProfit = totalIncome - totalExpense;

                    content.append("\nEXPENSES BREAKDOWN:\n");
                    content.append("-".repeat(40)).append("\n");
                    content.append(String.format("Faculty Salaries: $%.0f%n", facultyExpense));
                    content.append(String.format("Facilities: $%.0f%n", facilityExpense));
                    content.append(String.format("Administrative: $%.0f%n", adminExpense));
                    content.append(String.format("Other Expenses: $%.0f%n", otherExpense));
                    content.append(String.format("Total Expenses: $%.0f%n", totalExpense));
                    content.append(String.format("Net Profit: $%.0f%n", netProfit));
                    content.append(String.format("Profit Margin: %.1f%%", (netProfit/totalIncome)*100));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            content.append("Error retrieving financial data: ").append(e.getMessage());
        }

        return content.toString();
    }

    private String generateDynamicAttendanceReport() {
        StringBuilder content = new StringBuilder();

        content.append("ATTENDANCE REPORT\n");
        content.append("-".repeat(40)).append("\n");
        content.append("Note: Attendance tracking system coming soon.\n");
        content.append("This report will show real attendance data when implemented.\n\n");

        content.append("ATTENDANCE BY DEPARTMENT (Sample Data):\n");
        content.append("-".repeat(40)).append("\n");
        content.append("Computer Science: 92.5%\n");
        content.append("Information Technology: 88.3%\n");
        content.append("Software Engineering: 94.1%\n");

        return content.toString();
    }

    private String generateDynamicPerformanceReport() {
        StringBuilder content = new StringBuilder();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT 
                    CASE 
                        WHEN gpa >= 3.5 THEN 'Excellent'
                        WHEN gpa >= 3.0 THEN 'Good'
                        WHEN gpa >= 2.0 THEN 'Average'
                        ELSE 'Poor'
                    END as performance_level,
                    COUNT(*) as student_count,
                    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM students WHERE gpa > 0), 1) as percentage
                FROM students 
                WHERE gpa > 0
                GROUP BY performance_level
                ORDER BY 
                    CASE performance_level
                        WHEN 'Excellent' THEN 1
                        WHEN 'Good' THEN 2
                        WHEN 'Average' THEN 3
                        WHEN 'Poor' THEN 4
                    END
            """;

            content.append("PERFORMANCE DISTRIBUTION\n");
            content.append("-".repeat(40)).append("\n");

            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String level = rs.getString("performance_level");
                    int count = rs.getInt("student_count");
                    double percent = rs.getDouble("percentage");
                    content.append(String.format("%-12s %-6d students (%.1f%%)%n", level, count, percent));
                }
            }

            String sql2 = """
                SELECT 
                    c.course_name,
                    COUNT(DISTINCT sr.student_id) as student_count,
                    AVG(sr.degree) as avg_degree,
                    ROUND(AVG(sr.degree), 1) as avg_percentage
                FROM student_results sr
                JOIN courses c ON sr.course_id = c.course_id
                GROUP BY c.course_id, c.course_name
                ORDER BY avg_degree DESC
                LIMIT 10
            """;

            content.append("\nCOURSE PERFORMANCE:\n");
            content.append("-".repeat(60)).append("\n");

            try (PreparedStatement pstmt = conn.prepareStatement(sql2);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String course = rs.getString("course_name");
                    int count = rs.getInt("student_count");
                    double avg = rs.getDouble("avg_percentage");
                    content.append(String.format("%-25s %-6d students Avg: %.1f%%%n", course, count, avg));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            content.append("Error retrieving performance data: ").append(e.getMessage());
        }

        return content.toString();
    }

    private String generateDynamicEnrollmentReport() {
        StringBuilder content = new StringBuilder();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql1 = """
                SELECT level, COUNT(*) as student_count
                FROM students 
                WHERE status = 'Active'
                GROUP BY level
                ORDER BY level
            """;

            content.append("ENROLLMENT BY LEVEL\n");
            content.append("-".repeat(30)).append("\n");

            try (PreparedStatement pstmt = conn.prepareStatement(sql1);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String level = rs.getString("level");
                    int count = rs.getInt("student_count");
                    content.append(String.format("%-12s %-6d students%n", level, count));
                }
            }

            String sql2 = """
                SELECT department, COUNT(*) as student_count
                FROM students 
                WHERE status = 'Active'
                GROUP BY department
                ORDER BY student_count DESC
            """;

            content.append("\nENROLLMENT BY DEPARTMENT\n");
            content.append("-".repeat(40)).append("\n");

            try (PreparedStatement pstmt = conn.prepareStatement(sql2);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String dept = rs.getString("department");
                    int count = rs.getInt("student_count");
                    content.append(String.format("%-25s %-6d students%n", dept, count));
                }
            }

            String sql3 = """
                SELECT 
                    c.course_name,
                    COUNT(e.enrollment_id) as enrollment_count
                FROM courses c
                LEFT JOIN enrollments e ON c.course_id = e.course_id
                WHERE c.status = 'Active'
                GROUP BY c.course_id, c.course_name
                ORDER BY enrollment_count DESC
                LIMIT 10
            """;

            content.append("\nTOP COURSES BY ENROLLMENT\n");
            content.append("-".repeat(40)).append("\n");

            try (PreparedStatement pstmt = conn.prepareStatement(sql3);
                 ResultSet rs = pstmt.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    String course = rs.getString("course_name");
                    int count = rs.getInt("enrollment_count");
                    content.append(String.format("%d. %-30s %-6d enrollments%n", rank++, course, count));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            content.append("Error retrieving enrollment data: ").append(e.getMessage());
        }

        return content.toString();
    }

    private String generateDynamicStudentTranscript() {
        StringBuilder content = new StringBuilder();

        content.append("STUDENT TRANSCRIPT\n");
        content.append("-".repeat(40)).append("\n");
        content.append("Enter student code in search field to generate transcript.\n\n");

        String studentCode = searchField.getText().trim();
        if (!studentCode.isEmpty()) {
            content.append("Transcript for Student: ").append(studentCode).append("\n");
            content.append("-".repeat(40)).append("\n");

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql1 = """
                    SELECT u.full_name, s.level, s.department, s.gpa
                    FROM students s
                    JOIN users u ON s.student_id = u.user_id
                    WHERE s.student_code = ?
                """;

                try (PreparedStatement pstmt = conn.prepareStatement(sql1)) {
                    pstmt.setString(1, studentCode);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        String name = rs.getString("full_name");
                        String level = rs.getString("level");
                        String dept = rs.getString("department");
                        double gpa = rs.getDouble("gpa");

                        content.append(String.format("Name: %s%n", name));
                        content.append(String.format("Level: %s%n", level));
                        content.append(String.format("Department: %s%n", dept));
                        content.append(String.format("GPA: %.2f%n%n", gpa));

                        String sql2 = """
                            SELECT c.course_name, sr.degree, sr.grade
                            FROM student_results sr
                            JOIN courses c ON sr.course_id = c.course_id
                            WHERE sr.student_id = ?
                            ORDER BY c.course_name
                        """;

                        try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                            pstmt2.setString(1, studentCode);
                            ResultSet rs2 = pstmt2.executeQuery();

                            content.append("COURSE RESULTS:\n");
                            content.append("-".repeat(50)).append("\n");

                            while (rs2.next()) {
                                String course = rs2.getString("course_name");
                                double degree = rs2.getDouble("degree");
                                String grade = rs2.getString("grade");
                                content.append(String.format("%-30s %-6.1f%% %-12s%n", course, degree, grade));
                            }
                        }
                    } else {
                        content.append("Student not found with code: ").append(studentCode);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                content.append("Error retrieving transcript: ").append(e.getMessage());
            }
        } else {
            content.append("Please enter a student code in the search field above.");
        }

        return content.toString();
    }

    private String generateDynamicClassPerformance() {
        StringBuilder content = new StringBuilder();
        content.append("CLASS PERFORMANCE REPORT\n");
        content.append("-".repeat(40)).append("\n");
        content.append("Note: Class performance tracking coming soon.\n");
        content.append("This report will show detailed class data when implemented.\n\n");

        content.append("SAMPLE CLASS PERFORMANCE:\n");
        content.append("-".repeat(40)).append("\n");
        content.append("Class CS101 - JAVA Programming: Average GPA 3.7\n");
        content.append("Class CS201 - Data Structures: Average GPA 3.5\n");

        return content.toString();
    }

    private String generateDynamicDepartmentStats() {
        StringBuilder content = new StringBuilder();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT department, COUNT(*) as student_count, AVG(gpa) as avg_gpa
                FROM students 
                WHERE status = 'Active' AND gpa > 0
                GROUP BY department
                ORDER BY avg_gpa DESC
            """;

            content.append("DEPARTMENT STATISTICS\n");
            content.append("-".repeat(40)).append("\n");

            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String dept = rs.getString("department");
                    int count = rs.getInt("student_count");
                    double gpa = rs.getDouble("avg_gpa");
                    content.append(String.format("%-25s %-10d Avg GPA: %.2f%n", dept, count, gpa));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            content.append("Error retrieving department stats: ").append(e.getMessage());
        }

        return content.toString();
    }

    private void updateStatistics() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql1 = "SELECT COUNT(*) as total FROM students WHERE status = 'Active'";
            try (PreparedStatement pstmt = conn.prepareStatement(sql1);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) totalStudentsLabel.setText(rs.getString("total"));
            }

            String sql2 = "SELECT AVG(gpa) as avg FROM students WHERE gpa > 0";
            try (PreparedStatement pstmt = conn.prepareStatement(sql2);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) averageGPALabel.setText(String.format("%.2f", rs.getDouble("avg")));
            }

            String sql3 = "SELECT COUNT(*) as total, SUM(CASE WHEN gpa >= 2.0 THEN 1 ELSE 0 END) as success FROM students WHERE gpa > 0";
            try (PreparedStatement pstmt = conn.prepareStatement(sql3);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int success = rs.getInt("success");
                    double rate = total > 0 ? (success * 100.0 / total) : 0;
                    successRateLabel.setText(String.format("%.1f%%", rate));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            totalStudentsLabel.setText("N/A");
            averageGPALabel.setText("N/A");
            successRateLabel.setText("N/A");
        }
    }

    private void loadScene(String fxmlFile) {
        try {
            URL resource = getClass().getResource("/View/" + fxmlFile);
            if (resource == null) {
                System.err.println("FXML not found: /View/" + fxmlFile);
                showAlert("Error", "Page not found: /View/" + fxmlFile);
                return;
            }

            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load page.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String error, String s) {

    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private class XSSFWorkbook {
    }
}