package projectpbo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DrugOrder {
    public static boolean saveOrder(String patientName, String patientRm, double totalPrice) {
        String sql = "INSERT INTO drug_orders (patient_name, patient_rm, total_price) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientName);
            ps.setString(2, patientRm);
            ps.setDouble(3, totalPrice);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving drug order: " + e.getMessage());
            return false;
        }
    }

    public static int countToday() {
        String sql = "SELECT COUNT(*) FROM drug_orders WHERE DATE(order_date) = CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting today's orders: " + e.getMessage());
        }
        return 0;
    }

    public static ObservableList<Order> fetchHistory() {
        ObservableList<Order> list = FXCollections.observableArrayList();
        String sql = "SELECT id, patient_name, patient_rm, total_price, order_date FROM drug_orders ORDER BY order_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Order(
                    rs.getInt("id"),
                    rs.getString("patient_name"),
                    rs.getString("patient_rm"),
                    rs.getDouble("total_price"),
                    rs.getTimestamp("order_date").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching order history: " + e.getMessage());
        }
        return list;
    }

    public static class Order {
        private final javafx.beans.property.IntegerProperty id = new javafx.beans.property.SimpleIntegerProperty();
        private final StringProperty patientName = new SimpleStringProperty();
        private final StringProperty patientRm = new SimpleStringProperty();
        private final DoubleProperty totalPrice = new SimpleDoubleProperty();
        private final javafx.beans.property.ObjectProperty<java.time.LocalDateTime> orderDate = new javafx.beans.property.SimpleObjectProperty<>();

        public Order(int id, String patientName, String patientRm, double totalPrice, java.time.LocalDateTime orderDate) {
            this.id.set(id);
            this.patientName.set(patientName);
            this.patientRm.set(patientRm);
            this.totalPrice.set(totalPrice);
            this.orderDate.set(orderDate);
        }

        public int getId() { return id.get(); }
        public javafx.beans.property.IntegerProperty idProperty() { return id; }

        public String getPatientName() { return patientName.get(); }
        public StringProperty patientNameProperty() { return patientName; }

        public String getPatientRm() { return patientRm.get(); }
        public StringProperty patientRmProperty() { return patientRm; }

        public double getTotalPrice() { return totalPrice.get(); }
        public DoubleProperty totalPriceProperty() { return totalPrice; }

        public java.time.LocalDateTime getOrderDate() { return orderDate.get(); }
        public javafx.beans.property.ObjectProperty<java.time.LocalDateTime> orderDateProperty() { return orderDate; }
        
        public String getFormattedDate() {
            return orderDate.get().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
        }
    }

    public static class Drug {
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty category = new SimpleStringProperty();
        private final StringProperty form = new SimpleStringProperty();
        private final DoubleProperty price = new SimpleDoubleProperty();

        public Drug(String name, String category, String form, double price) {
            this.name.set(name);
            this.category.set(category);
            this.form.set(form);
            this.price.set(price);
        }

        public String getName() { return name.get(); }
        public StringProperty nameProperty() { return name; }
        
        public String getCategory() { return category.get(); }
        public StringProperty categoryProperty() { return category; }

        public String getForm() { return form.get(); }
        public StringProperty formProperty() { return form; }

        public double getPrice() { return price.get(); }
        public DoubleProperty priceProperty() { return price; }

        @Override
        public String toString() {
            return getName();
        }

        public static ObservableList<Drug> fetchAll() {
            ObservableList<Drug> list = FXCollections.observableArrayList();
            String sql = "SELECT name, category, form, price FROM drugs ORDER BY name ASC";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Drug(
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("form"),
                        rs.getDouble("price")
                    ));
                }
            } catch (SQLException e) {
                System.err.println("Error fetching drugs: " + e.getMessage());
            }
            // If empty, maybe seed some data?
            if (list.isEmpty()) {
                seedDrugs();
                return fetchAll(); // Retry once
            }
            return list;
        }

        private static void seedDrugs() {
            String sql = "INSERT INTO drugs (name, category, form, price) VALUES (?, ?, ?, ?)";
            Object[][] data = {
                {"Paracetamol 500 mg", "Analgesic", "Tablet", 2000.0},
                {"Ibuprofen 200 mg", "Analgesic", "Tablet", 3500.0},
                {"Mefenamic Acid 500 mg", "Analgesic", "Tablet", 5000.0},
                {"Ketorolac Inj 30 mg", "Analgesic", "Injection", 25000.0},
                {"Amoxicillin 500 mg", "Antibiotic", "Tablet", 4000.0},
                {"Ciprofloxacin 500 mg", "Antibiotic", "Tablet", 8000.0},
                {"Cefixime 100 mg", "Antibiotic", "Tablet", 10000.0},
                {"Ceftriaxone 1g Inj", "Antibiotic", "Injection", 50000.0},
                {"Antasida Doen", "Stomach Medicine", "Tablet", 3000.0},
                {"Omeprazole 20 mg", "Stomach Medicine", "Capsule", 7000.0},
                {"Ranitidine 150 mg", "Stomach Medicine", "Tablet", 4500.0},
                {"Cetirizine 10 mg", "Allergy Medicine", "Tablet", 4000.0},
                {"Loratadine 10 mg", "Allergy Medicine", "Tablet", 6000.0},
                {"CTM 4 mg", "Allergy Medicine", "Tablet", 1500.0},
                {"Vitamin C 500 mg", "Vitamins", "Tablet", 2500.0},
                {"Vitamin B Complex", "Vitamins", "Tablet", 3000.0},
                {"OBH Syrup", "Cough & Flu", "Syrup", 15000.0},
                {"Guaifenesin 100 mg", "Cough & Flu", "Tablet", 4000.0},
                {"NaCl 0.9% 500ml", "Infusion Fluids", "Infusion", 12000.0},
                {"Ringer Lactate 500ml", "Infusion Fluids", "Infusion", 15000.0},
                {"D5% 500ml", "Infusion Fluids", "Infusion", 10000.0}
            };
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Object[] row : data) {
                    ps.setString(1, (String) row[0]);
                    ps.setString(2, (String) row[1]);
                    ps.setString(3, (String) row[2]);
                    ps.setDouble(4, (Double) row[3]);
                    ps.addBatch();
                }
                ps.executeBatch();
            } catch (SQLException e) {
                System.err.println("Error seeding drugs: " + e.getMessage());
            }
        }
    }
}
