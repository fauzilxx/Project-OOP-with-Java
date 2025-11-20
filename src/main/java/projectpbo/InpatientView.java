package projectpbo;

import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    public InpatientView(Stage stage) { this.stage = stage; }

    public static Parent createRoot(Stage stage) { return new InpatientView(stage).build(); }

    private Parent build() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f7fa;");
        root.setTop(buildHeader());
        root.setCenter(buildContent());
        return root;
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setPadding(new Insets(14,32,14,32));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06),12,0,0,4);");
        Label title = new Label("PASIEN RAWAT INAP");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#0f172a"));

        Button backBtn = new Button("← Kembali");
        backBtn.setStyle(primaryTextButton());
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

        TableColumn<Inpatient,String> nameCol = new TableColumn<>("Nama");
        nameCol.setCellValueFactory(c -> c.getValue().nameProperty());
        TableColumn<Inpatient,String> numberCol = new TableColumn<>("Nomor Pasien");
        numberCol.setCellValueFactory(c -> c.getValue().patientNumberProperty());
        TableColumn<Inpatient,String> illnessCol = new TableColumn<>("Penyakit");
        illnessCol.setCellValueFactory(c -> c.getValue().illnessProperty());
        TableColumn<Inpatient,String> roomCol = new TableColumn<>("Ruangan");
        roomCol.setCellValueFactory(c -> c.getValue().roomProperty());
        TableColumn<Inpatient,String> doctorCol = new TableColumn<>("Dokter Penanggung");
        doctorCol.setCellValueFactory(c -> c.getValue().doctorProperty());
        table.getColumns().addAll(nameCol, numberCol, illnessCol, roomCol, doctorCol);

        // Data
        seedData();
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
        TextField fRoom = makeSmallField("Ruangan");
        TextField fDoctor = makeSmallField("Dokter");
        Button addBtn = new Button("Tambah"); addBtn.setStyle(primaryButton());
        Button editBtn = new Button("Edit"); editBtn.setStyle(primaryButton());
        Button saveBtn = new Button("Simpan"); saveBtn.setStyle(primaryButton()); saveBtn.setDisable(true);

        addBtn.setOnMouseEntered(e -> addBtn.setStyle("-fx-background-color:linear-gradient(to right,#38bdf8,#0ea5e9); -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8; -fx-cursor: hand;"));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(primaryButton()));
        editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color:linear-gradient(to right,#38bdf8,#0ea5e9); -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8; -fx-cursor: hand;"));
        editBtn.setOnMouseExited(e -> editBtn.setStyle(primaryButton()));
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle("-fx-background-color:linear-gradient(to right,#38bdf8,#0ea5e9); -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8; -fx-cursor: hand;"));
        saveBtn.setOnMouseExited(e -> saveBtn.setStyle(primaryButton()));

        addBtn.setOnAction(e -> addPatient(fName, fNumber, fIllness, fRoom, fDoctor));
        editBtn.setOnAction(e -> {
            Inpatient sel = table.getSelectionModel().getSelectedItem();
            if(sel!=null){
                fName.setText(sel.getName());
                fNumber.setText(sel.getPatientNumber());
                fIllness.setText(sel.getIllness());
                fRoom.setText(sel.getRoom());
                fDoctor.setText(sel.getDoctor());
                saveBtn.setDisable(false);
            }
        });
        saveBtn.setOnAction(e -> {
            Inpatient sel = table.getSelectionModel().getSelectedItem();
            if(sel!=null){
                // update properties
                sel.nameProperty().set(fName.getText());
                sel.patientNumberProperty().set(fNumber.getText());
                sel.illnessProperty().set(fIllness.getText());
                sel.roomProperty().set(fRoom.getText());
                sel.doctorProperty().set(fDoctor.getText());
                table.refresh();
                fName.clear(); fNumber.clear(); fIllness.clear(); fRoom.clear(); fDoctor.clear();
                saveBtn.setDisable(true);
            }
        });
        form.getChildren().addAll(fName,fNumber,fIllness,fRoom,fDoctor,addBtn,editBtn,saveBtn);

        // Delete button
        Button delBtn = new Button("Hapus Terpilih");
        delBtn.setStyle(dangerButton());
        delBtn.setOnMouseEntered(e -> delBtn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8; -fx-cursor: hand;"));
        delBtn.setOnMouseExited(e -> delBtn.setStyle(dangerButton()));
        delBtn.setOnAction(e -> {
            Inpatient sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) masterData.remove(sel);
        });

        // Allow Enter key to trigger show (focus table)
        searchField.setOnKeyPressed(k -> { if (k.getCode()== KeyCode.ENTER) table.requestFocus(); });

        // Hover styling for form fields
        Stream.of(fName, fNumber, fIllness, fRoom, fDoctor).forEach(tf -> {
            tf.setOnMouseEntered(e -> tf.setStyle("-fx-background-radius:8; -fx-border-radius:8; -fx-background-color:#e2e8f0; -fx-border-color:#cbd5e1;"));
            tf.setOnMouseExited(e -> tf.setStyle(fieldStyle()));
        });

        box.getChildren().addAll(searchField, table, form, delBtn);
        return box;
    }

    private void addPatient(TextField fName, TextField fNumber, TextField fIllness, TextField fRoom, TextField fDoctor) {
        if (fName.getText().isBlank() || fNumber.getText().isBlank()) return;
        masterData.add(new Inpatient(fName.getText(), fNumber.getText(), fIllness.getText(), fRoom.getText(), fDoctor.getText()));
        fName.clear(); fNumber.clear(); fIllness.clear(); fRoom.clear(); fDoctor.clear();
    }

    private void seedData() {
        if (!masterData.isEmpty()) return;
        masterData.addAll(
            new Inpatient("Ahmad Wijaya", "RM001234", "Demam Berdarah", "A-12", "Dr. Salim"),
            new Inpatient("Siti Nurhaliza", "RM001235", "Infeksi Paru", "B-07", "Dr. Kejora"),
            new Inpatient("Budi Santoso", "RM001236", "Patah Tulang", "Ortho-03", "Dr. Dewi"),
            new Inpatient("Dewi Lestari", "RM001237", "Pneumonia", "C-05", "Dr. Rahman"),
            new Inpatient("Eko Prasetyo", "RM001238", "COVID-19", "Isolasi-02", "Dr. Fadli"),
            new Inpatient("Guntur Wibowo", "RM001240", "Gagal Ginjal", "Dialisis-01", "Dr. Gita")
        );
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

    // Data model inner class (could be moved to separate file later)
    public static class Inpatient {
        final javafx.beans.property.SimpleStringProperty name;
        final javafx.beans.property.SimpleStringProperty patientNumber;
        final javafx.beans.property.SimpleStringProperty illness;
        final javafx.beans.property.SimpleStringProperty room;
        final javafx.beans.property.SimpleStringProperty doctor;
        public Inpatient(String name, String patientNumber, String illness, String room, String doctor) {
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.patientNumber = new javafx.beans.property.SimpleStringProperty(patientNumber);
            this.illness = new javafx.beans.property.SimpleStringProperty(illness);
            this.room = new javafx.beans.property.SimpleStringProperty(room);
            this.doctor = new javafx.beans.property.SimpleStringProperty(doctor);
        }
        public String getName() { return name.get(); }
        public javafx.beans.property.StringProperty nameProperty() { return name; }
        public String getPatientNumber() { return patientNumber.get(); }
        public javafx.beans.property.StringProperty patientNumberProperty() { return patientNumber; }
        public String getIllness() { return illness.get(); }
        public javafx.beans.property.StringProperty illnessProperty() { return illness; }
        public String getRoom() { return room.get(); }
        public javafx.beans.property.StringProperty roomProperty() { return room; }
        public String getDoctor() { return doctor.get(); }
        public javafx.beans.property.StringProperty doctorProperty() { return doctor; }
        public boolean matches(String q) {
            if (q.isBlank()) return true;
            String all = (getName()+" "+getPatientNumber()+" "+getIllness()+" "+getRoom()+" "+getDoctor()).toLowerCase();
            return all.contains(q);
        }
    }
}
