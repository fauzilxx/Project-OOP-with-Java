package projectpbo;

import java.util.regex.Pattern;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
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

public class RegisterView {

    private final Stage hostStage;

    private TextField usernameField;
    private Label usernameError;
    private TextField emailField;
    private Label emailError;
    private TextField phoneField;
    private Label phoneError;

    private PasswordField passwordField;
    private TextField passwordVisibleField;
    private Label passwordError;
    private boolean passwordVisible = false;

    private PasswordField confirmField;
    private TextField confirmVisibleField;
    private Label confirmError;
    private boolean confirmVisible = false;

    public static Parent createRoot(Stage stage) {
        return new RegisterView(stage).build();
    }

    public RegisterView(Stage stage) {
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
        } catch (Exception ignored) {
        }
        iv.setFitWidth(420);
        iv.setFitHeight(420);
        StackPane.setAlignment(iv, Pos.CENTER);
        card.getChildren().add(iv);

        VBox captionBox = new VBox(10);
        captionBox.setAlignment(Pos.CENTER);
        captionBox.setPadding(new Insets(18, 0, 6, 0));
        Label title = new Label("Bergabung dengan Kami");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setWrapText(true);
        Label subtitle = new Label("Selalu hadir untuk mendukung kebutuhan kesehatan Anda");
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
        container.setSpacing(20);
        container.setAlignment(Pos.TOP_CENTER);
        container.setStyle("-fx-background-color: #f8fafb;");

