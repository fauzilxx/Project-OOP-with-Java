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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class PatientDashboard {

    private final Stage stage;
    
    // Form fields
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

    public PatientDashboard(Stage stage) {
        this.stage = stage;
    }

    public Parent build() {
        HBox root = new HBox();
        root.setPrefSize(1200, 700);

        // Left Panel (Branding)
        StackPane leftPanel = buildLeftPanel();
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        leftPanel.setPrefWidth(600);

        // Right Panel (Form + Header)
        BorderPane rightPanel = new BorderPane();
        rightPanel.setTop(buildHeader());
        rightPanel.setCenter(buildContent());
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        rightPanel.setPrefWidth(600);
        rightPanel.setStyle("-fx-background-color: #f8fafc;");

        root.getChildren().addAll(leftPanel, rightPanel);
        return root;
    }

    private StackPane buildLeftPanel() {
        StackPane stack = new StackPane();
        stack.setStyle("-fx-background-color: linear-gradient(to bottom right, #0ea5e9, #0284c7);");
        stack.setPadding(new Insets(40));

        VBox content = new VBox(24);
        content.setAlignment(Pos.CENTER);

        // Logo
        ImageView logoView = new ImageView();
        logoView.setFitWidth(300);
        logoView.setPreserveRatio(true);
        try {
            if (getClass().getResource("/assets/hospital-logo.jpg") != null) {
                logoView.setImage(new Image(getClass().getResourceAsStream("/assets/hospital-logo.jpg")));
            }
        } catch (Exception e) {
            System.err.println("Logo not found");
        }

        // Branding Text
        Label brandTitle = new Label("Nasihuy Hospital");
        brandTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        brandTitle.setTextFill(Color.WHITE);

        Label brandSubtitle = new Label("Selalu hadir untuk mendukung kebutuhan kesehatan Anda");
        brandSubtitle.setFont(Font.font("Segoe UI", 18));
        brandSubtitle.setTextFill(Color.web("#e0f2fe"));

        content.getChildren().addAll(logoView, brandTitle, brandSubtitle);
        stack.getChildren().add(content);

        return stack;
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setPadding(new Insets(20, 40, 20, 40));
        header.setAlignment(Pos.CENTER_RIGHT); // Align to right since it's in right panel
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05),10,0,0,2);");

        Label portalLabel = new Label("Patient Portal");
        portalLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        portalLabel.setTextFill(Color.web("#64748b"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.setStyle("-fx-background-color: white; -fx-border-color: #ef4444; -fx-border-radius: 20; -fx-border-width: 1.5; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;");
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle("-fx-background-color: #ef4444; -fx-border-color: #ef4444; -fx-border-radius: 20; -fx-border-width: 1.5; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;"));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle("-fx-background-color: white; -fx-border-color: #ef4444; -fx-border-radius: 20; -fx-border-width: 1.5; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;"));
        
        logoutBtn.setOnAction(e -> {
            stage.getScene().setRoot(LoginView.createRoot(stage));
        });

        header.getChildren().addAll(portalLabel, spacer, logoutBtn);
        return header;
    }

    private VBox buildContent() {
        VBox container = new VBox(24);
        container.setPadding(new Insets(30, 40, 40, 40));
        container.setAlignment(Pos.TOP_CENTER);
        container.setStyle("-fx-background-color: #f8fafb;");

        Label mainTitle = new Label("Daftar Antrian Periksa");
        mainTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        mainTitle.setTextFill(Color.web("#0f172a"));

        Label subtitle = new Label("Silakan lengkapi formulir di bawah ini.");
        subtitle.setFont(Font.font("Segoe UI", 14));
        subtitle.setTextFill(Color.web("#64748b"));

        VBox headerBox = new VBox(8, mainTitle, subtitle);
        headerBox.setAlignment(Pos.CENTER);

        VBox form = new VBox(24);
        form.setMaxWidth(500); 
        form.setAlignment(Pos.CENTER_LEFT);
        form.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0, 0, 2);");

        // Nama Pasien
        Label nameLabel = new Label("Nama Pasien");
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        nameLabel.setStyle("-fx-text-fill: #111010ff; -fx-font-weight: bold; -fx-font-size: 16px;");
        nameLabel.setPadding(new Insets(0, 0, 6, 0));
        nameField = new TextField();
        nameField.setPromptText("Masukkan nama lengkap sesuai KTP");
        nameField.setPrefHeight(45);
        nameField.setMaxWidth(Double.MAX_VALUE);
        nameField.setStyle(fieldStyle());
        nameError = errorLabel();

        // No. Telepon
        Label phoneLabel = new Label("Nomor Telepon / WhatsApp");
        phoneLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        phoneLabel.setStyle("-fx-text-fill: #111010ff; -fx-font-weight: bold; -fx-font-size: 16px;");
        phoneLabel.setPadding(new Insets(0, 0, 6, 0));
        phoneField = new TextField();
        phoneField.setPromptText("Contoh: 081234567890");
        phoneField.setPrefHeight(45);
        phoneField.setMaxWidth(Double.MAX_VALUE);
        phoneField.setStyle(fieldStyle());
        phoneError = errorLabel();

        // Email
        Label emailLabel = new Label("Alamat Email");
        emailLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        emailLabel.setStyle("-fx-text-fill: #111010ff; -fx-font-weight: bold; -fx-font-size: 16px;");
        emailLabel.setPadding(new Insets(0, 0, 6, 0));
        emailField = new TextField();
        emailField.setPromptText("email@example.com");
        emailField.setPrefHeight(45);
        emailField.setMaxWidth(Double.MAX_VALUE);
        emailField.setStyle(fieldStyle());
        emailError = errorLabel();

        // Alamat
        Label addressLabel = new Label("Alamat Lengkap");
        addressLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        addressLabel.setStyle("-fx-text-fill: #111010ff; -fx-font-weight: bold; -fx-font-size: 16px;");
        addressLabel.setPadding(new Insets(0, 0, 6, 0));
        addressField = new TextField();
        addressField.setPromptText("Jalan, Kota, Provinsi");
        addressField.setPrefHeight(45);
        addressField.setMaxWidth(Double.MAX_VALUE);
        addressField.setStyle(fieldStyle());
        addressError = errorLabel();

        // Keluhan/Gejala
        Label symptomsLabel = new Label("Keluhan / Gejala yang Dirasakan");
        symptomsLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        symptomsLabel.setStyle("-fx-text-fill: #111010ff; -fx-font-weight: bold; -fx-font-size: 16px;");
        symptomsLabel.setPadding(new Insets(0, 0, 6, 0));
        symptomsField = new TextArea();
        symptomsField.setPromptText("Jelaskan keluhan Anda secara singkat...");
        symptomsField.setPrefHeight(100);
        symptomsField.setMaxWidth(Double.MAX_VALUE);
        symptomsField.setWrapText(true);
        symptomsField.setStyle(fieldStyle());
        symptomsError = errorLabel();

        // Button Group
        registerBtn = new Button("Ambil Nomor Antrian");
        registerBtn.setPrefHeight(50);
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setStyle(buttonPrimaryStyle());
        registerBtn.setOnAction(e -> handleRegistration());
        registerBtn.setCursor(Cursor.HAND);
        
        // Hover effect for button
        registerBtn.setOnMouseEntered(e -> registerBtn.setStyle(buttonPrimaryHoverStyle()));
        registerBtn.setOnMouseExited(e -> registerBtn.setStyle(buttonPrimaryStyle()));

        form.getChildren().addAll(
                createFieldGroup(nameLabel, nameField, nameError),
                createFieldGroup(phoneLabel, phoneField, phoneError),
                createFieldGroup(emailLabel, emailField, emailError),
                createFieldGroup(addressLabel, addressField, addressError),
                createFieldGroup(symptomsLabel, symptomsField, symptomsError),
                new Region() {{ setMinHeight(10); }},
                registerBtn
        );

        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setMaxWidth(550);
        
        // Center the scrollpane content
        VBox centerBox = new VBox(headerBox, scrollPane);
        centerBox.setAlignment(Pos.TOP_CENTER);
        centerBox.setSpacing(30);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        container.getChildren().add(centerBox);
        return container;
    }

    private VBox createFieldGroup(Label label, javafx.scene.control.Control field, Label error) {
        VBox group = new VBox(8);
        group.getChildren().addAll(label, field, error);
        return group;
    }

    private void handleRegistration() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String symptoms = symptomsField.getText().trim();

        boolean valid = true;

        // Validasi Nama
        if (name.isEmpty()) {
            nameError.setText("Nama pasien wajib diisi");
            nameError.setVisible(true);
            nameField.setStyle(fieldErrorStyle());
            valid = false;
        } else if (name.length() < 3) {
            nameError.setText("Nama minimal 3 karakter");
            nameError.setVisible(true);
            nameField.setStyle(fieldErrorStyle());
            valid = false;
        } else {
            nameError.setVisible(false);
            nameField.setStyle(fieldStyle());
        }

        // Validasi No. Telepon
        if (phone.isEmpty()) {
            phoneError.setText("Nomor telepon wajib diisi");
            phoneError.setVisible(true);
            phoneField.setStyle(fieldErrorStyle());
            valid = false;
        } else if (!phone.matches("\\d{10,13}")) {
            phoneError.setText("Nomor telepon harus 10-13 digit angka");
            phoneError.setVisible(true);
            phoneField.setStyle(fieldErrorStyle());
            valid = false;
        } else {
            phoneError.setVisible(false);
            phoneField.setStyle(fieldStyle());
        }

        // Validasi Email (WAJIB)
        if (email.isEmpty()) {
            emailError.setText("Email wajib diisi");
            emailError.setVisible(true);
            emailField.setStyle(fieldErrorStyle());
            valid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            emailError.setText("Format email tidak valid");
            emailError.setVisible(true);
            emailField.setStyle(fieldErrorStyle());
            valid = false;
        } else {
            emailError.setVisible(false);
            emailField.setStyle(fieldStyle());
        }

        // Validasi Alamat
        if (address.isEmpty()) {
            addressError.setText("Alamat lengkap wajib diisi");
            addressError.setVisible(true);
            addressField.setStyle(fieldErrorStyle());
            valid = false;
        } else if (address.length() < 5) {
            addressError.setText("Alamat minimal 5 karakter");
            addressError.setVisible(true);
            addressField.setStyle(fieldErrorStyle());
            valid = false;
        } else {
            addressError.setVisible(false);
            addressField.setStyle(fieldStyle());
        }

        // Validasi Keluhan
        if (symptoms.isEmpty()) {
            symptomsError.setText("Keluhan/Gejala wajib diisi");
            symptomsError.setVisible(true);
            symptomsField.setStyle(fieldErrorStyle());
            valid = false;
        } else if (symptoms.length() < 3) {
            symptomsError.setText("Keluhan minimal 3 karakter");
            symptomsError.setVisible(true);
            symptomsField.setStyle(fieldErrorStyle());
            valid = false;
        } else {
            symptomsError.setVisible(false);
            symptomsField.setStyle(fieldStyle());
        }

        if (!valid) {
            return;
        }

        // Show loading state
        registerBtn.setDisable(true);
        registerBtn.setText("Sedang Memproses...");
        
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
                    registerBtn.setText("Daftar Antrian");
                });
            } catch (SQLException e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Gagal Mendaftar", "Database Error: " + e.getMessage());
                    registerBtn.setDisable(false);
                    registerBtn.setText("Daftar Antrian");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Gagal Mendaftar", "Terjadi kesalahan: " + e.getMessage());
                    registerBtn.setDisable(false);
                    registerBtn.setText("Daftar Antrian");
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

    private Label errorLabel() {
        Label label = new Label();
        label.setTextFill(Color.web("#dc2626"));
        label.setFont(Font.font("Segoe UI", 13));
        label.setVisible(false);
        label.setPadding(new Insets(5, 0, 0, 0));
        label.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 13px;");
        return label;
    }

    private String fieldStyle() {
        return "-fx-background-color: #ffffff; " +
               "-fx-border-color: #94a3b8; " +
               "-fx-border-width: 1.5; " +
               "-fx-border-radius: 8; " +
               "-fx-background-radius: 8; " +
               "-fx-padding: 12; " +
               "-fx-font-size: 14; " +
               "-fx-text-fill: #000000; " +
               "-fx-prompt-text-fill: #94a3b8;";
    }

    private String fieldErrorStyle() {
        return "-fx-background-color: #fef2f2; " +
               "-fx-border-color: #dc2626; " +
               "-fx-border-width: 2; " +
               "-fx-border-radius: 8; " +
               "-fx-background-radius: 8; " +
               "-fx-padding: 12; " +
               "-fx-font-size: 14; " +
               "-fx-text-fill: #000000; " +
               "-fx-prompt-text-fill: #94a3b8;";
    }

    private String buttonPrimaryStyle() {
        return "-fx-background-color: #0ea5e9; " +
               "-fx-text-fill: white; " +
               "-fx-font-size: 16; " +
               "-fx-font-weight: bold; " +
               "-fx-background-radius: 8; " +
               "-fx-cursor: hand; " +
               "-fx-effect: dropshadow(gaussian, rgba(14, 165, 233, 0.4), 10, 0, 0, 2);";
    }

    private String buttonPrimaryHoverStyle() {
        return "-fx-background-color: #0284c7; " +
               "-fx-text-fill: white; " +
               "-fx-font-size: 16; " +
               "-fx-font-weight: bold; " +
               "-fx-background-radius: 8; " +
               "-fx-cursor: hand; " +
               "-fx-effect: dropshadow(gaussian, rgba(14, 165, 233, 0.6), 12, 0, 0, 3);";
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

    private void showSuccess(String title, String message) {
        Alert alert = createAlert(Alert.AlertType.INFORMATION, title, message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = createAlert(Alert.AlertType.ERROR, title, message);
        alert.showAndWait();
    }
}
