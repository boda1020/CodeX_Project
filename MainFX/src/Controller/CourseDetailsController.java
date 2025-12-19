package Controller;

import Model.Course;
import Model.Lecture;
import DAO.LectureDAO;
import DAO.Impl.LectureDAOImpl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CourseDetailsController {

    @FXML private Label courseNameLabel;
    @FXML private Label courseCodeLabel;
    @FXML private Label instructorLabel;
    @FXML private Label scheduleLabel;
    @FXML private Label roomLabel;
    @FXML private Label statusLabel;
    @FXML private Label descriptionLabel;
    @FXML private VBox lecturesContainer;
    @FXML private Button backButton;
    @FXML private Button downloadAllButton;

    private Course currentCourse;

    private final LectureDAO lectureDAO = new LectureDAOImpl();  // جديد: معرف الـ DAO

    public void setCourse(Course course) {
        this.currentCourse = course;
        loadCourseDetails();
        loadLectures(); // لو عايز المحاضرات تظهر
    }

    private void loadCourseDetails() {
        if (currentCourse == null) return;

        courseNameLabel.setText(currentCourse.getCourseName());
        courseCodeLabel.setText(currentCourse.getCourseCode() + " • " + currentCourse.getCredits() + " Credits");
        instructorLabel.setText(currentCourse.getInstructorName());
        scheduleLabel.setText(currentCourse.getSchedule());
        roomLabel.setText(currentCourse.getRoom());
        statusLabel.setText(currentCourse.getStatus());
        descriptionLabel.setText(currentCourse.getDescription() != null ? currentCourse.getDescription() : "No description available.");
    }

    private void loadLectures() {
        lecturesContainer.getChildren().clear();

        if (currentCourse == null) return;

        List<Lecture> lectures = lectureDAO.getLecturesByCourseId(currentCourse.getCourseId());

        if (lectures.isEmpty()) {
            Label noLectures = new Label("No lectures available yet.");
            noLectures.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 50px;");
            lecturesContainer.getChildren().add(noLectures);
            return;
        }

        for (Lecture lecture : lectures) {
            HBox lectureItem = new HBox(15);
            lectureItem.setAlignment(Pos.CENTER_LEFT);
            lectureItem.setStyle("-fx-padding: 15; -fx-background-color: #f8fafc; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

            Label title = new Label(lecture.getTitle());
            title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #262451;");

            Button downloadBtn = new Button("Download PDF");
            downloadBtn.setStyle("-fx-background-color: #262451; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16;");
            downloadBtn.setOnAction(e -> downloadFile(lecture.getFilePath(), lecture.getTitle() + ".pdf"));

            // إضافة Region عشان يملأ المساحة
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);  // الصحيح كده

            lectureItem.getChildren().addAll(title, spacer, downloadBtn);
            lecturesContainer.getChildren().add(lectureItem);
        }
    }

    // method جديد لتحميل الملف
    private void downloadFile(String filePath, String fileName) {
        if (filePath == null || filePath.isEmpty()) {
            showAlert("No File", "No file attached to this lecture.");
            return;
        }

        File file = new File(filePath);
        if (file.exists()) {
            try {
                java.awt.Desktop.getDesktop().open(file);
            } catch (IOException e) {
                showAlert("Error", "Cannot open file. Please install a PDF reader.");
                e.printStackTrace();
            }
        } else {
            showAlert("File Not Found", "File not found: " + fileName);
        }
    }

    @FXML
    private void handleBack() {
        try {
            // ارجع لصفحة الكورسات (StudentCourses.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/StudentCourses.fxml"));  // اسم الـ FXML بتاع My Courses
            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("My Courses");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to go back.");
        }
    }

    @FXML
    private void handleDownloadAll() {
        showAlert("Coming Soon", "Download all PDFs feature coming soon!");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}