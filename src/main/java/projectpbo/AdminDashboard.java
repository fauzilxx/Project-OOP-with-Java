package projectpbo;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AdminDashboard {

    private final Stage stage;

    public AdminDashboard(Stage stage) { this.stage = stage; }

    public Parent build() {
        BorderPane root = new BorderPane();
        root.setPrefSize(1280, 800);
        root.setTop(createHeader());
        root.setCenter(createBody());
        root.setStyle("-fx-background-color: #f4f7f9;");
        return root;
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(12, 32, 12, 32));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06),12,0,0,4);");

        HBox branding = new HBox(12);
        branding.setAlignment(Pos.CENTER_LEFT);

        ImageView logo = new ImageView();
        try {
            // Menggunakan hospital-logo.jpg karena hospitallogo.png tidak ditemukan di assets
            String logoPath = getClass().getResource("/assets/hospital-logo.jpg").toExternalForm();
            logo.setImage(new Image(logoPath));
            logo.setFitHeight(40);
            logo.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("Logo header tidak ditemukan: " + e.getMessage());
        }

        Label title = new Label("NASIHUY HOSPITAL");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#052b4c"));

        branding.getChildren().addAll(logo, title);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox logoutBtn = new HBox(8);
        logoutBtn.setAlignment(Pos.CENTER);
        logoutBtn.setPadding(new Insets(8, 16, 8, 16));
        logoutBtn.setStyle("-fx-background-color: #fee2e2; -fx-background-radius: 8; -fx-cursor: hand;");

        ImageView lIcon = createIcon("/assets/logout-icon.png", 18, "#dc2626");
        Label lText = new Label("Logout");
        lText.setTextFill(Color.web("#dc2626"));
        lText.setFont(Font.font("System", FontWeight.BOLD, 13));

        logoutBtn.getChildren().addAll(lIcon, lText);

        logoutBtn.setOnMouseEntered(e -> {
            logoutBtn.setStyle("-fx-background-color: #fecaca; -fx-background-radius: 8; -fx-cursor: hand;");
            smoothScale(logoutBtn, 1.05);
        });
        logoutBtn.setOnMouseExited(e -> {
            logoutBtn.setStyle("-fx-background-color: #fee2e2; -fx-background-radius: 8; -fx-cursor: hand;");
            smoothScale(logoutBtn, 1.0);
        });
        logoutBtn.setOnMouseClicked(e -> {
            if (stage != null && stage.getScene() != null) {
                stage.getScene().setRoot(LoginView.createRoot(stage));
            }
        });

        header.getChildren().addAll(title, spacer, logoutBtn);
        return header;
    }

    private VBox createBody() {
        VBox body = new VBox(32);
        body.setPadding(new Insets(32, 48, 32, 48));
        body.setAlignment(Pos.TOP_CENTER);

        GridPane grid = new GridPane();
        grid.setHgap(32);
        grid.setVgap(32);
        grid.setAlignment(Pos.CENTER);

        grid.add(createDashboardCard("bed.png", "PASIEN RAWAT INAP", String.valueOf(Inpatient.count()), "Pasien", "#3b82f6", () -> {
            if (stage != null && stage.getScene() != null) {
                stage.getScene().setRoot(InpatientView.createRoot(stage));
            }
        }), 0, 0);
        grid.add(createDashboardCard("stetoschope.png", "PASIEN RAWAT JALAN", String.valueOf(Outpatient.count()), "Pasien", "#10b981", () -> {
            if (stage != null && stage.getScene() != null) {
                stage.getScene().setRoot(OutpatientView.createRoot(stage));
            }
        }), 1, 0);
        grid.add(createDashboardCard("calendar.png", "ANTRIAN HARI INI", String.valueOf(Queue.countWaiting()), "Menunggu", "#f97316", () -> {
            if (stage != null && stage.getScene() != null) {
                stage.getScene().setRoot(QueueView.createRoot(stage));
            }
        }), 2, 0);
        grid.add(createDashboardCard("door_open.png", "KETERSEDIAAN KAMAR", String.valueOf(calculateTotalAvailableBeds()), "Kamar Tersedia", "#6366f1", () -> {
            if (stage != null && stage.getScene() != null) {
                stage.getScene().setRoot(RoomBookingView.createRoot(stage));
            }
        }), 0, 1);
        grid.add(createDashboardCard("pills.png", "PEMESANAN OBAT", String.valueOf(DrugOrder.countToday()), "Pesanan Hari Ini", "#8b5cf6", () -> {
            if (stage != null && stage.getScene() != null) {
                stage.getScene().setRoot(DrugOrderView.createRoot(stage));
            }
        }), 1, 1);
        grid.add(createDashboardCard("wallet.png", "PENDAPATAN", formatCompactCurrency(Revenue.getTodayRevenue()), "Hari Ini", "#ef4444", () -> {
            if (stage != null && stage.getScene() != null) {
                stage.getScene().setRoot(RevenueView.createRoot(stage));
            }
        }), 2, 1);

        HBox bottom = new HBox(32);
        bottom.setAlignment(Pos.CENTER);
        VBox patientList = createPatientList();
        VBox doctorList = createDoctorList();
        HBox.setHgrow(patientList, Priority.ALWAYS);
        HBox.setHgrow(doctorList, Priority.ALWAYS);
        bottom.getChildren().addAll(patientList, doctorList);

        body.getChildren().addAll(grid, bottom);
        VBox.setVgrow(bottom, Priority.ALWAYS);
        return body;
    }

    private String formatCompactCurrency(long value) {
        if (value >= 1_000_000_000) {
            return String.format("Rp %.1f M", value / 1_000_000_000.0);
        } else if (value >= 1_000_000) {
            return String.format("Rp %.1f Jt", value / 1_000_000.0);
        } else if (value >= 1_000) {
            return String.format("Rp %.1f Rb", value / 1_000.0);
        }
        return "Rp " + value;
    }

    private int calculateTotalAvailableBeds() {
        int totalAvailable = 0;
        for (RoomBookingView.RoomType rt : RoomBookingView.getAvailableRooms()) {
            int occupied = RoomBooking.getOccupiedBeds(rt.name);
            totalAvailable += Math.max(0, rt.totalBeds - occupied);
        }
        return totalAvailable;
    }

    private VBox createDashboardCard(String iconName, String title, String value, String subtitle, String iconColor, Runnable action) {
        VBox card = new VBox(8);
        card.setPrefSize(290, 140);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-cursor:hand;");
        DropShadow baseShadow = new DropShadow(15, Color.rgb(0, 0, 0, 0.05));
        DropShadow hoverShadow = new DropShadow(28, Color.rgb(0, 0, 0, 0.18));
        card.setEffect(baseShadow);

        ImageView icon = createIcon("/assets/" + iconName, 24, iconColor);
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        titleLabel.setTextFill(Color.web("#6b7280"));
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        valueLabel.setTextFill(Color.web("#1f2937"));
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("System", 12));
        subtitleLabel.setTextFill(Color.web("#9ca3af"));
        HBox valueBox = new HBox(8, valueLabel, subtitleLabel);
        valueBox.setAlignment(Pos.BASELINE_LEFT);
        VBox textContent = new VBox(4, titleLabel, valueBox);
        HBox content = new HBox(16, icon, textContent);
        content.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(content);

        card.setOnMouseClicked(e -> {
            if (action != null) {
                action.run();
            }
        });
        card.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER && action != null) {
                action.run();
            }
        });
        card.setOnMouseEntered(e -> {
            card.setEffect(hoverShadow);
            smoothScale(card, 1.02);
        });
        card.setOnMouseExited(e -> {
            card.setEffect(baseShadow);
            smoothScale(card, 1.0);
        });
        card.setFocusTraversable(true);
        return card;
    }

    private VBox createPatientList() {
        ObservableList<String> patients = FXCollections.observableArrayList();
        // Combine inpatient + outpatient patients from DB
        for (Inpatient ip : Inpatient.fetchAll()) {
            patients.add(ip.getName() + " - " + ip.getPatientNumber());
        }
        for (Outpatient op : Outpatient.fetchAll()) {
            patients.add(op.getName() + " - " + op.getPatientNumber());
        }
        return createTitledList("DAFTAR PASIEN RUMAH SAKIT", "Cari daftar pasien rumah sakit...", patients);
    }

    private VBox createDoctorList() {
        ObservableList<String> doctors = FXCollections.observableArrayList();
        for (Doctor d : Doctor.fetchAll()) {
            doctors.add(d.toString());
        }
        return createTitledList("DAFTAR DOKTER AKTIF HARI INI", "Cari daftar dokter aktif hari ini...", doctors);
    }

    private VBox createTitledList(String title, String searchPrompt, ObservableList<String> items) {
        VBox container = new VBox(12);
        container.setStyle("-fx-background-color:white; -fx-padding:20; -fx-background-radius:12;");
        container.setEffect(new DropShadow(15, Color.rgb(0, 0, 0, 0.05)));
        VBox.setVgrow(container, Priority.ALWAYS);
        Label label = new Label(title);
        label.setFont(Font.font("System", FontWeight.BOLD, 14));
        label.setTextFill(Color.web("#1f2937"));

        StackPane searchBox = new StackPane();
        TextField searchField = new TextField();
        searchField.setPromptText(searchPrompt);
        searchField.setPadding(new Insets(0, 0, 0, 36));
        searchField.setPrefHeight(40);
        searchField.setStyle("-fx-background-radius:8; -fx-border-radius:8; -fx-background-color:#f3f4f6; -fx-border-color:#e5e7eb;");
        ImageView searchIcon = createIcon("/assets/search_icon.png", 14, "#9ca3af");
        StackPane.setAlignment(searchIcon, Pos.CENTER_LEFT);
        StackPane.setMargin(searchIcon, new Insets(0, 0, 0, 12));
        searchBox.getChildren().addAll(searchField, searchIcon);
        searchBox.setOnMouseEntered(e -> searchField.setStyle("-fx-background-radius:8; -fx-border-radius:8; -fx-background-color:#edf2f7; -fx-border-color:#cbd5e1;"));
        searchBox.setOnMouseExited(e -> searchField.setStyle("-fx-background-radius:8; -fx-border-radius:8; -fx-background-color:#f3f4f6; -fx-border-color:#e5e7eb;"));

        FilteredList<String> filtered = new FilteredList<>(items, p -> true);
        searchField.textProperty().addListener((obs, old, val) -> filtered.setPredicate(it -> val == null || val.isBlank() || it.toLowerCase().contains(val.toLowerCase())));

        ListView<String> listView = new ListView<>(filtered);
        listView.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-padding:8px;");
                } else {
                    setText(item);
                    setFont(Font.font(13));
                    setStyle("-fx-padding:8px; -fx-border-width:0 0 1 0; -fx-border-color:#f3f4f6;");
                }
            }
        });
        VBox.setVgrow(listView, Priority.ALWAYS);
        container.getChildren().addAll(label, searchBox, listView);
        return container;
    }

    private ImageView createIcon(String path, double size, String color) {
        ImageView icon = new ImageView();
        try {
            if (getClass().getResource(path) != null) {
                Image image = new Image(getClass().getResourceAsStream(path));
                icon.setImage(image);
                icon.setFitWidth(size);
                icon.setFitHeight(size);
                icon.setPreserveRatio(true);
                icon.setSmooth(true);
                javafx.scene.effect.Lighting lighting = new javafx.scene.effect.Lighting();
                lighting.setDiffuseConstant(1.0);
                lighting.setSpecularConstant(0.0);
                lighting.setSpecularExponent(0.0);
                lighting.setSurfaceScale(0.0);
                lighting.setLight(new javafx.scene.effect.Light.Distant(45, 45, Color.web(color)));
                icon.setEffect(lighting);
            } else {
                System.out.println("Icon not found: " + path);
            }
        } catch (Exception e) {
            System.err.println("Failed to load icon: " + path);
            e.printStackTrace();
        }
        return icon;
    }

    private void addNavHover(ImageView iv) {
        iv.setStyle("-fx-cursor:hand;");
        javafx.scene.effect.Effect original = iv.getEffect();
        DropShadow shadow = new DropShadow(12, Color.rgb(0, 0, 0, 0.18));
        iv.setOnMouseEntered(e -> {
            iv.setEffect(shadow);
            smoothScale(iv, 1.08);
        });
        iv.setOnMouseExited(e -> {
            iv.setEffect(original);
            smoothScale(iv, 1.0);
        });
    }

    private void smoothScale(javafx.scene.Node node, double target) {
        ScaleTransition st = new ScaleTransition(Duration.millis(300), node);
        st.setToX(target);
        st.setToY(target);
        st.setInterpolator(Interpolator.EASE_BOTH);
        st.play();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

