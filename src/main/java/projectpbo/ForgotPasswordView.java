package projectpbo;

import java.util.regex.Pattern;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ForgotPasswordView {

    private final Stage hostStage;
    private TextField emailField;
    private Label emailError;

    public static Parent createRoot(Stage stage) {
        return new ForgotPasswordView(stage).build();
    }

    public ForgotPasswordView(Stage stage) {
        this.hostStage = stage;
    }

    private Parent build() {
        HBox root = new HBox();
        root.setPrefSize(1200, 650);
        root.setMinWidth(900);
        root.setMinHeight(500);

        StackPane leftPane = createLeftPane();
        VBox rightPane = createRightPane();

        leftPane.setMinWidth(520);
        leftPane.setMaxWidth(600);
        leftPane.setPrefWidth(560);
        HBox.setHgrow(leftPane, Priority.NEVER);
        HBox.setHgrow(rightPane, Priority.ALWAYS);

        root.getChildren().addAll(leftPane, rightPane);
        return root;
    }

    private StackPane createLeftPane() {
        StackPane stack = new StackPane();
        stack.setStyle("-fx-background-color: linear-gradient(to bottom right, #075985, #06b6d4);");
        stack.setPadding(new Insets(36));

        StackPane card = new StackPane();
        card.setPrefWidth(560);
        card.setPrefHeight(520);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 28;");
        card.setEffect(new DropShadow(24, Color.rgb(0, 0, 0, 0.18)));

        ImageView iv = new ImageView();
        iv.setFitWidth(420);
        iv.setPreserveRatio(true);
        try {
            if (LoginView.class.getResource("/assets/hospital-logo.jpg") != null) {
                String imagePath = LoginView.class.getResource("/assets/hospital-logo.jpg").toExternalForm();
                Image img = new Image(imagePath, true);
                iv.setImage(img);
            }
        } catch (Exception ignored) {}
        iv.setFitWidth(420);
        iv.setFitHeight(420);
        StackPane.setAlignment(iv, Pos.CENTER);
        card.getChildren().add(iv);

        VBox captionBox = new VBox(10);
        captionBox.setAlignment(Pos.CENTER);
        captionBox.setPadding(new Insets(18, 0, 6, 0));
        Label title = new Label("Lupa Password?");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setWrapText(true);
        Label subtitle = new Label("Jangan khawatir, kami akan membantu Anda reset password");
        subtitle.setTextFill(Color.web("#e6f7fb", 0.95));
        subtitle.setFont(Font.font(13));
        captionBox.getChildren().addAll(title, subtitle);

        Region circle1 = new Region();
        circle1.setStyle("-fx-background-color: rgba(255,255,255,0.12); -fx-background-radius: 999;");
        circle1.prefWidthProperty().bind(stack.widthProperty().multiply(0.10));
        circle1.prefHeightProperty().bind(circle1.prefWidthProperty());
        StackPane.setAlignment(circle1, Pos.TOP_RIGHT);
        StackPane.setMargin(circle1, new Insets(40, 40, 0, 0));

        Region circle2 = new Region();
        circle2.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 999;");
        circle2.prefWidthProperty().bind(stack.widthProperty().multiply(0.16));
        circle2.prefHeightProperty().bind(circle2.prefWidthProperty());
        StackPane.setAlignment(circle2, Pos.BOTTOM_LEFT);
        StackPane.setMargin(circle2, new Insets(0, 0, 30, 30));

        VBox leftBox = new VBox(12, card, captionBox);
        leftBox.setAlignment(Pos.CENTER);
        stack.getChildren().addAll(leftBox, circle1, circle2);
        return stack;
    }

    private VBox createRightPane() {
        VBox container = new VBox();
        container.setPadding(new Insets(48, 54, 36, 54));
        container.setSpacing(24);
        container.setAlignment(Pos.TOP_CENTER);
        container.setStyle("-fx-background-color: #ffffff;");

        // back to login link
        HBox backRow = new HBox(8);
        backRow.setAlignment(Pos.CENTER_LEFT);
        Hyperlink back = new Hyperlink("Kembali ke Login");
        back.setBorder(Border.EMPTY);
        back.setOnAction(e -> navigateLogin());
        Label arrow = new Label("←");
        arrow.setTextFill(Color.web("#0b5ed7"));
        backRow.getChildren().addAll(arrow, back);

        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        Region logo = new Region();
        logo.setPrefSize(56, 56);
        logo.setStyle("-fx-background-radius: 14; -fx-background-color: linear-gradient(to bottom right, #1ea4ff, #0c7bd6);");
        Label hospital = new Label("Nasihuy Hospital");
        hospital.setTextFill(Color.WHITE);
        hospital.setFont(Font.font(24));
        StackPane logoStack = new StackPane(logo, hospital);
        Label title = new Label("Reset Password");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#04292B"));
        Label sub = new Label("Masukkan email Anda untuk menerima link reset password");
        sub.setTextFill(Color.web("#6b7b7d"));
        sub.setFont(Font.font(13));
        header.getChildren().addAll(logoStack, title, sub);

        VBox form = new VBox(12);
        form.setMinWidth(360);
        form.setMaxWidth(520);
        form.prefWidthProperty().bind(Bindings.min(520, Bindings.max(360, container.widthProperty().multiply(0.80))));

        Label emailLabel = new Label("Email");
        emailLabel.setFont(Font.font(13));

        // input with left icon
        HBox emailRow = new HBox(8);
        emailRow.setAlignment(Pos.CENTER_LEFT);
        emailRow.setMaxWidth(Double.MAX_VALUE);
        emailRow.prefWidthProperty().bind(form.widthProperty());
        Label mailIcon = new Label("✉");
        mailIcon.setTextFill(Color.web("#64748b"));
        mailIcon.setFont(Font.font(14));
        emailField = new TextField();
        emailField.setPromptText("Masukkan email Anda");
        emailField.setPrefHeight(42);
        emailField.setStyle(fieldDefaultStyle());
        HBox.setHgrow(emailField, Priority.ALWAYS);
        emailRow.getChildren().addAll(mailIcon, emailField);

        emailError = new Label();
        emailError.setTextFill(Color.web("#c92a2a"));
        emailError.setFont(Font.font(12));
        emailError.setVisible(false);

        Button sendBtn = new Button("Kirim Link Reset");
        sendBtn.setPrefHeight(44);
        sendBtn.setMaxWidth(Double.MAX_VALUE);
        sendBtn.setStyle(buttonPrimary());
        sendBtn.setOnAction(e -> handleSend(sendBtn));

        Label footer = new Label("© 2025 Nasihuy Hospital All rights reserved.");
        footer.setFont(Font.font(11));
        footer.setTextFill(Color.web("#9aa5a6"));
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(12, 0, 0, 0));

        form.getChildren().addAll(emailLabel, emailRow, emailError, sendBtn);
        container.getChildren().addAll(backRow, header, form, footer);
        return container;
    }

    private void navigateLogin() {
        if (hostStage != null && hostStage.getScene() != null) {
            hostStage.getScene().setRoot(LoginView.createRoot(hostStage));
        }
    }

    private void handleSend(Button sendBtn) {
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        if (!isValidEmail(email)) {
            emailError.setText("Masukkan email yang valid");
            emailError.setVisible(true);
            emailField.setStyle(fieldErrorStyle());
            return;
        }
        emailError.setVisible(false);
        emailField.setStyle(fieldDefaultStyle());

        sendBtn.setDisable(true);
        sendBtn.setText("Mengirim…");

        // Simulate async send; here we just show a confirmation
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Reset Password");
        a.setHeaderText(null);
        a.setContentText("Link reset telah dikirim ke " + email + ".");
        a.showAndWait();

        sendBtn.setDisable(false);
        sendBtn.setText("Kirim Link Reset");
    }

    private boolean isValidEmail(String email) {
        if (email.isEmpty()) return false;
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.compile(regex).matcher(email).matches();
    }

    private String fieldDefaultStyle() {
        return "-fx-background-color: #fbfdfe; -fx-border-color: #d6e6e6; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 0 10 0 10; -fx-font-size: 13;";
    }

    private String fieldErrorStyle() {
        return "-fx-background-color: #fff6f6; -fx-border-color: #e06b6b; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 0 10 0 10; -fx-font-size: 13;";
    }

    private String buttonPrimary() {
        return "-fx-background-radius: 10; -fx-background-insets: 0; -fx-font-weight: 600; -fx-text-fill: white; -fx-background-color: linear-gradient(to right, #2ea0ff, #1a73e8); -fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.08), 6, 0.0, 0, 2);";
    }
}