        VBox header = new VBox(12);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 16, 0));
        Region logo = new Region();
        logo.setPrefSize(60, 60);
        logo.setStyle("-fx-background-radius: 16; -fx-background-color: linear-gradient(to bottom right, #0ea5e9, #0284c7); -fx-effect: dropshadow(gaussian, rgba(14,165,233,0.25), 12, 0, 0, 4);");
        Label hospital = new Label("Nasihuy Hospital");
        hospital.setTextFill(Color.WHITE);
        hospital.setFont(Font.font("System", FontWeight.BOLD, 18));
        hospital.setAlignment(Pos.CENTER);
        StackPane logoStack = new StackPane(logo, hospital);
        logoStack.setPrefSize(60, 60);
        Label title = new Label("Buat Akun Baru");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.setTextFill(Color.web("#0f172a"));
        Label sub = new Label("Lengkapi formulir untuk mendaftar");
        sub.setTextFill(Color.web("#64748b"));
        sub.setFont(Font.font("System", 13));
        header.getChildren().addAll(logoStack, title, sub);

        VBox form = new VBox(8);
        form.setMinWidth(360);
        form.setMaxWidth(520);
        form.prefWidthProperty().bind(Bindings.min(520, Bindings.max(360, container.widthProperty().multiply(0.80))));

        // Username
        Label userLabel = new Label("Username");
        userLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        userLabel.setTextFill(Color.web("#334155"));
        HBox userRow = iconFieldRow("👤", usernameField = new TextField(), "Masukkan username");
        usernameError = errorLabel();

        // Email
        Label emailLabel = new Label("Email");
        emailLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        emailLabel.setTextFill(Color.web("#334155"));
        HBox emailRow = iconFieldRow("✉", emailField = new TextField(), "Masukkan email");
        emailError = errorLabel();

        // Phone
        Label phoneLabel = new Label("Nomor HP");
        phoneLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        phoneLabel.setTextFill(Color.web("#334155"));
        HBox phoneRow = iconFieldRow("☎", phoneField = new TextField(), "Contoh: 081234567890");
        phoneError = errorLabel();

        // Password
        Label passLabel = new Label("Password");
        passLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        passLabel.setTextFill(Color.web("#334155"));
        passwordField = new PasswordField();
        passwordField.setPromptText("Masukkan password");
        passwordVisibleField = new TextField();
        passwordVisibleField.setPromptText("Masukkan password");
        HBox passRow = passwordRow(passwordField, passwordVisibleField, true);
        passwordError = errorLabel();

        // Confirm
        Label confLabel = new Label("Konfirmasi Password");
        confLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        confLabel.setTextFill(Color.web("#334155"));
        confirmField = new PasswordField();
        confirmField.setPromptText("Ulangi password");
        confirmVisibleField = new TextField();
        confirmVisibleField.setPromptText("Ulangi password");
        HBox confRow = passwordRow(confirmField, confirmVisibleField, false);
        confirmError = errorLabel();

        Button registerBtn = new Button("Daftar Sekarang");
        registerBtn.setCursor(Cursor.HAND);
        registerBtn.setPrefHeight(44);
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setStyle(buttonPrimary());
        registerBtn.setOnAction(e -> handleRegister());
        registerBtn.setDefaultButton(true);

        // login link
        HBox loginRow = new HBox(4);
        loginRow.setAlignment(Pos.CENTER);
        Label have = new Label("Sudah punya akun?");
        Hyperlink goLogin = new Hyperlink("Login di sini");
        goLogin.setBorder(Border.EMPTY);
        goLogin.setOnAction(e -> navigateLogin());
        loginRow.getChildren().addAll(have, new Label(" "), goLogin);

        Label footer = new Label("© 2025 Nasihuy Hospital All rights reserved.");
        footer.setFont(Font.font(11));
        footer.setTextFill(Color.web("#9aa5a6"));
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(12, 0, 0, 0));


        form.getChildren().addAll(
                userLabel, userRow, usernameError,
                emailLabel, emailRow, emailError,
                phoneLabel, phoneRow, phoneError,
                passLabel, passRow, passwordError,
                confLabel, confRow, confirmError,
                registerBtn,
                loginRow
        );

        // Bungkus header dan form dengan ScrollPane untuk scroll ability
        VBox scrollContent = new VBox();
        scrollContent.setSpacing(20);
        scrollContent.getChildren().addAll(header, form);
        
        ScrollPane scrollPane = new ScrollPane(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: #f8fafb; -fx-control-inner-background: #f8fafb; -fx-font-size: 13; -fx-padding: 0;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        container.getChildren().addAll(scrollPane, footer);
        return container;
    }

    private void navigateLogin() {
        if (hostStage != null && hostStage.getScene() != null) {
            hostStage.getScene().setRoot(LoginView.createRoot(hostStage));
        }
    }

    private void handleRegister() {
        String username = text(usernameField);
        String email = text(emailField);
        String phone = text(phoneField);
        String pass = getPasswordValue(passwordField, passwordVisibleField, passwordVisible);
        String conf = getPasswordValue(confirmField, confirmVisibleField, confirmVisible);

        boolean valid = true;

        if (!isValidUsername(username)) {
            usernameError.setText("Username 3-50 karakter, huruf/angka/_/.-");
            usernameError.setVisible(true);
            setError(usernameField);
            valid = false;
        } else {
            clearError(usernameField, usernameError);
        }

        if (!isValidEmail(email)) {
            emailError.setText("Masukkan email yang valid");
            emailError.setVisible(true);
            setError(emailField);
            valid = false;
        } else {
            clearError(emailField, emailError);
        }

        if (!isValidPhone(phone)) {
            phoneError.setText("Nomor HP 10-15 digit");
            phoneError.setVisible(true);
            setError(phoneField);
            valid = false;
        } else {
            clearError(phoneField, phoneError);
        }

        if (!isStrongPassword(pass)) {
            passwordError.setText("Minimal 8 karakter, kombinasi huruf & angka");
            passwordError.setVisible(true);
            if (passwordVisible) {
                setError(passwordVisibleField);
            } else {
                setError(passwordField);
            }
            valid = false;
        } else {
            resetFieldStyles(passwordField, passwordVisibleField, passwordError);
        }

        if (!pass.equals(conf)) {
            confirmError.setText("Konfirmasi tidak cocok");
            confirmError.setVisible(true);
            if (confirmVisible) {
                setError(confirmVisibleField);
            } else {
                setError(confirmField);
            }
            valid = false;
        } else {
            resetFieldStyles(confirmField, confirmVisibleField, confirmError);
        }

        if (!valid) {
            return;
        }

        // Cek duplikasi sebelum insert
        if (DBConnection.isUsernameRegistered(username)) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Registrasi Gagal");
            a.setHeaderText(null);
            a.setContentText("Username sudah terdaftar, pilih username lain.");
            a.showAndWait();
            return;
        }
        if (DBConnection.isEmailRegistered(email)) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Registrasi Gagal");
            a.setHeaderText(null);
            a.setContentText("Email sudah terdaftar, gunakan email lain.");
            a.showAndWait();
            return;
        }


        // Register user as admin automatically, simpan nomor HP
        if (DBConnection.register(username, email, phone, pass, "admin")) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Registrasi Berhasil");
            a.setHeaderText(null);
            a.setContentText("Akun " + username + " berhasil dibuat. Silakan login.");
            a.showAndWait();
            navigateLogin();
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Registrasi Gagal");
            a.setHeaderText(null);
            a.setContentText("Gagal membuat akun. Coba lagi nanti.");
            a.showAndWait();
        }
    }

    // UI helpers
    private HBox iconFieldRow(String icon, TextField field, String prompt) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setPrefHeight(50);
        box.setStyle(inputContainerStyle());
        box.setPadding(new Insets(0, 12, 0, 12));

        Label ic = new Label(icon);
        ic.setTextFill(Color.web("#64748b"));
        ic.setFont(Font.font(16));

        field.setPromptText(prompt);
        field.setPrefHeight(50);
        field.setStyle(innerFieldStyle());
        HBox.setHgrow(field, Priority.ALWAYS);

        // Focus style on container
        field.focusedProperty().addListener((obs, was, is) -> box.setStyle(is ? inputContainerFocusedStyle() : inputContainerStyle()));

        box.getChildren().addAll(ic, field);
        return box;
    }

    private HBox passwordRow(PasswordField pf, TextField tf, boolean primary) {
        ImageView eyeView = new ImageView(new Image(getClass().getResourceAsStream("/assets/visibility.png")));
        eyeView.setFitWidth(18);
        eyeView.setFitHeight(18);
        Button eyeBtn = new Button();
        eyeBtn.setGraphic(eyeView);
        eyeBtn.setStyle("-fx-background-color: transparent;");

        // Container styled as a single rounded rectangle
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setPrefHeight(50);
        box.setStyle(inputContainerStyle());
        box.setPadding(new Insets(0, 8, 0, 12));

        StackPane stack = new StackPane();
        pf.setPrefHeight(50);
        pf.setStyle(innerFieldStyle());
        tf.setPrefHeight(50);
        tf.setStyle(innerFieldStyle());
        tf.setVisible(false);
        tf.setManaged(false);
        tf.setOpacity(0);
        stack.getChildren().addAll(pf, tf);
        stack.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(stack, Priority.ALWAYS);
        pf.prefWidthProperty().bind(stack.widthProperty());
        tf.prefWidthProperty().bind(stack.widthProperty());

        eyeBtn.setOnAction(e -> {
            boolean show = primary ? (passwordVisible = !passwordVisible) : (confirmVisible = !confirmVisible);
            if (show) {
                tf.setText(pf.getText());
                tf.setVisible(true);
                tf.setManaged(true);
                tf.setOpacity(1);
                pf.setVisible(false);
                pf.setManaged(false);
                pf.setOpacity(0);
                eyeView.setImage(new Image(getClass().getResourceAsStream("/assets/visibility_lock.png")));
                tf.requestFocus();
                tf.positionCaret(tf.getText().length());
            } else {
                pf.setText(tf.getText());
                pf.setVisible(true);
                pf.setManaged(true);
                pf.setOpacity(1);
                tf.setVisible(false);
                tf.setManaged(false);
                tf.setOpacity(0);
                eyeView.setImage(new Image(getClass().getResourceAsStream("/assets/visibility.png")));
                pf.requestFocus();
                pf.positionCaret(pf.getText().length());
            }
        });

        // Focus style on container
        pf.focusedProperty().addListener((o, w, is) -> box.setStyle(is ? inputContainerFocusedStyle() : inputContainerStyle()));
        tf.focusedProperty().addListener((o, w, is) -> box.setStyle(is ? inputContainerFocusedStyle() : inputContainerStyle()));

        eyeBtn.setMinWidth(44);
        eyeBtn.setPrefWidth(44);
        eyeBtn.setFocusTraversable(false);

        box.getChildren().addAll(stack, eyeBtn);
        return box;
    }

    private Label errorLabel() {
        Label l = new Label();
        l.setTextFill(Color.web("#c92a2a"));
        l.setFont(Font.font(12));
        l.setVisible(false);
        return l;
    }

    // Validation helpers
    private boolean isValidUsername(String u) {
        if (u == null) {
            return false;
        }
        if (u.length() < 3 || u.length() > 50) {
            return false;
        }
        return u.matches("[A-Za-z0-9_.-]+");
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.compile(regex).matcher(email).matches();
    }

    private boolean isValidPhone(String p) {
        if (p == null) {
            return false;
        }
        String digits = p.replaceAll("[^0-9]", "");
        return digits.length() >= 10 && digits.length() <= 15;
    }

    private boolean isStrongPassword(String s) {
        if (s == null || s.length() < 8) {
            return false;
        }
        boolean hasLetter = s.matches(".*[A-Za-z].*");
        boolean hasDigit = s.matches(".*[0-9].*");
        return hasLetter && hasDigit;
    }

    private String text(TextField f) {
        return f.getText() == null ? "" : f.getText().trim();
    }

    private String getPasswordValue(PasswordField pf, TextField tf, boolean visible) {
        return (visible ? tf.getText() : pf.getText());
    }

    private void setError(TextField f) {
        f.setStyle(fieldErrorStyle());
    }

    private void clearError(TextField f, Label l) {
        f.setStyle(fieldDefaultStyle());
        l.setVisible(false);
    }

    private void resetFieldStyles(PasswordField pf, TextField tf, Label l) {
        pf.setStyle(fieldDefaultStyle());
        tf.setStyle(fieldDefaultStyle());
        l.setVisible(false);
    }

    private String fieldDefaultStyle() {
        return innerFieldStyle();
    }
    private String fieldErrorStyle() {
        // show error on container by using a stronger border when used directly; inner fields stay transparent
        return "-fx-background-color: #fff6f6; -fx-border-color: #e06b6b; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 0 10 0 10; -fx-font-size: 14;";
    }
    private String buttonPrimary() {
        return "-fx-background-radius: 10; -fx-background-insets: 0; -fx-font-weight: 600; -fx-font-size: 14; -fx-text-fill: white; -fx-background-color: linear-gradient(to right, #0ea5e9, #0284c7); -fx-effect: dropshadow(gaussian, rgba(14,165,233,0.25), 8, 0, 0, 4); -fx-cursor: hand;";
    }

    private String inputContainerStyle() {
        return "-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 4, 0, 0, 2);";
    }
    private String inputContainerFocusedStyle() {
        return "-fx-background-color: #ffffff; -fx-border-color: #0ea5e9; -fx-border-width: 1.5; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(14,165,233,0.15), 8, 0, 0, 3);";
    }
    private String innerFieldStyle() {
        return "-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0; -fx-font-size: 14;";
    }
}
