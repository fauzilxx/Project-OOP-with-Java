package projectpbo;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static void main(String[] args) {
        // Inisialisasi database dan tabel saat aplikasi mulai
        DBConnection.createTables();
        DBConnection.migrateToNaturalKeys();
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Parent root = LoginView.createRoot(stage);
        Scene scene = new Scene(root, 1200, 650);
        stage.setScene(scene);
        stage.setTitle("Nasihuy Hospital");
        try {
            if (getClass().getResource("/assets/hospital-logo.jpg") != null) {
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/assets/hospital-logo.jpg")));
            }
        } catch (Exception e) {
            System.err.println("Gagal load icon aplikasi: " + e.getMessage());
        }
        stage.show();
    }
}
