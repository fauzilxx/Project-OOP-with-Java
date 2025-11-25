package projectpbo;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class RevenueView {

    private final Stage stage;
    private final ObservableList<RevenueRecord> revenueData = FXCollections.observableArrayList();

    public RevenueView(Stage stage) {
        this.stage = stage;
        generateDummyData();
    }

    public static Parent createRoot(Stage stage) {
        return new RevenueView(stage).build();
    }

    private void generateDummyData() {
        List<RevenueRecord> temp = new ArrayList<>();
        LocalDate today = LocalDate.now();
        Random rand = new Random();

        // Generate for last 14 days
        for (int i = 13; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long bookingRev = 5000000 + rand.nextInt(5000000); // 5jt - 10jt
            long drugRev = 2000000 + rand.nextInt(3000000);    // 2jt - 5jt
            long total = bookingRev + drugRev;
            temp.add(new RevenueRecord(date, total, 0));
        }

        // Calculate percentage changes
        for (int i = 1; i < temp.size(); i++) {
            RevenueRecord prev = temp.get(i - 1);
            RevenueRecord curr = temp.get(i);
            double change = ((double) (curr.totalRevenue - prev.totalRevenue) / prev.totalRevenue) * 100;
            curr.percentChange = change;
        }
        
        // Add to observable list
        revenueData.addAll(temp);
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

        Label title = new Label("LAPORAN PENDAPATAN HARIAN");
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

        // 1. Summary Cards
        RevenueRecord today = revenueData.get(revenueData.size() - 1);
        RevenueRecord yesterday = revenueData.get(revenueData.size() - 2);
        
        HBox cards = new HBox(20);
        cards.setAlignment(Pos.CENTER);
        cards.getChildren().addAll(
            createSummaryCard("Pendapatan Hari Ini", today.totalRevenue, today.percentChange),
            createSummaryCard("Pendapatan Kemarin", yesterday.totalRevenue, yesterday.percentChange)
        );

        // 2. Chart
        VBox chartSection = createSection("Grafik Pendapatan (14 Hari Terakhir)");
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Pendapatan (Rupiah)");
        
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setLegendVisible(false);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM");
        
        for(RevenueRecord r : revenueData) {
            series.getData().add(new XYChart.Data<>(r.date.format(fmt), r.totalRevenue));
        }
        lineChart.getData().add(series);
        lineChart.setPrefHeight(300);
        chartSection.getChildren().add(lineChart);

        // 3. Table
        VBox tableSection = createSection("Riwayat Pendapatan");
        TableView<RevenueRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(400);

        TableColumn<RevenueRecord, String> dateCol = new TableColumn<>("Tanggal");
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))));
        
        TableColumn<RevenueRecord, String> revCol = new TableColumn<>("Total Pendapatan");
        revCol.setCellValueFactory(c -> new SimpleStringProperty(formatCurrency(c.getValue().totalRevenue)));
        
        TableColumn<RevenueRecord, String> changeCol = new TableColumn<>("Perubahan (%)");
        changeCol.setCellValueFactory(c -> {
            double val = c.getValue().percentChange;
            String arrow = val > 0 ? "▲" : (val < 0 ? "▼" : "-");
            return new SimpleStringProperty(String.format("%s %.2f%%", arrow, Math.abs(val)));
        });
        changeCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item);
                    if(item.contains("▲")) setTextFill(Color.web("#10b981"));
                    else if(item.contains("▼")) setTextFill(Color.web("#ef4444"));
                    else setTextFill(Color.BLACK);
                }
            }
        });

        table.getColumns().addAll(dateCol, revCol, changeCol);
        
        // Sort by date descending for table
        ObservableList<RevenueRecord> tableData = FXCollections.observableArrayList(revenueData);
        Collections.reverse(tableData);
        table.setItems(tableData);
        
        tableSection.getChildren().add(table);

        content.getChildren().addAll(cards, chartSection, tableSection);
        return content;
    }

    private VBox createSummaryCard(String title, long amount, double change) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setPrefWidth(300);
        card.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.05),10,0,0,2);");
        
        Label tLbl = new Label(title); tLbl.setTextFill(Color.web("#64748b")); tLbl.setFont(Font.font(14));
        Label aLbl = new Label(formatCurrency(amount)); aLbl.setTextFill(Color.web("#0f172a")); aLbl.setFont(Font.font("System", FontWeight.BOLD, 24));
        
        String arrow = change > 0 ? "▲" : (change < 0 ? "▼" : "");
        String color = change > 0 ? "#10b981" : (change < 0 ? "#ef4444" : "#64748b");
        Label cLbl = new Label(String.format("%s %.2f%% dari hari sebelumnya", arrow, Math.abs(change)));
        cLbl.setTextFill(Color.web(color)); cLbl.setFont(Font.font(13));
        
        card.getChildren().addAll(tLbl, aLbl, cLbl);
        return card;
    }

    private VBox createSection(String title) {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.05),10,0,0,2);");
        Label l = new Label(title); l.setFont(Font.font("System", FontWeight.BOLD, 14)); l.setTextFill(Color.web("#334155"));
        box.getChildren().add(l);
        return box;
    }

    private String formatCurrency(long v) { return new DecimalFormat("Rp ###,###").format(v).replace(",","."); }

    public static class RevenueRecord {
        LocalDate date; long totalRevenue; double percentChange;
        public RevenueRecord(LocalDate d, long t, double p) { date=d; totalRevenue=t; percentChange=p; }
    }
}
