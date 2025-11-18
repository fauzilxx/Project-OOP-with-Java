package projectpbo;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Parent root = LoginView.createRoot(stage);
        Scene scene = new Scene(root, 1200, 650);
        stage.setScene(scene);
        stage.setTitle("Nasihuy Hospital");
        stage.show();
    }
}
