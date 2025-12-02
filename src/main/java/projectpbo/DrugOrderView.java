package projectpbo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class DrugOrderView {

    private final Stage stage;
    private final ObservableList<DrugOrder.Drug> drugCatalog = FXCollections.observableArrayList();
    private final ObservableList<CartItem> cart = FXCollections.observableArrayList();
    private final ObservableList<DrugOrder.Order> historyData = FXCollections.observableArrayList();
    private final SimpleDoubleProperty totalOrderPrice = new SimpleDoubleProperty(0);

    public DrugOrderView(Stage stage) {
        this.stage = stage;
        initCatalog();
        refreshHistory();
    }

    public static Parent createRoot(Stage stage) {
        return new DrugOrderView(stage).build();
    }

    private void initCatalog() {
        drugCatalog.setAll(DrugOrder.Drug.fetchAll());
    }

    private void refreshHistory() {
        historyData.setAll(DrugOrder.fetchHistory());
    }

    private Parent build() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f7fa;");
        root.setTop(buildHeader());

        ScrollPane scroll = new ScrollPane(buildContent());
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-background: #f5f7fa;");
        root.setCenter(scroll);

        return root;
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setPadding(new Insets(14, 32, 14, 32));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);");

        Button backBtn = new Button("← Kembali");
        backBtn.setStyle("-fx-background-color:transparent; -fx-text-fill:#0f766e; -fx-font-weight:600;");
        backBtn.setCursor(Cursor.HAND);
        backBtn.setOnAction(e -> stage.getScene().setRoot(new AdminDashboard(stage).build()));

        Label title = new Label("PEMESANAN OBAT (PHARMACY)");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(backBtn, spacer, title);
        return header;
    }

    private VBox buildContent() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(24));
        content.setAlignment(Pos.TOP_CENTER);

        // 1. Patient Info
        VBox patientSection = createSection("Informasi Pasien");
        GridPane pGrid = new GridPane();
        pGrid.setHgap(20);
        pGrid.setVgap(16);

        TextField pRM = makeField("Masukkan No. RM (Tekan Enter)");
        TextField pName = makeField("Nama Pasien");
        pName.setEditable(false);
        pName.setStyle(fieldStyle() + " -fx-background-color:#f1f5f9;");
        TextField pDoctor = makeField("Dokter Penanggung Jawab");
        pDoctor.setEditable(false);
        pDoctor.setStyle(fieldStyle() + " -fx-background-color:#f1f5f9;");
        TextField pLocation = makeField("Lokasi (Kamar / Poli)");
        pLocation.setEditable(false);
        pLocation.setStyle(fieldStyle() + " -fx-background-color:#f1f5f9;");

        // Auto-search logic
        pRM.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                Patient found = findPatient(pRM.getText());
                if (found != null) {
                    pName.setText(found.name);
                    pDoctor.setText(found.doctor);
                    pLocation.setText(found.location);
                    pRM.setStyle(fieldStyle() + " -fx-border-color:#10b981;"); // Green border on success
                } else {
                    showAlert("Not Found", "Pasien dengan No. RM tersebut tidak ditemukan.");
                    pName.clear();
                    pDoctor.clear();
                    pLocation.clear();
                    pRM.setStyle(fieldStyle() + " -fx-border-color:#ef4444;"); // Red border on fail
                }
            }
        });

        pGrid.add(new Label("No. Rekam Medis"), 0, 0);
        pGrid.add(pRM, 1, 0);
        pGrid.add(new Label("Nama Pasien"), 2, 0);
        pGrid.add(pName, 3, 0);
        pGrid.add(new Label("Dokter"), 0, 1);
        pGrid.add(pDoctor, 1, 1);
        pGrid.add(new Label("Lokasi"), 2, 1);
        pGrid.add(pLocation, 3, 1);

        Label hint = new Label("* Tekan Enter pada kolom No. RM untuk mencari data pasien otomatis");
        hint.setFont(Font.font(11));
        hint.setTextFill(Color.web("#64748b"));
        pGrid.add(hint, 1, 2, 3, 1);

        patientSection.getChildren().add(pGrid);

        // 2. Order Form
        VBox orderSection = createSection("Form Pemesanan Obat");
        GridPane oGrid = new GridPane();
        oGrid.setHgap(20);
        oGrid.setVgap(16);

        ComboBox<DrugOrder.Drug> drugCombo = new ComboBox<>(drugCatalog);
        drugCombo.setPromptText("Pilih Obat...");
        drugCombo.setPrefWidth(300);
        drugCombo.setEditable(true);

        // Fix ClassCastException by adding a StringConverter
        drugCombo.setConverter(new javafx.util.StringConverter<DrugOrder.Drug>() {
            @Override
            public String toString(DrugOrder.Drug d) {
                return d == null ? "" : d.getName();
            }

            @Override
            public DrugOrder.Drug fromString(String s) {
                return drugCatalog.stream().filter(d -> d.getName().equalsIgnoreCase(s)).findFirst().orElse(null);
            }
        });

        TextField catField = makeField("Kategori");
        catField.setEditable(false);
        TextField formField = makeField("Bentuk");
        formField.setEditable(false);
        TextField priceField = makeField("Harga Satuan");
        priceField.setEditable(false);

        drugCombo.setOnAction(e -> {
            // Use getSelectionModel().getSelectedItem() or handle potential nulls safely
            DrugOrder.Drug d = drugCombo.getSelectionModel().getSelectedItem();
            if (d == null && drugCombo.getValue() instanceof DrugOrder.Drug) {
                d = drugCombo.getValue();
            }

            if (d != null) {
                catField.setText(d.getCategory());
                formField.setText(d.getForm());
                priceField.setText(formatCurrency(d.getPrice()));
            }
        });

        TextField dosageField = makeField("Dosis (e.g. 3x1)");
        Spinner<Integer> qtySpinner = new Spinner<>(1, 100, 1);
        qtySpinner.setEditable(true);
        qtySpinner.setStyle(fieldStyle());
        TextField instrField = makeField("Cara Pakai");
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Catatan Tambahan");
        notesArea.setPrefRowCount(2);
        notesArea.setStyle(fieldStyle());

        Button addBtn = new Button("Tambah ke Keranjang");
        addBtn.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        addBtn.setCursor(Cursor.HAND);
        addBtn.setOnAction(e -> {
            DrugOrder.Drug sel = drugCombo.getValue();
            // If getValue() returns null (because fromString returned null), try to find by text manually
            if (sel == null) {
                String text = drugCombo.getEditor().getText();
                sel = drugCatalog.stream().filter(d -> d.getName().equalsIgnoreCase(text)).findFirst().orElse(null);
            }

            if (sel == null) {
                showAlert("Error", "Pilih obat yang valid dari daftar!");
                return;
            }

            cart.add(new CartItem(sel, dosageField.getText(), qtySpinner.getValue(), instrField.getText(), notesArea.getText()));
            calculateTotal();

            drugCombo.getSelectionModel().clearSelection();
            drugCombo.getEditor().clear();
            catField.clear();
            formField.clear();
            priceField.clear();
            dosageField.clear();
            qtySpinner.getValueFactory().setValue(1);
            instrField.clear();
            notesArea.clear();
        });

        oGrid.add(new Label("Nama Obat"), 0, 0);
        oGrid.add(drugCombo, 1, 0);
        oGrid.add(new Label("Kategori"), 2, 0);
        oGrid.add(catField, 3, 0);
        oGrid.add(new Label("Bentuk"), 0, 1);
        oGrid.add(formField, 1, 1);
        oGrid.add(new Label("Harga"), 2, 1);
        oGrid.add(priceField, 3, 1);
        oGrid.add(new Label("Dosis"), 0, 2);
        oGrid.add(dosageField, 1, 2);
        oGrid.add(new Label("Jumlah"), 2, 2);
        oGrid.add(qtySpinner, 3, 2);
        oGrid.add(new Label("Instruksi"), 0, 3);
        oGrid.add(instrField, 1, 3, 3, 1);
        oGrid.add(new Label("Catatan"), 0, 4);
        oGrid.add(notesArea, 1, 4, 3, 1);
        oGrid.add(addBtn, 1, 5);
        orderSection.getChildren().add(oGrid);

        // 3. Cart
        VBox cartSection = createSection("Keranjang Obat");
        TableView<CartItem> table = new TableView<>(cart);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(300);

        TableColumn<CartItem, String> cName = new TableColumn<>("Nama Obat");
        cName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().drug.getName()));
        TableColumn<CartItem, String> cDos = new TableColumn<>("Dosis");
        cDos.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().dosage));
        TableColumn<CartItem, Integer> cQty = new TableColumn<>("Qty");
        cQty.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().qty));
        TableColumn<CartItem, String> cPrice = new TableColumn<>("Harga");
        cPrice.setCellValueFactory(c -> new SimpleStringProperty(formatCurrency(c.getValue().drug.getPrice())));
        TableColumn<CartItem, String> cSub = new TableColumn<>("Subtotal");
        cSub.setCellValueFactory(c -> new SimpleStringProperty(formatCurrency(c.getValue().getSubtotal())));

        TableColumn<CartItem, Void> cAct = new TableColumn<>("Aksi");
        cAct.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Hapus");

            {
                btn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-size:11px;");
                btn.setCursor(Cursor.HAND);
                btn.setOnAction(e -> {
                    cart.remove(getIndex());
                    calculateTotal();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(cName, cDos, cQty, cPrice, cSub, cAct);
        cartSection.getChildren().add(table);

        // 4. Footer
        HBox footer = new HBox(20);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Label totalLbl = new Label("Total: Rp 0");
        totalLbl.setFont(Font.font("System", FontWeight.BOLD, 20));
        totalOrderPrice.addListener((o, old, val) -> totalLbl.setText("Total: " + formatCurrency(val.longValue())));

        Button cancelBtn = new Button("Batalkan");
        cancelBtn.setStyle("-fx-background-color:#cbd5e1; -fx-text-fill:#334155; -fx-font-weight:bold;");
        cancelBtn.setCursor(Cursor.HAND);
        cancelBtn.setOnAction(e -> {
            cart.clear();
            calculateTotal();
        });

        Button submitBtn = new Button("Proses Pesanan");
        submitBtn.setStyle("-fx-background-color:#0d9488; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:10 20;");
        submitBtn.setCursor(Cursor.HAND);
        submitBtn.setOnAction(e -> {
            if (cart.isEmpty()) {
                showAlert("Warning", "Keranjang kosong!");
                return;
            }
            String rm = pRM.getText().trim();
            String name = pName.getText().trim();
            if (rm.isEmpty() || name.isEmpty()) {
                showAlert("Error", "Data pasien belum lengkap!");
                return;
            }
            
            if (DrugOrder.saveOrder(name, rm, totalOrderPrice.get())) {
                showAlert("Sukses", "Pesanan berhasil dikirim ke farmasi!\nTotal: " + totalLbl.getText());
                cart.clear();
                calculateTotal();
                pRM.clear();
                pName.clear();
                pDoctor.clear();
                pLocation.clear();
                pRM.setStyle(fieldStyle());
                refreshHistory();
            } else {
                showAlert("Error", "Gagal menyimpan pesanan!");
            }
        });

        footer.getChildren().addAll(cancelBtn, totalLbl, submitBtn);

        // 5. History Table
        VBox historySection = createSection("Riwayat Pemesanan Obat");
        TableView<DrugOrder.Order> historyTable = new TableView<>(historyData);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historyTable.setPrefHeight(250);

        TableColumn<DrugOrder.Order, String> hDate = new TableColumn<>("Tanggal");
        hDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFormattedDate()));
        
        TableColumn<DrugOrder.Order, String> hRM = new TableColumn<>("No. RM");
        hRM.setCellValueFactory(c -> c.getValue().patientRmProperty());
        
        TableColumn<DrugOrder.Order, String> hName = new TableColumn<>("Nama Pasien");
        hName.setCellValueFactory(c -> c.getValue().patientNameProperty());
        
        TableColumn<DrugOrder.Order, String> hTotal = new TableColumn<>("Total Harga");
        hTotal.setCellValueFactory(c -> new SimpleStringProperty(formatCurrency(c.getValue().getTotalPrice())));

        historyTable.getColumns().addAll(hDate, hRM, hName, hTotal);
        historySection.getChildren().add(historyTable);

        content.getChildren().addAll(patientSection, orderSection, cartSection, footer, historySection);
        return content;
    }

    private VBox createSection(String title) {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.05),10,0,0,2);");
        Label l = new Label(title);
        l.setFont(Font.font("System", FontWeight.BOLD, 14));
        l.setTextFill(Color.web("#334155"));
        box.getChildren().add(l);
        return box;
    }

    private TextField makeField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(fieldStyle());
        return tf;
    }

    private String fieldStyle() {
        return "-fx-background-radius:6; -fx-border-radius:6; -fx-border-color:#cbd5e1; -fx-padding:8;";
    }

    private void calculateTotal() {
        totalOrderPrice.set(cart.stream().mapToDouble(CartItem::getSubtotal).sum());
    }

    private String formatCurrency(double v) {
        return new DecimalFormat("Rp ###,###").format(v).replace(",", ".");
    }

    private void showAlert(String t, String m) {
        new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait();
    }

    private Patient findPatient(String rm) {
        // Check Inpatients
        String sqlIn = "SELECT name, doctor, room FROM inpatients WHERE patient_number = ?";
        // Check Outpatients
        String sqlOut = "SELECT name, doctor, 'Rawat Jalan' as loc FROM outpatients WHERE patient_number = ?";
        // Check Queues
        String sqlQ = "SELECT patient_name, doctor_name, 'Antrian' as loc FROM queues WHERE patient_number = ?";
        
        try (Connection conn = DBConnection.getConnection()) {
            // Try Inpatients
            try (PreparedStatement ps = conn.prepareStatement(sqlIn)) {
                ps.setString(1, rm);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return new Patient(rs.getString("name"), rs.getString("doctor"), rs.getString("room"));
                }
            }
            // Try Outpatients
            try (PreparedStatement ps = conn.prepareStatement(sqlOut)) {
                ps.setString(1, rm);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return new Patient(rs.getString("name"), rs.getString("doctor"), rs.getString("loc"));
                }
            }
            // Try Queues
            try (PreparedStatement ps = conn.prepareStatement(sqlQ)) {
                ps.setString(1, rm);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return new Patient(rs.getString("patient_name"), rs.getString("doctor_name"), rs.getString("loc"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class Patient {
        String name;
        String doctor;
        String location;

        public Patient(String n, String doc, String loc) {
            name = n;
            doctor = doc;
            location = loc;
        }
    }

    public static class CartItem {
        DrugOrder.Drug drug;
        String dosage;
        String instructions;
        String notes;
        int qty;

        public CartItem(DrugOrder.Drug d, String dos, int q, String i, String n) {
            drug = d;
            dosage = dos;
            qty = q;
            instructions = i;
            notes = n;
        }

        public double getSubtotal() {
            return drug.getPrice() * qty;
        }
    }
}
