package projectpbo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class QuickPatientRegistrationView {
    
    private TextField nameField;
    private TextField phoneField;
    private TextField emailField;
    private TextField addressField;
    private TextArea symptomsField;
    private Label nameError;
    private Label phoneError;
    private Label emailError;
    private Label addressError;
    private Label symptomsError;
    private Button registerBtn;
    private Button backBtn;
    private final Stage hostStage;

    public static Parent createRoot(Stage stage) {
        QuickPatientRegistrationView view = new QuickPatientRegistrationView(stage);
        return view.build();
    }

    public QuickPatientRegistrationView(Stage stage) {
        this.hostStage = stage;
    }

    private Parent build() {
        HBox root = new HBox();
        root.setPrefSize(1200, 650);
        root.setMinWidth(900);
        root.setMinHeight(500);

        StackPane leftPane = createLeftPane();
        VBox rightPane = createRightPane();

        // Panel 50:50
        HBox.setHgrow(leftPane, Priority.ALWAYS);
        HBox.setHgrow(rightPane, Priority.ALWAYS);
        leftPane.setPrefWidth(600);
        rightPane.setPrefWidth(600);

        root.getChildren().addAll(leftPane, rightPane);
        return root;
    }

    private StackPane createLeftPane() {
        StackPane stack = new StackPane();
        stack.setStyle("-fx-background-color: linear-gradient(to bottom right, #075985, #06b6d4);");
        stack.setPadding(new Insets(36));

        // Card with rounded corners and shadow that will hold the image
        StackPane card = new StackPane();
        card.setPrefWidth(560);
        card.setPrefHeight(520);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 28;");
        card.setEffect(new DropShadow(24, Color.rgb(0, 0, 0, 0.18)));

        ImageView iv = new ImageView();
        iv.setFitWidth(420);
        iv.setPreserveRatio(true);
        try {
            if (QuickPatientRegistrationView.class.getResource("/assets/hospital-logo.jpg") != null) {
                String imagePath = QuickPatientRegistrationView.class.getResource("/assets/hospital-logo.jpg").toExternalForm();
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

        Label subtitle = new Label("Pendaftaran Pasien Antrian Periksa");
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
        container.setPadding(new Insets(0));
        container.setSpacing(0);
        container.setAlignment(Pos.TOP_CENTER);
        container.setStyle("-fx-background-color: #f8fafb;");

        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(40, 54, 24, 54));
        header.setStyle("-fx-background-color: #f8fafb;");

        Label mainTitle = new Label("Daftar Antrian Periksa");
        mainTitle.setFont(Font.font("System", FontWeight.BOLD, 26));
        mainTitle.setTextFill(Color.web("#0f172a"));

        Label subtitle = new Label("Lengkapi data untuk pendaftaran");
        subtitle.setFont(Font.font("System", 13));
        subtitle.setTextFill(Color.web("#64748b"));

        header.getChildren().addAll(mainTitle, subtitle);

        VBox form = new VBox(12);
        form.setMaxWidth(450);
        form.setAlignment(Pos.CENTER_LEFT);

        // Nama Pasien
        Label nameLabel = new Label("Nama Pasien *");
        nameLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
        nameLabel.setTextFill(Color.web("#1e293b"));
        nameField = new TextField();
        nameField.setPromptText("Masukkan nama lengkap");
        nameField.setPrefHeight(40);
        nameField.setPrefWidth(450);
        nameField.setStyle(fieldStyle());
        nameError = errorLabel();

        // No. Telepon
        Label phoneLabel = new Label("No. Telepon *");
        phoneLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
        phoneLabel.setTextFill(Color.web("#1e293b"));
        phoneField = new TextField();
        phoneField.setPromptText("Contoh: 081234567890");
        phoneField.setPrefHeight(40);
        phoneField.setPrefWidth(450);
        phoneField.setStyle(fieldStyle());
        phoneError = errorLabel();

        // Email
        Label emailLabel = new Label("Email *");
        emailLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
        emailLabel.setTextFill(Color.web("#1e293b"));
        emailField = new TextField();
        emailField.setPromptText("Masukkan email aktif");
        emailField.setPrefHeight(40);
        emailField.setPrefWidth(450);
        emailField.setStyle(fieldStyle());
        emailError = errorLabel();

        // Alamat
        Label addressLabel = new Label("Alamat *");
        addressLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
        addressLabel.setTextFill(Color.web("#1e293b"));
        addressField = new TextField();
        addressField.setPromptText("Alamat lengkap");
        addressField.setPrefHeight(40);
        addressField.setPrefWidth(450);
        addressField.setStyle(fieldStyle());
        addressError = errorLabel();

        // Keluhan/Gejala
        Label symptomsLabel = new Label("Keluhan/Gejala *");
        symptomsLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
        symptomsLabel.setTextFill(Color.web("#1e293b"));
        symptomsField = new TextArea();
        symptomsField.setPromptText("Contoh: Demam, batuk, sakit kepala");
        symptomsField.setPrefHeight(80);
        symptomsField.setPrefWidth(450);
        symptomsField.setWrapText(true);
        symptomsField.setStyle(fieldStyle());
        symptomsError = errorLabel();

        form.getChildren().addAll(
                nameLabel, nameField, nameError,
                phoneLabel, phoneField, phoneError,
                emailLabel, emailField, emailError,
                addressLabel, addressField, addressError,
                symptomsLabel, symptomsField, symptomsError
        );

        // Button Group
        registerBtn = new Button("Daftar Sekarang");
        registerBtn.setPrefHeight(44);
        registerBtn.setPrefWidth(450);
        registerBtn.setStyle(buttonPrimaryStyle());
        registerBtn.setOnAction(e -> handleRegistration());
        registerBtn.setCursor(Cursor.HAND);

        backBtn = new Button("Kembali ke Login");
        backBtn.setPrefHeight(44);
        backBtn.setPrefWidth(450);
        backBtn.setStyle(buttonSecondaryStyle());
        backBtn.setOnAction(e -> navigateBackToLogin());
        backBtn.setCursor(Cursor.HAND);

        VBox buttonBox = new VBox(16);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(24, 0, 36, 0));
        buttonBox.getChildren().addAll(registerBtn, backBtn);

        // Scroll untuk form
        VBox scrollContent = new VBox(0);
        scrollContent.setPadding(new Insets(0, 0, 0, 0));
        scrollContent.setAlignment(Pos.TOP_CENTER);
        scrollContent.setStyle("-fx-background-color: #f8fafb;");
        scrollContent.getChildren().addAll(form, buttonBox);

        ScrollPane scrollPane = new ScrollPane(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: #f8fafb; -fx-control-inner-background: #f8fafb;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        container.getChildren().addAll(header, scrollPane);
        return container;
    }

    private void handleRegistration() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String symptoms = symptomsField.getText().trim();

        boolean valid = true;

        // Validasi Nama
        if (name.isEmpty() || name.length() < 3) {
            nameError.setText("Nama minimal 3 karakter");
            nameError.setVisible(true);
            valid = false;
        } else {
            nameError.setVisible(false);
        }

        // Validasi No. Telepon
        if (phone.isEmpty() || !phone.matches("\\d{10,13}")) {
            phoneError.setText("No. telepon harus 10-13 digit angka");
            phoneError.setVisible(true);
            valid = false;
        } else {
            phoneError.setVisible(false);
        }

        // Validasi Email (WAJIB)
        if (email.isEmpty()) {
            emailError.setText("Email harus diisi");
            emailError.setVisible(true);
            valid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            emailError.setText("Email format tidak valid");
            emailError.setVisible(true);
            valid = false;
        } else {
            emailError.setVisible(false);
        }

        // Validasi Alamat
        if (address.isEmpty() || address.length() < 5) {
            addressError.setText("Alamat minimal 5 karakter");
            addressError.setVisible(true);
            valid = false;
        } else {
            addressError.setVisible(false);
        }

        // Validasi Keluhan
        if (symptoms.isEmpty() || symptoms.length() < 3) {
            symptomsError.setText("Keluhan minimal 3 karakter");
            symptomsError.setVisible(true);
            valid = false;
        } else {
            symptomsError.setVisible(false);
        }

        if (!valid) {
            return;
        }

        // Show loading state
        registerBtn.setDisable(true);
        registerBtn.setText("Sedang Memproses...");
        backBtn.setDisable(true);
        
        // Run in background thread
        new Thread(() -> {
            try {
                String patientNumber = generatePatientNumber();
                int estimatedWaitTime = calculateEstimatedWaitTime();
                
                insertPatientToQueue(name, phone, email, address, symptoms, patientNumber);
                
                // Kirim email
                sendRegistrationEmail(email, name, patientNumber, estimatedWaitTime);
                
                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    showSuccess("Berhasil Terdaftar", 
                        "Nomor Pasien: " + patientNumber + "\n" +
                        "Perkiraan waktu tunggu: " + estimatedWaitTime + " menit\n" +
                        "Email konfirmasi telah dikirim ke: " + email);
                    
                    // Clear fields
                    nameField.clear();
                    phoneField.clear();
                    emailField.clear();
                    addressField.clear();
                    symptomsField.clear();

                    // Reset button state
                    registerBtn.setDisable(false);
                    registerBtn.setText("Daftar Sekarang");
                    backBtn.setDisable(false);

                    // Kembali ke login
                    navigateBackToLogin();
                });
            } catch (SQLException e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Gagal Mendaftar", "Database Error: " + e.getMessage());
                    registerBtn.setDisable(false);
                    registerBtn.setText("Daftar Sekarang");
                    backBtn.setDisable(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Gagal Mendaftar", "Terjadi kesalahan: " + e.getMessage());
                    registerBtn.setDisable(false);
                    registerBtn.setText("Daftar Sekarang");
                    backBtn.setDisable(false);
                });
            }
        }).start();
    }

    private void insertPatientToQueue(String name, String phone, String email, String address, String symptoms, String patientNumber) throws SQLException {
        Connection conn = DBConnection.getConnection();
        
        String sql = "INSERT INTO queues (patient_number, patient_name, phone, email, address, symptoms, status, queue_number, registration_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int queueNumber = getNextQueueNumber();
            
            pstmt.setString(1, patientNumber);
            pstmt.setString(2, name);
            pstmt.setString(3, phone);
            pstmt.setString(4, email);
            pstmt.setString(5, address);
            pstmt.setString(6, symptoms);
            pstmt.setString(7, "Menunggu");
            pstmt.setInt(8, queueNumber);
            pstmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            
            pstmt.executeUpdate();
        }
    }

    private int getNextQueueNumber() throws SQLException {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT COALESCE(MAX(queue_number), 0) + 1 as next_number FROM queues WHERE DATE(registration_time) = CURDATE()";
        
        try (var stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("next_number");
            }
            return 1;
        }
    }

    private String generatePatientNumber() throws SQLException {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(patient_number, 3) AS UNSIGNED)), 0) + 1 as next_num FROM queues";
        
        try (var stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int nextNum = rs.getInt("next_num");
                return String.format("AP%03d", nextNum);
            }
            return "AP001";
        }
    }

    private int calculateEstimatedWaitTime() throws SQLException {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT COUNT(*) as pending_count FROM queues WHERE status = 'Menunggu' AND DATE(registration_time) = CURDATE()";
        
        try (var stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int pendingCount = rs.getInt("pending_count");
                return pendingCount * 5; // 5 menit per pasien
            }
            return 5;
        }
    }

    private void sendRegistrationEmail(String toEmail, String patientName, String patientNumber, int waitTime) {
        try {
            String subject = "Konfirmasi Pendaftaran - Nasihuy Hospital";
            String htmlBody = "<html>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6;'>" +
                "<div style='background-color: #f0f9ff; padding: 20px; border-radius: 8px;'>" +
                "<h2 style='color: #075985;'>Selamat Datang di Nasihuy Hospital</h2>" +
                "<p>Halo <b>" + patientName + "</b>,</p>" +
                "<p>Terima kasih telah melakukan pendaftaran. Berikut detail pendaftaran Anda:</p>" +
                "<div style='background-color: white; padding: 15px; border-left: 4px solid #0ea5e9; margin: 15px 0;'>" +
                "<p><b>Nomor Pasien:</b> <span style='font-size: 18px; color: #0369a1;'>" + patientNumber + "</span></p>" +
                "<p><b>Perkiraan Waktu Tunggu:</b> " + waitTime + " menit</p>" +
                "<p><b>Waktu Pendaftaran:</b> " + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "</p>" +
                "</div>" +
                "<p>Silahkan datang ke rumah sakit dan tunjukkan nomor pasien Anda kepada petugas di bagian pendaftaran.</p>" +
                "<p style='color: #666; font-size: 12px;'><i>Email ini dikirimkan secara otomatis. Jangan membalas email ini.</i></p>" +
                "</div>" +
                "</body>" +
                "</html>";
            
            AccountService.sendRegistrationConfirmation(toEmail, subject, htmlBody);
            System.out.println("Email konfirmasi terkirim ke: " + toEmail);
        } catch (Exception e) {
            System.err.println("Gagal mengirim email: " + e.getMessage());
        }
    }

    private void navigateBackToLogin() {
        if (hostStage != null && hostStage.getScene() != null) {
            hostStage.getScene().setRoot(LoginView.createRoot(hostStage));
        }
    }

    private Label errorLabel() {
        Label label = new Label();
        label.setTextFill(Color.web("#c92a2a"));
        label.setFont(Font.font(12));
        label.setVisible(false);
        label.setPadding(new Insets(4, 0, 0, 0));
        return label;
    }

    private String fieldStyle() {
        return "-fx-border-color: #e2e8f0; " +
               "-fx-border-radius: 8; " +
               "-fx-padding: 10; " +
               "-fx-font-size: 13; " +
               "-fx-text-fill: #0f172a; " +
               "-fx-control-inner-background: white; " +
               "-fx-focus-color: #0ea5e9; " +
               "-fx-faint-focus-color: rgba(14, 165, 233, 0.2);";
    }

    private String buttonPrimaryStyle() {
        return "-fx-background-color: linear-gradient(to right, #0ea5e9, #0284c7); " +
               "-fx-text-fill: white; " +
               "-fx-font-size: 14; " +
               "-fx-font-weight: bold; " +
               "-fx-border-radius: 8; " +
               "-fx-cursor: hand; " +
               "-fx-effect: dropshadow(gaussian, rgba(14, 165, 233, 0.3), 8, 0, 0, 2);";
    }

    private String buttonSecondaryStyle() {
        return "-fx-background-color: #e2e8f0; " +
               "-fx-text-fill: #334155; " +
               "-fx-font-size: 14; " +
               "-fx-font-weight: bold; " +
               "-fx-border-radius: 8; " +
               "-fx-cursor: hand;";
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
