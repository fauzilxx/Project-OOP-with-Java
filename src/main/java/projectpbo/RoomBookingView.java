package projectpbo;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class RoomBookingView {
    private final Stage stage;
    private final ObservableList<RoomType> roomTypes = FXCollections.observableArrayList();
    private final ObservableList<RoomBooking> bookings = FXCollections.observableArrayList();
    private final FlowPane cardsPane = new FlowPane();

    public RoomBookingView(Stage stage) {
        this.stage = stage;
        initRooms();
        bookings.setAll(RoomBooking.fetchAll());
    }

    public static Parent createRoot(Stage stage) {
        return new RoomBookingView(stage).build();
    }

    public static ObservableList<RoomType> getAvailableRooms() {
        return FXCollections.observableArrayList(
                new RoomType("Kelas 1", 200_000, "6 kasur / ruang", "/assets/kelas_1.jpg", 18),
                new RoomType("Kelas 2", 300_000, "4 kasur / ruang", "/assets/kelas_2.jpg", 16),
                new RoomType("Kelas 3", 400_000, "2 kasur / ruang", "/assets/kelas_3.jpg", 12),
                new RoomType("Private", 550_000, "1 kasur, sofa panjang, meja, TV, dispenser, kulkas", "/assets/private.jpg", 3),
                new RoomType("VIP", 700_000, "1 kasur, area 2x Private, sofa panjang + sofa kecil, meja & kursi office, kasur lipat, TV, dispenser, kulkas 2 pintu", "/assets/vip.jpg", 2)
        );
    }

    private void initRooms() {
        roomTypes.addAll(getAvailableRooms());
    }

    private Parent build() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:#f5f7fa;");
        root.setTop(buildHeader());

        // Bungkus buildContent() dengan ScrollPane untuk scroll vertikal
        ScrollPane scrollPane = new ScrollPane(buildContent());
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color:#f5f7fa; -fx-background:#f5f7fa;");

        root.setCenter(scrollPane);
        return root;
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setPadding(new Insets(14, 32, 14, 32));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:white; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.06),12,0,0,4);");
        Label title = new Label("PEMESANAN KAMAR RAWAT INAP");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#0f172a"));
        Button backBtn = new Button("← Kembali");
        backBtn.setStyle(primaryTextButton());
        backBtn.setCursor(Cursor.HAND);
        backBtn.setOnAction(e -> stage.getScene().setRoot(new AdminDashboard(stage).build()));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(backBtn, spacer, title);
        return header;
    }

    private VBox buildContent() {
        VBox wrapper = new VBox(28);
        wrapper.setPadding(new Insets(28));
        wrapper.setAlignment(Pos.TOP_LEFT);
        
        cardsPane.setHgap(24);
        cardsPane.setVgap(24); // responsive: wrap length menyesuaikan lebar kontainer
        // Wrap length akan di-bind ke lebar wrapper agar kartu otomatis pindah baris saat window diperkecil
        wrapper.widthProperty().addListener((obs, oldW, newW) -> {
            double pad = 56; // padding kiri+kanan (28 + 28)
            cardsPane.setPrefWrapLength(Math.max(260, newW.doubleValue() - pad));
        });
        refreshRoomCards();

        // Form Section
        VBox formSection = new VBox(12);
        Label formTitle = new Label("Form Pemesanan Kamar");
        formTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        formTitle.setTextFill(Color.web("#1f2937"));

        HBox form = new HBox(10);
        form.setAlignment(Pos.CENTER_LEFT);
        TextField fName = makeField("Nama Pasien", 150);
        TextField fAddress = makeField("Alamat", 150);
        ComboBox<RoomType> fRoom = new ComboBox<>(roomTypes);
        fRoom.setPromptText("Pilih Kamar");
        fRoom.setPrefWidth(140);
        fRoom.setStyle(fieldStyle());
        DatePicker fCheckIn = new DatePicker(LocalDate.now());
        fCheckIn.setPromptText("Tgl Masuk");
        fCheckIn.setStyle(fieldStyle());
        fCheckIn.setPrefWidth(140);

        Button checkInBtn = new Button("Check In");
        Button editBtn = new Button("Edit");
        Button checkoutBtn = new Button("Checkout");
        Button saveBtn = new Button("Simpan");
        saveBtn.setDisable(true);

        Arrays.asList(checkInBtn, editBtn, checkoutBtn, saveBtn).forEach(b -> {
            b.setStyle(primaryButton());
            b.setOnMouseEntered(e -> b.setStyle(hoverPrimaryButton()));
            b.setOnMouseExited(e -> b.setStyle(primaryButton()));
            b.setCursor(Cursor.HAND);
        });

        form.getChildren().addAll(fName, fAddress, fRoom, fCheckIn, checkInBtn, editBtn, saveBtn, checkoutBtn);
        formSection.getChildren().addAll(formTitle, form);

        // Table Section
        VBox tableSection = new VBox(12);
        Label bookingTitle = new Label("Daftar Booking Kamar");
        bookingTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        TextField searchField = new TextField();
        searchField.setPromptText("Cari booking (nama / kamar / alamat)...");
        searchField.setPrefHeight(40);
        searchField.setStyle(fieldStyle());

        TableView<RoomBooking> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(420);
        table.setCursor(Cursor.HAND);
        table.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-border-color:#e2e8f0; -fx-border-radius:12; -fx-cursor:hand;");
        table.setOnMouseEntered(e -> table.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-border-color:#94a3b8; -fx-border-radius:12; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.18),18,0,0,4);"));
        table.setOnMouseExited(e -> table.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-border-color:#e2e8f0; -fx-border-radius:12;"));

        TableColumn<RoomBooking, String> nameCol = new TableColumn<>("Nama");
        nameCol.setCellValueFactory(c -> c.getValue().patient_nameProperty());
        TableColumn<RoomBooking, String> roomCol = new TableColumn<>("Kamar");
        roomCol.setCellValueFactory(c -> c.getValue().roomNameProperty());
        TableColumn<RoomBooking, String> addrCol = new TableColumn<>("Alamat");
        addrCol.setCellValueFactory(c -> c.getValue().addressProperty());
        TableColumn<RoomBooking, String> inCol = new TableColumn<>("Tgl Masuk");
        inCol.setCellValueFactory(c -> c.getValue().checkInProperty());
        TableColumn<RoomBooking, String> outCol = new TableColumn<>("Tgl Keluar");
        outCol.setCellValueFactory(c -> c.getValue().checkOutProperty());
        TableColumn<RoomBooking, String> daysCol = new TableColumn<>("Hari");
        daysCol.setCellValueFactory(c -> c.getValue().daysProperty());
        TableColumn<RoomBooking, String> costCol = new TableColumn<>("Biaya");
        costCol.setCellValueFactory(c -> c.getValue().costProperty());
        table.getColumns().add(nameCol);
        table.getColumns().add(roomCol);
        table.getColumns().add(addrCol);
        table.getColumns().add(inCol);
        table.getColumns().add(outCol);
        table.getColumns().add(daysCol);
        table.getColumns().add(costCol);

        FilteredList<RoomBooking> filtered = new FilteredList<>(bookings, p -> true);
        searchField.textProperty().addListener((obs, old, val) -> {
            String q = val == null ? "" : val.toLowerCase();
            // Simple match logic
            filtered.setPredicate(b -> {
                if (q.isBlank()) return true;
                String all = (b.getpatient_name() + " " + b.getRoomName() + " " + b.getAddress()).toLowerCase();
                return all.contains(q);
            });
        });
        SortedList<RoomBooking> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);

        tableSection.getChildren().addAll(bookingTitle, searchField, table);

        // Logic
        checkInBtn.setOnAction(e -> {
            if (fName.getText().isBlank() || fRoom.getValue() == null || fCheckIn.getValue() == null) {
                return;
            }

            String pName = fName.getText().trim();
            String pAddr = fAddress.getText().trim();
            RoomType selectedRoom = fRoom.getValue();

            // 1. Check if patient exists in Inpatient records
            if (!Inpatient.exists(pName)) {
                new Alert(Alert.AlertType.ERROR, "Nama pasien tidak ada di data Rawat Inap", ButtonType.OK).showAndWait();
                return;
            }

            // 2. Sync/Validate Address
            String registeredAddr = Inpatient.getAddress(pName);
            if (registeredAddr != null && !registeredAddr.isBlank() && !registeredAddr.equalsIgnoreCase(pAddr)) {
                new Alert(Alert.AlertType.ERROR, "Alamat tidak sesuai dengan data pasien (" + registeredAddr + ")", ButtonType.OK).showAndWait();
                return;
            }

            // 3. Check if patient already has an active booking
            if (RoomBooking.hasActiveBooking(pName)) {
                new Alert(Alert.AlertType.ERROR, "Pasien sudah memiliki kamar pasien (Booking Aktif)", ButtonType.OK).showAndWait();
                return;
            }

            // 4. Check Room Availability
            int occupied = RoomBooking.getOccupiedBeds(selectedRoom.name);
            if (occupied >= selectedRoom.totalBeds) {
                new Alert(Alert.AlertType.ERROR, "Kamar penuh! Silakan pilih kamar lain.", ButtonType.OK).showAndWait();
                return;
            }

            // 5. Validate Check-In Date
            if (fCheckIn.getValue().isBefore(LocalDate.now())) {
                new Alert(Alert.AlertType.ERROR, "Tanggal check-in tidak boleh sebelum hari ini!", ButtonType.OK).showAndWait();
                return;
            }

            RoomBooking added = RoomBooking.add(pName, pAddr, selectedRoom.name, selectedRoom.name, fCheckIn.getValue().toString());
            if (added != null) {
                // Update Inpatient room
                Inpatient.updateRoomByName(pName, selectedRoom.name);
                
                bookings.add(0, added);
                refreshRoomCards();
            }
            clearForm(fName, fAddress, fCheckIn, fRoom, saveBtn);
        });
        editBtn.setOnAction(e -> {
            RoomBooking sel = table.getSelectionModel().getSelectedItem();
            if (sel != null && (sel.getCheckOut() == null || sel.getCheckOut().isEmpty())) {
                loadBookingToForm(sel, fName, fAddress, fCheckIn, fRoom, saveBtn);
            }
        });
        saveBtn.setOnAction(e -> {
            RoomBooking sel = table.getSelectionModel().getSelectedItem();
            if (sel != null && (sel.getCheckOut() == null || sel.getCheckOut().isEmpty())) {
                applyEdit(sel, fName, fAddress, fCheckIn, fRoom, saveBtn);
                refreshRoomCards();
            }
        });
        checkoutBtn.setOnAction(e -> {
            RoomBooking sel = table.getSelectionModel().getSelectedItem();
            if (sel != null && (sel.getCheckOut() == null || sel.getCheckOut().isEmpty())) {
                processCheckout(sel, table);
                refreshRoomCards();
            }
        });

        wrapper.getChildren().addAll(cardsPane, formSection, tableSection);
        return wrapper;
    }

    private void clearForm(TextField n, TextField addr, DatePicker in, ComboBox<RoomType> room, Button save) {
        n.clear();
        addr.clear();
        in.setValue(LocalDate.now());
        room.setValue(null);
        save.setDisable(true);
    }

    private void loadBookingToForm(RoomBooking b, TextField n, TextField addr, DatePicker in, ComboBox<RoomType> room, Button save) {
        n.setText(b.getpatient_name());
        addr.setText(b.getAddress());
        in.setValue(LocalDate.parse(b.getCheckIn()));
        // Find room type
        roomTypes.stream().filter(rt -> rt.name.equals(b.getRoomName())).findFirst().ifPresent(room::setValue);
        save.setDisable(false);
    }

    private void applyEdit(RoomBooking b, TextField n, TextField addr, DatePicker in, ComboBox<RoomType> room, Button save) {
        if (room.getValue() == null || in.getValue() == null) {
            return;
        }
        b.patient_nameProperty().set(n.getText().trim());
        b.addressProperty().set(addr.getText().trim());
        b.roomNameProperty().set(room.getValue().name);
        b.roomTypeProperty().set(room.getValue().name);
        b.checkInProperty().set(in.getValue().toString());
        
        RoomBooking.update(b);
        
        save.setDisable(true);
        clearForm(n, addr, in, room, save);
    }

    private void processCheckout(RoomBooking sel, TableView<RoomBooking> table) {
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Checkout Kamar");
        dialog.setHeaderText("Masukkan tanggal keluar untuk menghitung biaya");
        DatePicker dp = new DatePicker(LocalDate.now());
        dp.setStyle(fieldStyle());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(dp);
        dialog.setResultConverter(bt -> bt == ButtonType.OK ? dp.getValue() : null);

        LocalDate out = dialog.showAndWait().orElse(null);
        if (out == null) {
            return;
        }
        LocalDate in = LocalDate.parse(sel.getCheckIn());
        if (out.isBefore(in)) {
            new Alert(Alert.AlertType.ERROR, "Tanggal checkout tidak boleh sebelum check-in!", ButtonType.OK).showAndWait();
            return;
        }
        long days = Math.max(1, ChronoUnit.DAYS.between(in, out));
        
        // Find price
        long price = 0;
        for(RoomType rt : roomTypes) {
            if(rt.name.equals(sel.getRoomName())) {
                price = rt.pricePerDay;
                break;
            }
        }
        
        long cost = days * price;
        sel.checkOutProperty().set(out.toString());
        sel.daysProperty().set(String.valueOf(days));
        sel.costProperty().set(formatCurrency(cost));
        
        RoomBooking.update(sel);
        
        // Automatically remove patient from Inpatient list
        Inpatient.deleteByName(sel.getpatient_name());
        
        table.refresh();
        
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Total biaya: " + formatCurrency(cost) + " (" + days + " hari)\nPasien telah dihapus dari daftar rawat inap.", ButtonType.OK);
        a.setHeaderText("Checkout Berhasil");
        a.setTitle("Informasi");
        a.showAndWait();
    }

    private String formatCurrency(long value) {
        return new DecimalFormat("Rp ###,###").format(value).replace(",", ".");
    }

    private void refreshRoomCards() {
        cardsPane.getChildren().clear();
        roomTypes.forEach(rt -> cardsPane.getChildren().add(createRoomCard(rt)));
    }

    private VBox createRoomCard(RoomType rt) {
        VBox card = new VBox(8);
        card.setPrefSize(260, 360);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color:white; -fx-background-radius:14; -fx-cursor:hand; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.05),15,0,0,4);");

        ImageView imgView = new ImageView();
        try {
            imgView.setImage(new Image(getClass().getResource(rt.imagePath).toExternalForm()));
        } catch (Exception e) {
            System.out.println("Image not found: " + rt.imagePath);
        }
        imgView.setFitWidth(236);
        imgView.setFitHeight(140);
        imgView.setPreserveRatio(false);

        Label name = new Label(rt.name);
        name.setFont(Font.font("System", FontWeight.BOLD, 15));
        name.setTextFill(Color.web("#052b4c"));
        
        Label desc = new Label(rt.description);
        desc.setWrapText(true);
        desc.setFont(Font.font(12));
        desc.setTextFill(Color.web("#475569"));
        
        // Availability Logic
        int occupied = RoomBooking.getOccupiedBeds(rt.name);
        int available = Math.max(0, rt.totalBeds - occupied);
        String statusColor = available > 0 ? "#0d9488" : "#dc2626";
        
        Label availability = new Label("Tersedia: " + available + " / " + rt.totalBeds + " bed");
        availability.setFont(Font.font("System", FontWeight.BOLD, 12));
        availability.setTextFill(Color.web(statusColor));

        Label price = new Label(formatCurrency(rt.pricePerDay)+" / hari");
        price.setFont(Font.font("System", FontWeight.BOLD,14));
        price.setTextFill(Color.web("#0d9488"));
        
        Button infoBtn = new Button("Info Lainnya");
        infoBtn.setStyle(primaryButton());
        infoBtn.setOnMouseEntered(e -> infoBtn.setStyle(hoverPrimaryButton()));
        infoBtn.setOnMouseExited(e -> infoBtn.setStyle(primaryButton()));
        infoBtn.setCursor(Cursor.HAND);
        infoBtn.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION, rt.name+"\n"+rt.description+"\nHarga: "+formatCurrency(rt.pricePerDay)+" / hari\nKapasitas: "+rt.totalBeds+" bed", ButtonType.OK).showAndWait());
        
        card.setCursor(Cursor.HAND);
        card.getChildren().addAll(imgView, name, desc, availability, price, infoBtn);
        return card;
    }

    private TextField makeField(String prompt, double width){
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(width);
        tf.setStyle(fieldStyle());
        return tf;
    }
    private String fieldStyle(){
        return "-fx-background-radius:8; -fx-border-radius:8; -fx-background-color:#f1f5f9; -fx-border-color:#e2e8f0;";
    }
    private String primaryButton(){
        return "-fx-background-color:linear-gradient(to right,#0ea5e9,#0284c7); -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8;";
    }
    private String hoverPrimaryButton(){
        return "-fx-background-color:linear-gradient(to right,#38bdf8,#0ea5e9); -fx-text-fill:white; -fx-font-weight:600; -fx-background-radius:8; -fx-cursor:hand;";
    }
    private String primaryTextButton(){
        return "-fx-background-color:transparent; -fx-text-fill:#0f766e; -fx-font-weight:600;";
    }

    // Data Classes
    public static class RoomType {
        final String name;
        final long pricePerDay;
        final String description;
        final String imagePath;
        final int totalBeds;

        public RoomType(String n, long p, String d, String img, int beds) {
            name = n;
            pricePerDay = p;
            description = d;
            imagePath = img;
            totalBeds = beds;
        }
        @Override public String toString(){
            return name;
        }
    }
    // Booking class removed, using RoomBooking model
}