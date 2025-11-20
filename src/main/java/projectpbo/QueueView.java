package projectpbo;

import javafx.collections.FXCollections;
import java.util.Arrays;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class QueueView {
    private final Stage stage;
    private final ObservableList<QueueEntry> masterData = FXCollections.observableArrayList();

    public QueueView(Stage stage){ this.stage = stage; }
    public static Parent createRoot(Stage stage){ return new QueueView(stage).build(); }

    private Parent build(){
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:#f5f7fa;");
        root.setTop(buildHeader());
        root.setCenter(buildContent());
        return root;
    }

    private HBox buildHeader(){
        HBox header = new HBox(16);
        header.setPadding(new Insets(14,32,14,32));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:white; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.06),12,0,0,4);");
        Label title = new Label("ANTRIAN PEMERIKSAAN PASIEN");
        title.setFont(Font.font("System", FontWeight.BOLD,16));
        title.setTextFill(Color.web("#0f172a"));
        Button backBtn = new Button("← Kembali"); backBtn.setStyle(primaryTextButton());
        backBtn.setOnAction(e -> stage.getScene().setRoot(new AdminDashboard(stage).build()));
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(backBtn, spacer, title);
        return header;
    }

    private VBox buildContent(){
        VBox box = new VBox(18); box.setPadding(new Insets(24)); box.setAlignment(Pos.TOP_LEFT);

        TextField searchField = new TextField(); searchField.setPromptText("Cari pasien antrian..."); searchField.setPrefHeight(40); searchField.setStyle(fieldStyle());

        TableView<QueueEntry> table = new TableView<>(); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS); table.setPrefHeight(520);
        table.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-border-color:#e2e8f0; -fx-border-radius:12;");
        table.setOnMouseEntered(e -> table.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-border-color:#94a3b8; -fx-border-radius:12; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.18),18,0,0,4);"));
        table.setOnMouseExited(e -> table.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-border-color:#e2e8f0; -fx-border-radius:12;"));

        TableColumn<QueueEntry,String> nameCol = new TableColumn<>("Nama"); nameCol.setCellValueFactory(c -> c.getValue().nameProperty());
        TableColumn<QueueEntry,String> numberCol = new TableColumn<>("Nomor Pasien"); numberCol.setCellValueFactory(c -> c.getValue().patientNumberProperty());
        TableColumn<QueueEntry,String> complaintCol = new TableColumn<>("Keluhan"); complaintCol.setCellValueFactory(c -> c.getValue().complaintProperty());
        TableColumn<QueueEntry,String> timeCol = new TableColumn<>("Waktu Masuk"); timeCol.setCellValueFactory(c -> c.getValue().arrivalTimeProperty());
        TableColumn<QueueEntry,String> statusCol = new TableColumn<>("Status"); statusCol.setCellValueFactory(c -> c.getValue().statusProperty());
        table.getColumns().add(nameCol); table.getColumns().add(numberCol); table.getColumns().add(complaintCol); table.getColumns().add(timeCol); table.getColumns().add(statusCol);

        seedData();
        FilteredList<QueueEntry> filtered = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs,old,val)->{
            String q = val==null?"":val.toLowerCase();
            filtered.setPredicate(en -> en.matches(q));
        });
        SortedList<QueueEntry> sorted = new SortedList<>(filtered); sorted.comparatorProperty().bind(table.comparatorProperty()); table.setItems(sorted);

        HBox form = new HBox(10); form.setAlignment(Pos.CENTER_LEFT);
        TextField fName = makeField("Nama",120); TextField fNumber = makeField("Nomor",110); TextField fComplaint = makeField("Keluhan",140); TextField fTime = makeField("Waktu (HH:MM)",110);
        ComboBox<String> fStatus = new ComboBox<>(FXCollections.observableArrayList("Menunggu","Diproses","Selesai")); fStatus.setValue("Menunggu"); fStatus.setPrefWidth(110); fStatus.setStyle(fieldStyle());
        Button addBtn = new Button("Tambah"); Button editBtn = new Button("Edit"); Button saveBtn = new Button("Simpan"); Button doneBtn = new Button("Tandai Selesai");
        Arrays.asList(addBtn,editBtn,saveBtn,doneBtn).forEach(b -> { b.setStyle(primaryButton()); b.setOnMouseEntered(e -> b.setStyle(hoverPrimaryButton())); b.setOnMouseExited(e -> b.setStyle(primaryButton())); });
        saveBtn.setDisable(true);
        doneBtn.setStyle(dangerButton()); doneBtn.setOnMouseEntered(e -> doneBtn.setStyle(hoverDangerButton())); doneBtn.setOnMouseExited(e -> doneBtn.setStyle(dangerButton()));

        addBtn.setOnAction(e -> addQueueEntry(fName,fNumber,fComplaint,fTime,fStatus));
        editBtn.setOnAction(e -> {
            QueueEntry sel = table.getSelectionModel().getSelectedItem();
            if(sel!=null){
                fName.setText(sel.getName()); fNumber.setText(sel.getPatientNumber()); fComplaint.setText(sel.getComplaint());
                fTime.setText(sel.getArrivalTime().substring(sel.getArrivalTime().length()-5));
                fStatus.setValue(sel.getStatus()); saveBtn.setDisable(false);
            }
        });
        saveBtn.setOnAction(e -> {
            QueueEntry sel = table.getSelectionModel().getSelectedItem();
            if(sel!=null){
                if(fName.getText().isBlank() || fNumber.getText().isBlank()) return;
                String timeRaw = fTime.getText().trim(); if(!timeRaw.matches("^[0-2]\\d:[0-5]\\d$")) timeRaw="00:00";
                String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy"));
                sel.nameProperty().set(fName.getText().trim());
                sel.patientNumberProperty().set(fNumber.getText().trim());
                sel.complaintProperty().set(fComplaint.getText().trim());
                sel.arrivalTimeProperty().set(today+" "+timeRaw);
                sel.statusProperty().set(fStatus.getValue());
                table.refresh(); saveBtn.setDisable(true);
                fName.clear(); fNumber.clear(); fComplaint.clear(); fTime.clear(); fStatus.setValue("Menunggu");
            }
        });
        doneBtn.setOnAction(e -> {
            QueueEntry sel = table.getSelectionModel().getSelectedItem(); if(sel!=null){ sel.statusProperty().set("Selesai"); table.refresh(); }
        });
        form.getChildren().addAll(fName,fNumber,fComplaint,fTime,fStatus,addBtn,editBtn,saveBtn,doneBtn);

        Button delBtn = new Button("Hapus Terpilih"); delBtn.setStyle(dangerButton()); delBtn.setOnMouseEntered(e -> delBtn.setStyle(hoverDangerButton())); delBtn.setOnMouseExited(e -> delBtn.setStyle(dangerButton()));
        delBtn.setOnAction(e -> { QueueEntry sel = table.getSelectionModel().getSelectedItem(); if(sel!=null) masterData.remove(sel); });

        searchField.setOnKeyPressed(k -> { if(k.getCode()== KeyCode.ENTER) table.requestFocus(); });

        box.getChildren().addAll(searchField, table, form, delBtn);
        return box;
    }

    private void addQueueEntry(TextField fName, TextField fNumber, TextField fComplaint, TextField fTime, ComboBox<String> fStatus){
        if(fName.getText().isBlank() || fNumber.getText().isBlank()) return;
        String timeRaw = fTime.getText().trim(); if(!timeRaw.matches("^[0-2]\\d:[0-5]\\d$")) timeRaw = "00:00";
        String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        masterData.add(new QueueEntry(fName.getText().trim(), fNumber.getText().trim(), fComplaint.getText().trim(), today+" "+timeRaw, fStatus.getValue()));
        fName.clear(); fNumber.clear(); fComplaint.clear(); fTime.clear(); fStatus.setValue("Menunggu");
    }

    private void seedData(){
        if(!masterData.isEmpty()) return;
        String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        masterData.addAll(
            new QueueEntry("Rendi Saputra","Q0001","Demam",today+" 08:15","Menunggu"),
            new QueueEntry("Nisa Amalia","Q0002","Batuk",today+" 08:30","Menunggu"),
            new QueueEntry("Hendra Wijaya","Q0003","Kontrol Luka",today+" 08:45","Diproses"),
            new QueueEntry("Lala Permata","Q0004","Pusing",today+" 09:10","Menunggu")
        );
    }

    private TextField makeField(String prompt, double width){ TextField tf = new TextField(); tf.setPromptText(prompt); tf.setPrefWidth(width); tf.setStyle(fieldStyle()); return tf; }
    private String fieldStyle(){ return "-fx-background-radius:8; -fx-border-radius:8; -fx-background-color:#f1f5f9; -fx-border-color:#e2e8f0;"; }
    private String primaryButton(){ return "-fx-background-color:linear-gradient(to right,#0ea5e9,#0284c7); -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8;"; }
    private String hoverPrimaryButton(){ return "-fx-background-color:linear-gradient(to right,#38bdf8,#0ea5e9); -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8; -fx-cursor:hand;"; }
    private String dangerButton(){ return "-fx-background-color:#dc2626; -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8;"; }
    private String hoverDangerButton(){ return "-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8; -fx-cursor:hand;"; }
    private String primaryTextButton(){ return "-fx-background-color:transparent; -fx-text-fill:#0f766e; -fx-font-weight:600;"; }

    public static class QueueEntry {
        final javafx.beans.property.SimpleStringProperty name;
        final javafx.beans.property.SimpleStringProperty patientNumber;
        final javafx.beans.property.SimpleStringProperty complaint;
        final javafx.beans.property.SimpleStringProperty arrivalTime;
        final javafx.beans.property.SimpleStringProperty status;
        public QueueEntry(String n,String num,String comp,String at,String st){
            name = new javafx.beans.property.SimpleStringProperty(n);
            patientNumber = new javafx.beans.property.SimpleStringProperty(num);
            complaint = new javafx.beans.property.SimpleStringProperty(comp);
            arrivalTime = new javafx.beans.property.SimpleStringProperty(at);
            status = new javafx.beans.property.SimpleStringProperty(st);
        }
        public String getName(){ return name.get(); } public javafx.beans.property.StringProperty nameProperty(){ return name; }
        public String getPatientNumber(){ return patientNumber.get(); } public javafx.beans.property.StringProperty patientNumberProperty(){ return patientNumber; }
        public String getComplaint(){ return complaint.get(); } public javafx.beans.property.StringProperty complaintProperty(){ return complaint; }
        public String getArrivalTime(){ return arrivalTime.get(); } public javafx.beans.property.StringProperty arrivalTimeProperty(){ return arrivalTime; }
        public String getStatus(){ return status.get(); } public javafx.beans.property.StringProperty statusProperty(){ return status; }
        public boolean matches(String q){ if(q.isBlank()) return true; String all=(getName()+" "+getPatientNumber()+" "+getComplaint()+" "+getArrivalTime()+" "+getStatus()).toLowerCase(); return all.contains(q); }
    }
}