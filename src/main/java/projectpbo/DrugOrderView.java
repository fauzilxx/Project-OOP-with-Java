package projectpbo;

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
    private final ObservableList<Drug> drugCatalog = FXCollections.observableArrayList();
    private final ObservableList<CartItem> cart = FXCollections.observableArrayList();
    private final SimpleDoubleProperty totalOrderPrice = new SimpleDoubleProperty(0);

    public DrugOrderView(Stage stage) {
        this.stage = stage;
        initCatalog();
    }

    public static Parent createRoot(Stage stage) {
        return new DrugOrderView(stage).build();
    }

    private void initCatalog() {
        drugCatalog.addAll(
            new Drug("Paracetamol 500 mg", "Analgesic", "Tablet", 2000),
            new Drug("Ibuprofen 200 mg", "Analgesic", "Tablet", 3500),
            new Drug("Mefenamic Acid 500 mg", "Analgesic", "Tablet", 5000),
            new Drug("Ketorolac Inj 30 mg", "Analgesic", "Injection", 25000),
            new Drug("Amoxicillin 500 mg", "Antibiotic", "Tablet", 4000),
            new Drug("Ciprofloxacin 500 mg", "Antibiotic", "Tablet", 8000),
            new Drug("Cefixime 100 mg", "Antibiotic", "Tablet", 10000),
            new Drug("Ceftriaxone 1g Inj", "Antibiotic", "Injection", 50000),
            new Drug("Antasida Doen", "Stomach Medicine", "Tablet", 3000),
            new Drug("Omeprazole 20 mg", "Stomach Medicine", "Capsule", 7000),
            new Drug("Ranitidine 150 mg", "Stomach Medicine", "Tablet", 4500),
            new Drug("Cetirizine 10 mg", "Allergy Medicine", "Tablet", 4000),
            new Drug("Loratadine 10 mg", "Allergy Medicine", "Tablet", 6000),
            new Drug("CTM 4 mg", "Allergy Medicine", "Tablet", 1500),
            new Drug("Vitamin C 500 mg", "Vitamins", "Tablet", 2500),
            new Drug("Vitamin B Complex", "Vitamins", "Tablet", 3000),
            new Drug("OBH Syrup", "Cough & Flu", "Syrup", 15000),
            new Drug("Guaifenesin 100 mg", "Cough & Flu", "Tablet", 4000),
            new Drug("NaCl 0.9% 500ml", "Infusion Fluids", "Infusion", 12000),
            new Drug("Ringer Lactate 500ml", "Infusion Fluids", "Infusion", 15000),
            new Drug("D5% 500ml", "Infusion Fluids", "Infusion", 10000)
        );
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
        pGrid.setHgap(20); pGrid.setVgap(16);
        
        TextField pRM = makeField("Masukkan No. RM (Tekan Enter)");
        TextField pName = makeField("Nama Pasien"); pName.setEditable(false); pName.setStyle(fieldStyle()+" -fx-background-color:#f1f5f9;");
        DatePicker pDOB = new DatePicker(); pDOB.setStyle(fieldStyle()+" -fx-opacity:0.9;"); pDOB.setPrefWidth(200); pDOB.setDisable(true);
        TextField pDoctor = makeField("Dokter Penanggung Jawab"); pDoctor.setEditable(false); pDoctor.setStyle(fieldStyle()+" -fx-background-color:#f1f5f9;");
        TextField pLocation = makeField("Lokasi (Kamar / Poli)"); pLocation.setEditable(false); pLocation.setStyle(fieldStyle()+" -fx-background-color:#f1f5f9;");

        // Auto-search logic
        pRM.setOnKeyPressed(e -> {
            if(e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                Patient found = findPatient(pRM.getText());
                if(found != null) {
                    pName.setText(found.name);
                    pDOB.setValue(found.dob);
                    pDoctor.setText(found.doctor);
                    pLocation.setText(found.location);
                    pRM.setStyle(fieldStyle()+" -fx-border-color:#10b981;"); // Green border on success
                } else {
                    showAlert("Not Found", "Pasien dengan No. RM tersebut tidak ditemukan.");
                    pName.clear(); pDOB.setValue(null); pDoctor.clear(); pLocation.clear();
                    pRM.setStyle(fieldStyle()+" -fx-border-color:#ef4444;"); // Red border on fail
                }
            }
        });

        pGrid.add(new Label("No. Rekam Medis"), 0, 0); pGrid.add(pRM, 1, 0);
        pGrid.add(new Label("Nama Pasien"), 2, 0); pGrid.add(pName, 3, 0);
        pGrid.add(new Label("Tgl Lahir"), 0, 1); pGrid.add(pDOB, 1, 1);
        pGrid.add(new Label("Dokter"), 2, 1); pGrid.add(pDoctor, 3, 1);
        pGrid.add(new Label("Lokasi"), 0, 2); pGrid.add(pLocation, 1, 2);
        
        Label hint = new Label("* Tekan Enter pada kolom No. RM untuk mencari data pasien otomatis");
        hint.setFont(Font.font(11)); hint.setTextFill(Color.web("#64748b"));
        pGrid.add(hint, 1, 3, 3, 1);
        
        patientSection.getChildren().add(pGrid);

        // 2. Order Form
        VBox orderSection = createSection("Form Pemesanan Obat");
        GridPane oGrid = new GridPane();
        oGrid.setHgap(20); oGrid.setVgap(16);

        ComboBox<Drug> drugCombo = new ComboBox<>(drugCatalog);
        drugCombo.setPromptText("Pilih Obat...");
        drugCombo.setPrefWidth(300);
        drugCombo.setEditable(true);
        
        // Fix ClassCastException by adding a StringConverter
        drugCombo.setConverter(new javafx.util.StringConverter<Drug>() {
            @Override public String toString(Drug d) { return d == null ? "" : d.name; }
            @Override public Drug fromString(String s) {
                return drugCatalog.stream().filter(d -> d.name.equalsIgnoreCase(s)).findFirst().orElse(null);
            }
        });
        
        TextField catField = makeField("Kategori"); catField.setEditable(false);
        TextField formField = makeField("Bentuk"); formField.setEditable(false);
        TextField priceField = makeField("Harga Satuan"); priceField.setEditable(false);

        drugCombo.setOnAction(e -> {
            // Use getSelectionModel().getSelectedItem() or handle potential nulls safely
            Drug d = drugCombo.getSelectionModel().getSelectedItem();
            if (d == null && drugCombo.getValue() instanceof Drug) {
                 d = drugCombo.getValue();
            }
            
            if(d != null) {
                catField.setText(d.category);
                formField.setText(d.form);
                priceField.setText(formatCurrency(d.price));
            }
        });

        TextField dosageField = makeField("Dosis (e.g. 3x1)");
        Spinner<Integer> qtySpinner = new Spinner<>(1, 100, 1); qtySpinner.setEditable(true); qtySpinner.setStyle(fieldStyle());
        TextField instrField = makeField("Cara Pakai");
        TextArea notesArea = new TextArea(); notesArea.setPromptText("Catatan Tambahan"); notesArea.setPrefRowCount(2); notesArea.setStyle(fieldStyle());

        Button addBtn = new Button("Tambah ke Keranjang");
        addBtn.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        addBtn.setCursor(Cursor.HAND);
        addBtn.setOnAction(e -> {
            Drug sel = drugCombo.getValue();
            // If getValue() returns null (because fromString returned null), try to find by text manually
            if(sel == null) {
                String text = drugCombo.getEditor().getText();
                sel = drugCatalog.stream().filter(d -> d.name.equalsIgnoreCase(text)).findFirst().orElse(null);
            }
            
            if(sel == null) { showAlert("Error", "Pilih obat yang valid dari daftar!"); return; }
            
            cart.add(new CartItem(sel, dosageField.getText(), qtySpinner.getValue(), instrField.getText(), notesArea.getText()));
            calculateTotal();
            
            drugCombo.getSelectionModel().clearSelection();
            drugCombo.getEditor().clear();
            catField.clear(); formField.clear(); priceField.clear();
            dosageField.clear(); qtySpinner.getValueFactory().setValue(1);
            instrField.clear(); notesArea.clear();
        });

        oGrid.add(new Label("Nama Obat"), 0, 0); oGrid.add(drugCombo, 1, 0);
        oGrid.add(new Label("Kategori"), 2, 0); oGrid.add(catField, 3, 0);
        oGrid.add(new Label("Bentuk"), 0, 1); oGrid.add(formField, 1, 1);
        oGrid.add(new Label("Harga"), 2, 1); oGrid.add(priceField, 3, 1);
        oGrid.add(new Label("Dosis"), 0, 2); oGrid.add(dosageField, 1, 2);
        oGrid.add(new Label("Jumlah"), 2, 2); oGrid.add(qtySpinner, 3, 2);
        oGrid.add(new Label("Instruksi"), 0, 3); oGrid.add(instrField, 1, 3, 3, 1);
        oGrid.add(new Label("Catatan"), 0, 4); oGrid.add(notesArea, 1, 4, 3, 1);
        oGrid.add(addBtn, 1, 5);
        orderSection.getChildren().add(oGrid);

        // 3. Cart
        VBox cartSection = createSection("Keranjang Obat");
        TableView<CartItem> table = new TableView<>(cart);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(300);
        
        TableColumn<CartItem, String> cName = new TableColumn<>("Nama Obat");
        cName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().drug.name));
        TableColumn<CartItem, String> cDos = new TableColumn<>("Dosis");
        cDos.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().dosage));
        TableColumn<CartItem, Integer> cQty = new TableColumn<>("Qty");
        cQty.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().qty));
        TableColumn<CartItem, String> cPrice = new TableColumn<>("Harga");
        cPrice.setCellValueFactory(c -> new SimpleStringProperty(formatCurrency(c.getValue().drug.price)));
        TableColumn<CartItem, String> cSub = new TableColumn<>("Subtotal");
        cSub.setCellValueFactory(c -> new SimpleStringProperty(formatCurrency(c.getValue().getSubtotal())));
        
        TableColumn<CartItem, Void> cAct = new TableColumn<>("Aksi");
        cAct.setCellFactory(param -> new TableCell<>(){
            private final Button btn = new Button("Hapus");
            {
                btn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-size:11px;");
                btn.setCursor(Cursor.HAND);
                btn.setOnAction(e -> { cart.remove(getIndex()); calculateTotal(); });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty); setGraphic(empty?null:btn);
            }
        });
        
        table.getColumns().addAll(cName, cDos, cQty, cPrice, cSub, cAct);
        cartSection.getChildren().add(table);

        // 4. Footer
        HBox footer = new HBox(20);
        footer.setAlignment(Pos.CENTER_RIGHT);
        
        Label totalLbl = new Label("Total: Rp 0");
        totalLbl.setFont(Font.font("System", FontWeight.BOLD, 20));
        totalOrderPrice.addListener((o,old,val) -> totalLbl.setText("Total: "+formatCurrency(val.longValue())));
        
        Button cancelBtn = new Button("Batalkan");
        cancelBtn.setStyle("-fx-background-color:#cbd5e1; -fx-text-fill:#334155; -fx-font-weight:bold;");
        cancelBtn.setCursor(Cursor.HAND);
        cancelBtn.setOnAction(e -> { cart.clear(); calculateTotal(); });
        
        Button submitBtn = new Button("Proses Pesanan");
        submitBtn.setStyle("-fx-background-color:#0d9488; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:10 20;");
        submitBtn.setCursor(Cursor.HAND);
        submitBtn.setOnAction(e -> {
            if(cart.isEmpty()) { showAlert("Warning", "Keranjang kosong!"); return; }
            showAlert("Sukses", "Pesanan berhasil dikirim ke farmasi!\nTotal: "+totalLbl.getText());
            cart.clear(); calculateTotal();
        });
        
        footer.getChildren().addAll(cancelBtn, totalLbl, submitBtn);
        
        content.getChildren().addAll(patientSection, orderSection, cartSection, footer);
        return content;
    }

    private VBox createSection(String title) {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.05),10,0,0,2);");
        Label l = new Label(title); l.setFont(Font.font("System", FontWeight.BOLD, 14)); l.setTextFill(Color.web("#334155"));
        box.getChildren().add(l);
        return box;
    }

    private TextField makeField(String prompt) {
        TextField tf = new TextField(); tf.setPromptText(prompt); tf.setStyle(fieldStyle()); return tf;
    }
    private String fieldStyle() { return "-fx-background-radius:6; -fx-border-radius:6; -fx-border-color:#cbd5e1; -fx-padding:8;"; }
    private void calculateTotal() { totalOrderPrice.set(cart.stream().mapToDouble(CartItem::getSubtotal).sum()); }
    private String formatCurrency(long v) { return new DecimalFormat("Rp ###,###").format(v).replace(",","."); }
    private void showAlert(String t, String m) { new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }

    private Patient findPatient(String rm) {
        // Dummy database
        if(rm.equalsIgnoreCase("RM001234")) return new Patient("Ahmad Wijaya", LocalDate.of(1990, 5, 12), "Dr. Agus Salim, Sp.PD", "Ruang Melati 101");
        if(rm.equalsIgnoreCase("RM001235")) return new Patient("Siti Nurhaliza", LocalDate.of(1985, 8, 23), "Dr. Bintang Kejora, Sp.A", "Poli Anak");
        if(rm.equalsIgnoreCase("RM001236")) return new Patient("Budi Santoso", LocalDate.of(1978, 11, 2), "Dr. Citra Dewi, Sp.OG", "Ruang Mawar 202");
        if(rm.equalsIgnoreCase("RM001237")) return new Patient("Dewi Lestari", LocalDate.of(1995, 2, 14), "Dr. Darmawan, Sp.B", "Poli Bedah");
        if(rm.equalsIgnoreCase("RM001238")) return new Patient("Eko Prasetyo", LocalDate.of(1988, 7, 30), "Dr. Erna Susanti, Sp.JP", "Ruang Anggrek 305");
        return null;
    }

    public static class Patient {
        String name, doctor, location; LocalDate dob;
        public Patient(String n, LocalDate d, String doc, String loc) { name=n; dob=d; doctor=doc; location=loc; }
    }

    public static class Drug {
        String name, category, form; long price;
        public Drug(String n, String c, String f, long p) { name=n; category=c; form=f; price=p; }
        @Override public String toString() { return name; }
    }
    public static class CartItem {
        Drug drug; String dosage, instructions, notes; int qty;
        public CartItem(Drug d, String dos, int q, String i, String n) { drug=d; dosage=dos; qty=q; instructions=i; notes=n; }
        public long getSubtotal() { return drug.price * qty; }
    }
}
