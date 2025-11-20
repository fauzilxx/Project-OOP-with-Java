package projectpbo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
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

public class OutpatientView {

    private final Stage stage;
    private final ObservableList<Outpatient> masterData = FXCollections.observableArrayList();

    public OutpatientView(Stage stage) { this.stage = stage; }
    public static Parent createRoot(Stage stage) { return new OutpatientView(stage).build(); }

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
        Label title = new Label("PASIEN RAWAT JALAN");
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

        TextField searchField = new TextField();
        searchField.setPromptText("Cari pasien rawat jalan...");
        searchField.setPrefHeight(40);
        searchField.setStyle(fieldStyle());

        TableView<Outpatient> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(480);
        table.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius:12;");
        table.setOnMouseEntered(e -> table.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #94a3b8; -fx-border-radius:12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18),18,0,0,4);"));
        table.setOnMouseExited(e -> table.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius:12;"));

        TableColumn<Outpatient,String> nameCol = new TableColumn<>("Nama");
        nameCol.setCellValueFactory(c -> c.getValue().nameProperty());
        TableColumn<Outpatient,String> numberCol = new TableColumn<>("Nomor Pasien");
        numberCol.setCellValueFactory(c -> c.getValue().patientNumberProperty());
        TableColumn<Outpatient,String> illnessCol = new TableColumn<>("Keluhan");
        illnessCol.setCellValueFactory(c -> c.getValue().complaintProperty());
        TableColumn<Outpatient,String> scheduleCol = new TableColumn<>("Jadwal Pemeriksaan");
        scheduleCol.setCellValueFactory(c -> c.getValue().scheduleProperty());
        TableColumn<Outpatient,String> doctorCol = new TableColumn<>("Dokter");
        doctorCol.setCellValueFactory(c -> c.getValue().doctorProperty());
        // Add columns individually to avoid generic varargs warning
        table.getColumns().add(nameCol);
        table.getColumns().add(numberCol);
        table.getColumns().add(illnessCol);
        table.getColumns().add(scheduleCol);
        table.getColumns().add(doctorCol);

        seedData();
        FilteredList<Outpatient> filtered = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, old, val) -> {
            String q = val == null ? "" : val.toLowerCase();
            filtered.setPredicate(op -> op.matches(q));
        });
        SortedList<Outpatient> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);

        HBox form = new HBox(10);
        form.setAlignment(Pos.CENTER_LEFT);
        TextField fName = makeSmallField("Nama");
        TextField fNumber = makeSmallField("Nomor");
        TextField fComplaint = makeSmallField("Keluhan");
        DatePicker fDate = new DatePicker(); fDate.setPromptText("Tanggal"); fDate.setStyle(fieldStyle());
        TextField fTime = makeSmallField("Jam (HH:MM)");
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
        addBtn.setOnAction(e -> addOutpatient(fName, fNumber, fComplaint, fDate, fTime, fDoctor));
        editBtn.setOnAction(e -> {
            Outpatient sel = table.getSelectionModel().getSelectedItem();
            if(sel!=null){
                fName.setText(sel.getName());
                fNumber.setText(sel.getPatientNumber());
                fComplaint.setText(sel.getComplaint());
                String sched = sel.getSchedule();
                if(sched!=null && sched.length()>=5){
                    String timePart = sched.substring(sched.length()-5);
                    fTime.setText(timePart);
                    String datePart = sched.substring(0, sched.length()-6);
                    try { fDate.setValue(java.time.LocalDate.parse(datePart, java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy"))); } catch(Exception ex){ fDate.setValue(null); }
                }
                fDoctor.setText(sel.getDoctor());
                saveBtn.setDisable(false);
            }
        });
        saveBtn.setOnAction(e -> {
            Outpatient sel = table.getSelectionModel().getSelectedItem();
            if(sel!=null && fDate.getValue()!=null){
                String dateStr = java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy").format(fDate.getValue());
                String timeStr = fTime.getText().trim(); if(!timeStr.matches("^[0-2]\\d:[0-5]\\d$")) timeStr="00:00";
                sel.nameProperty().set(fName.getText().trim());
                sel.patientNumberProperty().set(fNumber.getText().trim());
                sel.complaintProperty().set(fComplaint.getText().trim());
                sel.scheduleProperty().set(dateStr+" "+timeStr);
                sel.doctorProperty().set(fDoctor.getText().trim());
                table.refresh();
                fName.clear(); fNumber.clear(); fComplaint.clear(); fTime.clear(); fDoctor.clear(); fDate.setValue(null); saveBtn.setDisable(true);
            }
        });
        form.getChildren().addAll(fName,fNumber,fComplaint,fDate,fTime,fDoctor,addBtn,editBtn,saveBtn);

        Button delBtn = new Button("Hapus Terpilih"); delBtn.setStyle(dangerButton());
        delBtn.setOnMouseEntered(e -> delBtn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8; -fx-cursor: hand;"));
        delBtn.setOnMouseExited(e -> delBtn.setStyle(dangerButton()));
        delBtn.setOnAction(e -> { Outpatient sel = table.getSelectionModel().getSelectedItem(); if (sel != null) masterData.remove(sel); });

        searchField.setOnKeyPressed(k -> { if (k.getCode()== KeyCode.ENTER) table.requestFocus(); });

        box.getChildren().addAll(searchField, table, form, delBtn);
        return box;
    }

    private void addOutpatient(TextField fName, TextField fNumber, TextField fComplaint, DatePicker fDate, TextField fTime, TextField fDoctor) {
        if(fName.getText().isBlank() || fNumber.getText().isBlank() || fDate.getValue()==null || fTime.getText().isBlank()) return;
        String dateStr = java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy").format(fDate.getValue());
        String timeStr = fTime.getText().trim();
        if(!timeStr.matches("^[0-2]\\d:[0-5]\\d$")) timeStr = "00:00";
        String schedule = dateStr + " " + timeStr;
        masterData.add(new Outpatient(fName.getText().trim(), fNumber.getText().trim(), fComplaint.getText().trim(), schedule, fDoctor.getText().trim()));
        fName.clear(); fNumber.clear(); fComplaint.clear(); fTime.clear(); fDoctor.clear(); fDate.setValue(null);
    }

    private void seedData() {
        if(!masterData.isEmpty()) return;
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy");
        masterData.addAll(
            new Outpatient("Sari Utami","OP0001","Demam",fmt.format(today)+" 09:00","Dr. Salim"),
            new Outpatient("Rizky Pratama","OP0002","Batuk",fmt.format(today)+" 09:30","Dr. Kejora"),
            new Outpatient("Lina Marlina","OP0003","Pusing",fmt.format(today)+" 10:00","Dr. Dewi"),
            new Outpatient("Tono Susanto","OP0004","Nyeri Kepala",fmt.format(today)+" 10:30","Dr. Rahman"),
            new Outpatient("Maya Anindya","OP0005","Kontrol Luka",fmt.format(today.plusDays(1))+" 11:00","Dr. Fadli"),
            new Outpatient("Yoga Firdaus","OP0006","Alergi",fmt.format(today.plusDays(1))+" 11:30","Dr. Gita")
        );
    }

    private TextField makeSmallField(String prompt) {
        TextField tf = new TextField(); tf.setPromptText(prompt); tf.setPrefWidth(120); tf.setStyle(fieldStyle()); return tf;
    }

    private String fieldStyle() { return "-fx-background-radius:8; -fx-border-radius:8; -fx-background-color:#f1f5f9; -fx-border-color:#e2e8f0;"; }
    private String primaryButton() { return "-fx-background-color:linear-gradient(to right,#0ea5e9,#0284c7); -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8;"; }
    private String dangerButton() { return "-fx-background-color:#dc2626; -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8;"; }
    private String primaryTextButton() { return "-fx-background-color:transparent; -fx-text-fill:#0f766e; -fx-font-weight:600;"; }

    public static class Outpatient {
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleStringProperty patientNumber;
        private final javafx.beans.property.SimpleStringProperty complaint;
        private final javafx.beans.property.SimpleStringProperty schedule;
        private final javafx.beans.property.SimpleStringProperty doctor;
        public Outpatient(String n,String num,String comp,String sched,String doc){
            this.name = new javafx.beans.property.SimpleStringProperty(n);
            this.patientNumber = new javafx.beans.property.SimpleStringProperty(num);
            this.complaint = new javafx.beans.property.SimpleStringProperty(comp);
            this.schedule = new javafx.beans.property.SimpleStringProperty(sched);
            this.doctor = new javafx.beans.property.SimpleStringProperty(doc);
        }
        public String getName(){return name.get();} public javafx.beans.property.StringProperty nameProperty(){return name;}
        public String getPatientNumber(){return patientNumber.get();} public javafx.beans.property.StringProperty patientNumberProperty(){return patientNumber;}
        public String getComplaint(){return complaint.get();} public javafx.beans.property.StringProperty complaintProperty(){return complaint;}
        public String getSchedule(){return schedule.get();} public javafx.beans.property.StringProperty scheduleProperty(){return schedule;}
        public String getDoctor(){return doctor.get();} public javafx.beans.property.StringProperty doctorProperty(){return doctor;}
        public boolean matches(String q){ if(q.isBlank()) return true; String all=(getName()+" "+getPatientNumber()+" "+getComplaint()+" "+getSchedule()+" "+getDoctor()).toLowerCase(); return all.contains(q); }
    }
}