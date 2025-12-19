package Controller;

import Database.DatabaseConnection;
import Model.Course;
import Model.User;
import DAO.CourseDAO;
import DAO.Impl.CourseDAOImpl;
import Utils.CurrentUser;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class StudentCoursesController {

    @FXML private Button backButton;
    @FXML private VBox coursesContainer;

    private final CourseDAO courseDAO = new CourseDAOImpl();

    @FXML
    private void initialize() {
        loadCoursesByLevel();
    }

    private void loadCoursesByLevel() {
        coursesContainer.getChildren().clear();

        User currentUser = CurrentUser.getCurrentUser();
        if (currentUser == null) {
            showAlert("Error", "Please login again.");
            return;
        }

        String studentLevel = getStudentLevel(currentUser.getUserId());
        if (studentLevel == null || studentLevel.trim().isEmpty()) {
            Label msg = new Label("No level assigned. Please contact admin.");
            msg.setStyle("-fx-font-size: 18px; -fx-text-fill: #64748b; -fx-padding: 50px;");
            coursesContainer.getChildren().add(msg);
            return;
        }

        List<Course> courses = courseDAO.getCoursesByLevel(studentLevel);

        if (courses.isEmpty()) {
            Label msg = new Label("No courses available for your level (" + studentLevel + ").");
            msg.setStyle("-fx-font-size: 18px; -fx-text-fill: #64748b; -fx-padding: 50px;");
            coursesContainer.getChildren().add(msg);
            return;
        }

        for (Course course : courses) {
            VBox courseCard = createCourseCard(course);
            coursesContainer.getChildren().add(courseCard);
        }
    }

    private String getStudentLevel(int userId) {
        String sql = "SELECT level FROM students WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("level");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private VBox createCourseCard(Course course) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        card.setOnMouseClicked(e -> openCourseDetails(course));

        VBox infoBox = new VBox(8);
        Label nameLabel = new Label(course.getCourseName().toUpperCase());
        nameLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #262451;");

        Label instructorLabel = new Label("Instructor: " + course.getInstructorName());
        instructorLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #64748b;");

        Label descriptionLabel = new Label(course.getDescription() != null ? course.getDescription() : "No description available.");
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
        descriptionLabel.setWrapText(true);

        infoBox.getChildren().addAll(nameLabel, instructorLabel, descriptionLabel);

        HBox buttonsBox = new HBox(15);
        buttonsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button openButton = new Button("📄 Open Material");
        openButton.setStyle("-fx-background-color: #262451; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 20;");
        openButton.setOnAction(e -> handleOpenFile(course));

        Button detailsButton = new Button("resources.View Details");
        detailsButton.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 20;");
        detailsButton.setOnAction(e -> openCourseDetails(course));

        buttonsBox.getChildren().addAll(openButton, detailsButton);

        card.getChildren().addAll(infoBox, buttonsBox);

        return card;
    }

    private void handleOpenFile(Course course) {
        String pdfPath = course.getPdfPath();
        if (pdfPath == null || pdfPath.isEmpty()) {
            showAlert("No Material", "No main material available for this course.");
            return;
        }

        File pdfFile = new File(pdfPath);
        if (pdfFile.exists()) {
            try {
                java.awt.Desktop.getDesktop().open(pdfFile);
            } catch (IOException e) {
                showAlert("Error", "Cannot open PDF. Install a PDF reader.");
            }
        } else {
            showAlert("File Not Found", "Material file not found on your device.");
        }
    }

    private void openCourseDetails(Course course) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Course_details.fxml"));
            Parent root = loader.load();

            CourseDetailsController controller = loader.getController();
            controller.setCourse(course);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(course.getCourseName());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open course details.");
        }
    }

    @FXML
    private void handleBack() {
        loadScene("/View/Student_dashboard.fxml", "Student Dashboard");
    }

    private void loadScene(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}