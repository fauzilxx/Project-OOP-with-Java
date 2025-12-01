package projectpbo;

import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class InpatientView {

    private final Stage stage;
    private final ObservableList<Inpatient> masterData = FXCollections.observableArrayList();
    private ContextMenu doctorSuggestions = new ContextMenu();

    public InpatientView(Stage stage) {
        this.stage = stage;
    }

    public static Parent createRoot(Stage stage) {
        return new InpatientView(stage).build();
    }

    private Parent build() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f7fa;");
        root.setTop(buildHeader());
        root.setCenter(buildContent());
        return root;
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setPadding(new Insets(14, 32, 14, 32));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06),12,0,0,4);");
        Label title = new Label("PASIEN RAWAT INAP");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#0f172a"));

        Button backBtn = new Button("← Kembali");
        backBtn.setStyle(primaryTextButton());
        backBtn.setCursor(javafx.scene.Cursor.HAND);
        backBtn.setOnAction(e -> stage.getScene().setRoot(new AdminDashboard(stage).build()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(backBtn, spacer, title);
        return header;
    }

    private VBox buildContent() {
        VBox box = new VBox(18);
        box.setPadding(new Insets(24));
        box.setAlignment(Pos.TOP_LEFT);

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Cari pasien...");
        searchField.setPrefHeight(40);
        searchField.setStyle(fieldStyle());
        searchField.setOnMouseEntered(e -> searchField.setStyle("-fx-background-radius:8; -fx-border-radius:8; -fx-background-color:#e2e8f0; -fx-border-color:#cbd5e1;"));
        searchField.setOnMouseExited(e -> searchField.setStyle(fieldStyle()));

        // Table
        TableView<Inpatient> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(480);
        table.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius:12;");
        table.setOnMouseEntered(e -> table.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #94a3b8; -fx-border-radius:12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18),18,0,0,4);"));
        table.setOnMouseExited(e -> table.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius:12;"));

        TableColumn<Inpatient, String> nameCol = new TableColumn<>("Nama");
        nameCol.setCellValueFactory(c -> c.getValue().nameProperty());
        TableColumn<Inpatient, String> numberCol = new TableColumn<>("Nomor Pasien");
        numberCol.setCellValueFactory(c -> c.getValue().patientNumberProperty());
        TableColumn<Inpatient, String> illnessCol = new TableColumn<>("Penyakit");
        illnessCol.setCellValueFactory(c -> c.getValue().illnessProperty());
        TableColumn<Inpatient, String> roomCol = new TableColumn<>("Ruangan");
        roomCol.setCellValueFactory(c -> c.getValue().roomProperty());
        TableColumn<Inpatient, String> doctorCol = new TableColumn<>("Dokter Penanggung");
        doctorCol.setCellValueFactory(c -> c.getValue().doctorProperty());
        TableColumn<Inpatient, String> addrCol = new TableColumn<>("Alamat");
        addrCol.setCellValueFactory(c -> c.getValue().addressProperty());
        table.getColumns().addAll(nameCol, numberCol, illnessCol, roomCol, doctorCol, addrCol);

        // Load data from database (now via Inpatient static methods)
        masterData.setAll(Inpatient.fetchAll());
        FilteredList<Inpatient> filtered = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, old, val) -> {
            String q = val == null ? "" : val.toLowerCase();
            filtered.setPredicate(ip -> ip.matches(q));
        });
        SortedList<Inpatient> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);

        // Form add / edit
        HBox form = new HBox(10);
        form.setAlignment(Pos.CENTER_LEFT);
        TextField fName = makeSmallField("Nama");
        TextField fNumber = makeSmallField("Nomor");
        TextField fIllness = makeSmallField("Penyakit");
        TextField fDoctor = makeSmallField("Dokter");

        // Autocomplete logic
        fDoctor.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                doctorSuggestions.hide();
                return;
            }
            
            String lower = newVal.toLowerCase();
            java.util.List<Doctor> matches = Doctor.fetchAll().stream()
                .filter(d -> d.getName().toLowerCase().contains(lower))
                .collect(java.util.stream.Collectors.toList());
                
            if (matches.isEmpty()) {
                doctorSuggestions.hide();
            } else {
                doctorSuggestions.getItems().clear();
                for (Doctor d : matches) {
                    MenuItem item = new MenuItem(d.toString());
                    item.setOnAction(e -> {
                        fDoctor.setText(d.getName());
                        doctorSuggestions.hide();
                    });
                    doctorSuggestions.getItems().add(item);
                }
                if (!doctorSuggestions.isShowing()) {
                    doctorSuggestions.show(fDoctor, Side.BOTTOM, 0, 0);
                }
            }
        });

        TextField fAddress = makeSmallField("Alamat");
        Button addBtn = new Button("Tambah");
        addBtn.setStyle(primaryButton());
        Button editBtn = new Button("Edit");
        editBtn.setStyle(primaryButton());
        Button saveBtn = new Button("Simpan");
        saveBtn.setStyle(primaryButton());
        saveBtn.setDisable(true);

        addBtn.setOnMouseEntered(e -> addBtn.setStyle("-fx-background-color:linear-gradient(to right,#38bdf8,#0ea5e9); -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8; -fx-cursor: hand;"));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(primaryButton()));
        editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color:linear-gradient(to right,#38bdf8,#0ea5e9); -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8; -fx-cursor: hand;"));
        editBtn.setOnMouseExited(e -> editBtn.setStyle(primaryButton()));
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle("-fx-background-color:linear-gradient(to right,#38bdf8,#0ea5e9); -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8; -fx-cursor: hand;"));
        saveBtn.setOnMouseExited(e -> saveBtn.setStyle(primaryButton()));

        addBtn.setOnAction(e -> addPatient(fName, fNumber, fIllness, fDoctor, fAddress));
        editBtn.setOnAction(e -> {
            Inpatient sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                fName.setText(sel.getName());
                fNumber.setText(sel.getPatientNumber());
                fNumber.setDisable(true); // Disable editing of PK
                fIllness.setText(sel.getIllness());
                fDoctor.setText(sel.getDoctor());
                fAddress.setText(sel.getAddress());
                saveBtn.setDisable(false);
            }
        });
        saveBtn.setOnAction(e -> {
            Inpatient sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                // PK cannot be changed
                sel.nameProperty().set(fName.getText());
                // sel.patientNumberProperty().set(fNumber.getText());
                sel.illnessProperty().set(fIllness.getText());
                sel.doctorProperty().set(fDoctor.getText());
                sel.addressProperty().set(fAddress.getText());
                Inpatient.update(sel);
                table.refresh();
                fName.clear();
                fNumber.clear();
                fNumber.setDisable(false);
                fIllness.clear();
                fDoctor.clear();
                fAddress.clear();
                saveBtn.setDisable(true);
            }
        });
        form.getChildren().addAll(fName, fNumber, fIllness, fDoctor, fAddress, addBtn, editBtn, saveBtn);

        // Delete button
        Button delBtn = new Button("Hapus Terpilih");
        delBtn.setStyle(dangerButton());
        delBtn.setOnMouseEntered(e -> delBtn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8; -fx-cursor: hand;"));
        delBtn.setOnMouseExited(e -> delBtn.setStyle(dangerButton()));
        delBtn.setOnAction(e -> {
            Inpatient sel = table.getSelectionModel().getSelectedItem();
            if (sel != null && Inpatient.delete(sel)) {
                masterData.remove(sel);
            }
        });

        // Allow Enter key to trigger show (focus table)
        searchField.setOnKeyPressed(k -> {
            if (k.getCode() == KeyCode.ENTER) {
                table.requestFocus();
            }
        });

        // Hover styling for form fields
        Stream.of(fName, fNumber, fIllness, fDoctor, fAddress).forEach(tf -> {
            tf.setOnMouseEntered(e -> tf.setStyle("-fx-background-radius:8; -fx-border-radius:8; -fx-background-color:#e2e8f0; -fx-border-color:#cbd5e1;"));
            tf.setOnMouseExited(e -> tf.setStyle(fieldStyle()));
        });

        box.getChildren().addAll(searchField, table, form, delBtn);
        return box;
    }

    private void addPatient(TextField fName, TextField fNumber, TextField fIllness, TextField fDoctor, TextField fAddress) {
        if (fName.getText().isBlank() || fNumber.getText().isBlank()) {
            return;
        }
        // Check for duplicate patient number
        if (Inpatient.isPatientNumberExists(fNumber.getText().trim())) {
            new Alert(Alert.AlertType.ERROR, "Nomor Pasien sudah terdaftar!", ButtonType.OK).showAndWait();
            return;
        }

        // Validate Doctor
        String doctorName = fDoctor.getText().trim();
        if (!doctorName.isEmpty() && !Doctor.exists(doctorName)) {
            new Alert(Alert.AlertType.ERROR, "Dokter tidak ada dalam daftar!", ButtonType.OK).showAndWait();
            return;
        }

        Inpatient added = Inpatient.add(fName.getText(), fNumber.getText(), fIllness.getText(), "", fDoctor.getText(), fAddress.getText());
        if (added != null) {
            masterData.add(0, added);
        }
        fName.clear();
        fNumber.clear();
        fIllness.clear();
        fDoctor.clear();
        fAddress.clear();
    }

    private TextField makeSmallField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(120);
        tf.setStyle(fieldStyle());
        return tf;
    }

    // Styling helpers
    private String fieldStyle() {
        return "-fx-background-radius:8; -fx-border-radius:8; -fx-background-color:#f1f5f9; -fx-border-color:#e2e8f0;";
    }
    private String primaryButton() {
        return "-fx-background-color:linear-gradient(to right,#0ea5e9,#0284c7); -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8;";
    }
    private String dangerButton() {
        return "-fx-background-color:#dc2626; -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8;";
    }
    private String primaryTextButton() {
        return "-fx-background-color:transparent; -fx-text-fill:#0f766e; -fx-font-weight:600;";
    }

    // Inner Inpatient class removed; using database-backed model in separate file.
}
