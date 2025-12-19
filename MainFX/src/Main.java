import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.setProperty("prism.allowhidpi", "false");

            // تحميل FXML – المسار صح لأن resources/View/Login.fxml موجود
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/View/Login.fxml"));
            if (loader.getLocation() == null) {
                throw new IllegalStateException("FXML file not found: /View/Login.fxml\"");
            }
            Parent root = loader.load();

            // إعداد الشاشة
            Screen screen = Screen.getPrimary();
            Scene scene = new Scene(root);

            primaryStage.setTitle("Codex University System");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.setFullScreen(false);

            primaryStage.setWidth(screen.getVisualBounds().getWidth());
            primaryStage.setHeight(screen.getVisualBounds().getHeight());
            primaryStage.centerOnScreen();

            // أيقونة (تأكد إن الصورة في resources/Images/codex_icon.png)
            primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/resources/Images/codex_icon.png")));

            // CSS
            try {
                scene.getStylesheets().add(Main.class.getResource("/resources/Styles/Dashboard.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("CSS not found: /Styles/Dashboard.css");
            }

            primaryStage.show();

            System.out.println("Application started successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Startup Error");
            alert.setHeaderText("Cannot start application");
            alert.setContentText("Error: " + e.getMessage() + "\n\nCheck:\n1. Login.fxml in src/main/resources/View\n2. Mark resources as Resources Root");
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}