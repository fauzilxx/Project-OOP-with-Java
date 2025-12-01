package projectpbo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Doctor {
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();

    public Doctor(String name, String category) {
        this.name.set(name);
        this.category.set(category);
    }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public String getCategory() { return category.get(); }
    public StringProperty categoryProperty() { return category; }

    @Override
    public String toString() {
        return getName() + " - " + getCategory();
    }

    public static ObservableList<Doctor> fetchAll() {
        ObservableList<Doctor> list = FXCollections.observableArrayList();
        String sql = "SELECT name, category FROM doctors ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Doctor(rs.getString("name"), rs.getString("category")));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctors: " + e.getMessage());
        }
        
        if (list.isEmpty()) {
            seedDoctors();
            return fetchAll();
        }
        return list;
    }

    public static boolean exists(String name) {
        String sql = "SELECT COUNT(*) FROM doctors WHERE LOWER(name) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking doctor existence: " + e.getMessage());
        }
        return false;
    }

    private static void seedDoctors() {
        // 1. Create table first
        try (Connection conn = DBConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS doctors (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), category VARCHAR(255))");
        } catch (SQLException e) {
            System.err.println("Error creating doctors table: " + e.getMessage());
            return;
        }

        // 2. Insert data
        String sql = "INSERT INTO doctors (name, category) VALUES (?, ?)";
        Object[][] data = {
            {"Dr. Agus Salim, Sp.PD", "Penyakit Dalam"},
            {"Dr. Bintang Kejora, Sp.A", "Pediatri (Anak)"},
            {"Dr. Citra Dewi, Sp.OG", "Kandungan & Kebidanan"},
            {"Dr. Darmawan, Sp.B", "Bedah Umum"},
            {"Dr. Erna Susanti, Sp.JP", "Jantung & Pembuluh Darah"},
            {"Dr. Fadli Rahman, Sp.S", "Saraf"},
            {"Dr. Gita Purnama, Sp.M", "Mata"},
            {"Dr. Hendra Wijaya, Sp.THT-KL", "THT-KL"},
            {"Dr. Indah Permata, Sp.KK", "Kulit & Kelamin"},
            {"Dr. Joko Santoso, Sp.OT", "Orthopedi"},
            {"Dr. Kartika Sari, Sp.P", "Paru"},
            {"Dr. Lukman Hakim, Sp.U", "Urologi"},
            {"Dr. Maya Putri, Sp.KJ", "Kedokteran Jiwa (Psikiater)"},
            {"Dr. Nanda Pratama, Sp.An", "Anestesi"},
            {"Dr. Olivia Wijaya, Sp.Rad", "Radiologi"}
        };
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : data) {
                ps.setString(1, (String) row[0]);
                ps.setString(2, (String) row[1]);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            System.err.println("Error seeding doctors: " + e.getMessage());
        }
    }
}
