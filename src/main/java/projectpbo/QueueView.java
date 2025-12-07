package projectpbo;

import java.util.Arrays;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class QueueView {
    private final Stage stage;
    private final ObservableList<Queue> masterData = FXCollections.observableArrayList();

    public QueueView(Stage stage) {
        this.stage = stage;
    }

    public static Parent createRoot(Stage stage) {
        return new QueueView(stage).build();
    }

    private Parent build() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:#f5f7fa;");
        root.setTop(buildHeader());
        root.setCenter(buildContent());
        return root;
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setPadding(new Insets(14, 32, 14, 32));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:white; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.06),12,0,0,4);");
        Label title = new Label("ANTRIAN PEMERIKSAAN PASIEN");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#0f172a"));
        Button backBtn = new Button("← Kembali");
        backBtn.setStyle(primaryTextButton());
        backBtn.setCursor(Cursor.HAND);
        backBtn.setOnAction(e -> stage.getScene().setRoot(new AdminDashboard(stage).build()));
        
        header.getChildren().addAll(backBtn, title);
        return header;
    }

    private VBox buildContent() {
        VBox box = new VBox(18);
        box.setPadding(new Insets(24));
        box.setAlignment(Pos.TOP_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Cari pasien antrian...");
        searchField.setPrefHeight(40);
        searchField.setStyle(fieldStyle());

        TableView<Queue> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(520);
        table.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-border-color:#e2e8f0; -fx-border-radius:12;");
        table.setOnMouseEntered(e -> table.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-border-color:#94a3b8; -fx-border-radius:12; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.18),18,0,0,4);"));
        table.setOnMouseExited(e -> table.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-border-color:#e2e8f0; -fx-border-radius:12;"));

        TableColumn<Queue, String> nameCol = new TableColumn<>("Nama");
        nameCol.setCellValueFactory(c -> c.getValue().patientNameProperty());
        TableColumn<Queue, String> numberCol = new TableColumn<>("Nomor Pasien");
        numberCol.setCellValueFactory(c -> c.getValue().patientNumberProperty());
        TableColumn<Queue, String> complaintCol = new TableColumn<>("Keluhan");
        complaintCol.setCellValueFactory(c -> c.getValue().complaintProperty());
        TableColumn<Queue, String> timeCol = new TableColumn<>("Waktu Masuk");
        timeCol.setCellValueFactory(c -> c.getValue().arrivalTimeProperty());
        TableColumn<Queue, String> waitCol = new TableColumn<>("Waktu Tunggu");
        waitCol.setCellValueFactory(c -> c.getValue().waitingTimeProperty());
        TableColumn<Queue, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> c.getValue().statusProperty());
        table.getColumns().add(nameCol);
        table.getColumns().add(numberCol);
        table.getColumns().add(complaintCol);
        table.getColumns().add(timeCol);
        table.getColumns().add(waitCol);
        table.getColumns().add(statusCol);

        masterData.setAll(Queue.fetchAll());
        FilteredList<Queue> filtered = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, old, val) -> {
            String q = val == null ? "" : val.toLowerCase();
            filtered.setPredicate(en -> en.matches(q));
        });
        SortedList<Queue> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);

        HBox form = new HBox(10);
        form.setAlignment(Pos.CENTER_LEFT);
        TextField fName = makeField("Nama", 120);
        TextField fNumber = makeField("Nomor", 110);
        TextField fComplaint = makeField("Keluhan", 140);
        TextField fTime = makeField("Waktu (HH:MM)", 110);
        ComboBox<String> fStatus = new ComboBox<>(FXCollections.observableArrayList("Menunggu", "Diproses", "Selesai"));
        fStatus.setValue("Menunggu");
        fStatus.setPrefWidth(110);
        fStatus.setStyle(fieldStyle());
        Button addBtn = new Button("Tambah");
        Button editBtn = new Button("Edit");
        Button saveBtn = new Button("Simpan");
        Button doneBtn = new Button("Tandai Selesai");
        Arrays.asList(addBtn, editBtn, saveBtn, doneBtn).forEach(b -> {
            b.setStyle(primaryButton());
            b.setOnMouseEntered(e -> b.setStyle(hoverPrimaryButton()));
            b.setOnMouseExited(e -> b.setStyle(primaryButton()));
        });
        saveBtn.setDisable(true);
        doneBtn.setStyle(dangerButton());
        doneBtn.setOnMouseEntered(e -> doneBtn.setStyle(hoverDangerButton()));
        doneBtn.setOnMouseExited(e -> doneBtn.setStyle(dangerButton()));

        addBtn.setOnAction(e -> {
            addQueueEntry(fName, fNumber, fComplaint, fTime, fStatus);
            refreshData();
        });
        editBtn.setOnAction(e -> {
            Queue sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                fName.setText(sel.getPatientName());
                fNumber.setText(sel.getPatientNumber());
                fNumber.setDisable(true); // Disable editing of PK
                fComplaint.setText(sel.getComplaint());
                if (sel.getArrivalTime() != null && sel.getArrivalTime().length() >= 5) {
                    fTime.setText(sel.getArrivalTime().substring(sel.getArrivalTime().length() - 5));
                }
                fStatus.setValue(sel.getStatus());
                saveBtn.setDisable(false);
            }
        });
        saveBtn.setOnAction(e -> {
            Queue sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                if (fName.getText().isBlank() || fNumber.getText().isBlank()) return;
                
                // PK cannot be changed
                String timeRaw = fTime.getText().trim();
                if (!timeRaw.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) timeRaw = "00:00";
                String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy"));
                sel.patientNameProperty().set(fName.getText().trim());
                // sel.patientNumberProperty().set(fNumber.getText().trim());
                sel.complaintProperty().set(fComplaint.getText().trim());
                sel.arrivalTimeProperty().set(today + " " + timeRaw);
                sel.statusProperty().set(fStatus.getValue());
                Queue.update(sel);
                refreshData();
                saveBtn.setDisable(true);
                fName.clear();
                fNumber.clear();
                fNumber.setDisable(false);
                fComplaint.clear();
                fTime.clear();
                fStatus.setValue("Menunggu");
            }
        });
        doneBtn.setOnAction(e -> {
            Queue sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                sel.statusProperty().set("Selesai");
                Queue.update(sel);
                refreshData();
            }
        });
        form.getChildren().addAll(fName, fNumber, fComplaint, fTime, fStatus, addBtn, editBtn, saveBtn, doneBtn);

        Button delBtn = new Button("Hapus Terpilih");
        delBtn.setStyle(dangerButton());
        delBtn.setOnMouseEntered(e -> delBtn.setStyle(hoverDangerButton()));
        delBtn.setOnMouseExited(e -> delBtn.setStyle(dangerButton()));
        delBtn.setOnAction(e -> {
            Queue sel = table.getSelectionModel().getSelectedItem();
            if (sel != null && Queue.delete(sel)) {
                refreshData();
            }
        });

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle(primaryButton());
        refreshBtn.setOnAction(e -> refreshData());

        searchField.setOnKeyPressed(k -> {
            if (k.getCode() == KeyCode.ENTER) table.requestFocus();
        });

        box.getChildren().addAll(searchField, table, form, new HBox(10, delBtn, refreshBtn));
        return box;
    }

    private void refreshData() {
        masterData.setAll(Queue.fetchAll());
    }

    private void addQueueEntry(TextField fName, TextField fNumber, TextField fComplaint, TextField fTime, ComboBox<String> fStatus) {
        if (fName.getText().isBlank() || fNumber.getText().isBlank()) return;
        
        if (Queue.isPatientNumberExists(fNumber.getText().trim())) {
            Alert alert = createAlert(Alert.AlertType.ERROR, "Error", "Nomor pasien sudah digunakan!");
            alert.showAndWait();
            return;
        }

        String timeRaw = fTime.getText().trim();
        if (!timeRaw.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) timeRaw = "00:00";
        String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        java.time.LocalDateTime arrival;
        try {
            arrival = java.time.LocalDateTime.parse(today + " " + timeRaw, java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm"));
        } catch (Exception ex) {
            arrival = null;
        }
        Queue created = Queue.add(fName.getText().trim(), fNumber.getText().trim(), fComplaint.getText().trim(), arrival, "", fStatus.getValue());
        // if (created != null) masterData.add(0, created); // Removed, using refreshData() instead
        fName.clear();
        fNumber.clear();
        fComplaint.clear();
        fTime.clear();
        fStatus.setValue("Menunggu");
    }

    private TextField makeField(String prompt, double width) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(width);
        tf.setStyle(fieldStyle());
        return tf;
    }

    private String fieldStyle() {
        return "-fx-background-radius:8; -fx-border-radius:8; -fx-background-color:#f1f5f9; -fx-border-color:#e2e8f0;";
    }

    private String primaryButton() {
        return "-fx-background-color:linear-gradient(to right,#0ea5e9,#0284c7); -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8;";
    }

    private String hoverPrimaryButton() {
        return "-fx-background-color:linear-gradient(to right,#38bdf8,#0ea5e9); -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8; -fx-cursor:hand;";
    }

    private String dangerButton() {
        return "-fx-background-color:#dc2626; -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8;";
    }

    private String hoverDangerButton() {
        return "-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8; -fx-cursor:hand;";
    }

    private String primaryTextButton() {
        return "-fx-background-color:transparent; -fx-text-fill:#0f766e; -fx-font-weight:600;";
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

    // Nested QueueEntry removed; using DB-backed Queue model.
}