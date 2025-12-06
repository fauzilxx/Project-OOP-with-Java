package projectpbo;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
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

public class LoginView {

    private TextField usernameField;
    private PasswordField passwordField;
    private TextField passwordVisibleField;
    private boolean passwordVisible = false;
    private Label usernameError;
    private Label passwordError;
    private CheckBox rememberMe;
    private final Stage hostStage;

    // Public factory method used by launcher
    public static Parent createRoot() {
        LoginView view = new LoginView(null);
        return view.build();
    }

    // Factory with stage to allow navigation on successful login
    public static Parent createRoot(Stage stage) {
        LoginView view = new LoginView(stage);
        return view.build();
    }

    public LoginView() {
        this(null);
    }

    public LoginView(Stage stage) {
        this.hostStage = stage;
    }

    private Parent build() {
        HBox root = new HBox();
        root.setPrefSize(1200, 650);
        root.setMinWidth(900);
        root.setMinHeight(500);

        StackPane leftPane = createLeftPane();
        VBox rightPane = createRightPane();

        // Panel kiri: lebar tetap, panel kanan: grow
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

        // Card with rounded corners and shadow that will hold the image
        StackPane card = new StackPane();
        card.setPrefWidth(560);    // <--- beri pref agar tidak tergantung pada children
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
            } else {
                System.out.println("Resource not found: /assets/hospital-logo.jpg");
            }
        } catch (Exception e) {
            System.err.println("Image load failed: " + e.getMessage());
        }

        iv.setPreserveRatio(true);
        iv.setSmooth(true);

        // Gambar selalu stabil, tidak mengikuti resize parent
        iv.setFitWidth(420);
        iv.setFitHeight(420);
        StackPane.setAlignment(iv, Pos.CENTER);

        card.getChildren().add(iv);

        // Caption below the card
        VBox captionBox = new VBox(10);
        captionBox.setAlignment(Pos.CENTER);
        captionBox.setPadding(new Insets(18, 0, 6, 0));

        Label title = new Label("Nasihuy Hospital");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setWrapText(true);

        Label subtitle = new Label("Selalu hadir untuk mendukung kebutuhan kesehatan Anda");
        subtitle.setTextFill(Color.web("#e6f7fb", 0.95));
        subtitle.setFont(Font.font(13));

        captionBox.getChildren().addAll(title, subtitle);

        // Decorative circles
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

        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setFillWidth(true);

        container.setMaxWidth(Double.MAX_VALUE);
        container.setMaxHeight(Double.MAX_VALUE);

        

        Label welcome = new Label("Welcome Back");
        welcome.setFont(Font.font("System", FontWeight.BOLD, 24));
        welcome.setTextFill(Color.web("#04292B"));

        Label subtitle = new Label("Sign in to access your account");
        subtitle.setTextFill(Color.web("#6b7b7d"));
        subtitle.setFont(Font.font(13));

        header.getChildren().addAll( welcome, subtitle);

        VBox form = new VBox(12);
        form.setMinWidth(360);
        form.setMaxWidth(520);
        form.setFillWidth(true);
        // Clamp form width between 360 and 520, following ~80% of container width
        form.prefWidthProperty().bind(
            Bindings.min(520,
                Bindings.max(360, container.widthProperty().multiply(0.80))
            )
        );
        VBox.setVgrow(form, Priority.ALWAYS);

        Label userLabel = new Label("Username or Email");
        userLabel.setFont(Font.font(13));
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefHeight(42);
        usernameField.setStyle(fieldDefaultStyle());
        // Field width follows the form width
        usernameField.prefWidthProperty().bind(form.widthProperty());

        usernameError = new Label();
        usernameError.setTextFill(Color.web("#c92a2a"));
        usernameError.setFont(Font.font(12));
        usernameError.setVisible(false);

        Label passLabel = new Label("Password");
        passLabel.setFont(Font.font(13));

        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(42);
        // width is managed by the password row stack binding below
        passwordField.setManaged(true);
        passwordField.setOpacity(1);
        passwordField.setStyle(fieldDefaultStyle());

        passwordVisibleField = new TextField();
        passwordVisibleField.setPromptText("Enter your password");
        passwordVisibleField.setPrefHeight(42);
        passwordVisibleField.setVisible(false);
        passwordVisibleField.setManaged(false);
        passwordVisibleField.setOpacity(0); 
        passwordVisibleField.setStyle(fieldDefaultStyle());

        ImageView eyeView = new ImageView(new Image(getClass().getResourceAsStream("/assets/visibility.png")));
        eyeView.setFitWidth(18);
        eyeView.setFitHeight(18);

        Button eyeBtn = new Button();
        eyeBtn.setGraphic(eyeView);
        eyeBtn.setStyle("-fx-background-color: transparent;");
        eyeBtn.setOnAction(e -> togglePassword(eyeView));
        eyeBtn.setCursor(Cursor.HAND);

        // Build a responsive password row (stack fills remaining width minus eye button)
        HBox passwordRow = stackPasswordFields(passwordField, passwordVisibleField, eyeBtn);
        passwordRow.setAlignment(Pos.CENTER_LEFT);
        passwordRow.setSpacing(8);
        passwordRow.setMaxWidth(Double.MAX_VALUE);
        passwordRow.prefWidthProperty().bind(form.widthProperty());

        passwordError = new Label();
        passwordError.setTextFill(Color.web("#c92a2a"));
        passwordError.setFont(Font.font(12));
        passwordError.setVisible(false);

        HBox leftRight = new HBox();
        leftRight.setAlignment(Pos.CENTER_LEFT);
        leftRight.setSpacing(8);

        rememberMe = new CheckBox("Remember me");
        rememberMe.setFont(Font.font(13));
        rememberMe.setCursor(Cursor.HAND);


        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Hyperlink forgot = new Hyperlink("Forgot password?");
        forgot.setFont(Font.font(13));
        forgot.setBorder(Border.EMPTY);
        forgot.setOnAction(e -> {
            if (hostStage != null && hostStage.getScene() != null) {
                hostStage.getScene().setRoot(ForgotPasswordView.createRoot(hostStage));
            } else {
                showInfo("Reset Password", "Navigasi tidak tersedia tanpa Stage.");
            }
        });

        leftRight.getChildren().addAll(rememberMe, spacer, forgot);

        Button loginBtn = new Button("Sign In");
        loginBtn.setPrefHeight(44);
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle(buttonMedicalStyle());
        loginBtn.setOnAction(e -> handleLogin());
        loginBtn.setCursor(Cursor.HAND);
        loginBtn.setDefaultButton(true);


        HBox registerBox = new HBox();
        registerBox.setAlignment(Pos.CENTER);
        Label noAccount = new Label("Don't have an account? ");
        Hyperlink register = new Hyperlink("Register here");
        register.setOnAction(e -> {
            if (hostStage != null && hostStage.getScene() != null) {
                hostStage.getScene().setRoot(RegisterView.createRoot(hostStage));
            } else {
                showInfo("Register", "Navigasi tidak tersedia tanpa Stage.");
            }
        });
        registerBox.getChildren().addAll(noAccount, register);

        form.getChildren().addAll(
                userLabel,
                usernameField,
                usernameError,
                passLabel,
                passwordRow,
                passwordError,
                leftRight,
                loginBtn,
                registerBox
        );

        container.getChildren().addAll(header, form);

        return container;
    }

    private void togglePassword(ImageView eyeView) {
    passwordVisible = !passwordVisible; // toggle state

    if (passwordVisible) {
        // SHOW PASSWORD
        passwordVisibleField.setText(passwordField.getText());
        passwordVisibleField.setVisible(true);
        passwordVisibleField.setManaged(true);
        passwordVisibleField.setOpacity(1);

        passwordField.setVisible(false);
        passwordField.setManaged(false);
        passwordField.setOpacity(0);

        // ganti icon ke ikon "open eye"
        eyeView.setImage(new Image(getClass().getResourceAsStream("/assets/visibility_lock.png")));

        // tetap fokus
        passwordVisibleField.requestFocus();
        passwordVisibleField.positionCaret(passwordVisibleField.getText().length());
    } else {
        // HIDE PASSWORD
        passwordField.setText(passwordVisibleField.getText());
        passwordField.setVisible(true);
        passwordField.setManaged(true);
        passwordField.setOpacity(1);

        passwordVisibleField.setVisible(false);
        passwordVisibleField.setManaged(false);
        passwordVisibleField.setOpacity(0);

        // ganti icon ke ikon "closed eye"
        eyeView.setImage(new Image(getClass().getResourceAsStream("/assets/visibility.png")));

        passwordField.requestFocus();
        passwordField.positionCaret(passwordField.getText().length());
    }
}


    private void handleLogin() {
        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String password = (passwordVisible ? passwordVisibleField.getText() : passwordField.getText());

        boolean valid = true;

        if (username.length() < 3) {
            usernameError.setText("Username must be at least 3 characters");
            usernameError.setVisible(true);
            usernameField.setStyle(fieldErrorStyle());
            valid = false;
        } else if (username.length() > 50) {
            usernameError.setText("Username must be less than 50 characters");
            usernameError.setVisible(true);
            usernameField.setStyle(fieldErrorStyle());
            valid = false;
        } else {
            usernameError.setVisible(false);
            usernameField.setStyle(fieldDefaultStyle());
        }

        if (password == null) password = "";
        if (password.length() < 6) {
            passwordError.setText("Password must be at least 6 characters");
            passwordError.setVisible(true);
            if (passwordVisible) passwordVisibleField.setStyle(fieldErrorStyle());
            else passwordField.setStyle(fieldErrorStyle());
            valid = false;
        } else if (password.length() > 100) {
            passwordError.setText("Password must be less than 100 characters");
            passwordError.setVisible(true);
            if (passwordVisible) passwordVisibleField.setStyle(fieldErrorStyle());
            else passwordField.setStyle(fieldErrorStyle());
            valid = false;
        } else {
            passwordError.setVisible(false);
            passwordField.setStyle(fieldDefaultStyle());
            passwordVisibleField.setStyle(fieldDefaultStyle());
        }

        if (!valid) {
            return;
        }

        // Verify credentials with database
        if (DBConnection.login(username, password)) {
            // Navigate to Dashboard if stage is provided
            if (hostStage != null && hostStage.getScene() != null) {
                String role = DBConnection.getUserRole(username);
                if ("admin".equalsIgnoreCase(role)) {
                    hostStage.getScene().setRoot(new AdminDashboard(hostStage).build());
                } else {
                    hostStage.getScene().setRoot(new PatientDashboard(hostStage).build());
                }
            } else {
                showInfo("Login Successful", "Welcome back, " + username + "!");
            }
            passwordField.clear();
            passwordVisibleField.clear();
        } else {
            Alert alert = createAlert(Alert.AlertType.ERROR, "Login Failed", 
                "Username atau password salah, atau akun belum terdaftar.");
            alert.showAndWait();
        }
    }

    private Alert createAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        try {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/assets/hospital-logo.jpg")));
        } catch (Exception e) {
            System.err.println("Gagal load icon: " + e.getMessage());
        }
        return alert;
    }

    private void showInfo(String title, String content) {
        Alert a = createAlert(Alert.AlertType.INFORMATION, title, content);
        a.showAndWait();
    }

    private HBox stackPasswordFields(PasswordField pf, TextField tf, Button eyeBtn) {
        StackPane stack = new StackPane();
        stack.getChildren().addAll(pf, tf);
        // Let the stack grow with its container; fields follow the stack width
        stack.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(stack, Priority.ALWAYS);
        pf.setMaxWidth(Double.MAX_VALUE);
        tf.setMaxWidth(Double.MAX_VALUE);
        pf.prefWidthProperty().bind(stack.widthProperty());
        tf.prefWidthProperty().bind(stack.widthProperty());

        HBox box = new HBox();
        HBox.setHgrow(stack, Priority.ALWAYS);
        box.getChildren().addAll(stack, eyeBtn);
        eyeBtn.setMinWidth(60);
        eyeBtn.setPrefHeight(34);
        eyeBtn.setStyle("-fx-background-color: transparent; -fx-padding: 0 8 0 8;");
        return box;
    }

    private String fieldDefaultStyle() {
        return "-fx-background-color: #fbfdfe; -fx-border-color: #d6e6e6; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 0 10 0 10; -fx-font-size: 13;";
    }

    private String fieldErrorStyle() {
        return "-fx-background-color: #fff6f6; -fx-border-color: #e06b6b; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 0 10 0 10; -fx-font-size: 13;";
    }

    private String buttonMedicalStyle() {
        return "-fx-background-radius: 10; -fx-background-insets: 0; -fx-font-weight: 600; -fx-text-fill: white; -fx-background-color: linear-gradient(to right, #27b0b0, #0c97a2); -fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.08), 6, 0.0, 0, 2);";
    }
}
