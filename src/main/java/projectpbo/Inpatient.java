package projectpbo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Inpatient {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty patientNumber = new SimpleStringProperty();
    private final StringProperty illness = new SimpleStringProperty();
    private final StringProperty room = new SimpleStringProperty();
    private final StringProperty doctor = new SimpleStringProperty();

    public Inpatient(int id, String name, String patientNumber, String illness, String room, String doctor) {
        this.id.set(id);
        this.name.set(name);
        this.patientNumber.set(patientNumber);
        this.illness.set(illness);
        this.room.set(room);
        this.doctor.set(doctor);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }
    public String getPatientNumber() { return patientNumber.get(); }
    public StringProperty patientNumberProperty() { return patientNumber; }
    public String getIllness() { return illness.get(); }
    public StringProperty illnessProperty() { return illness; }
    public String getRoom() { return room.get(); }
    public StringProperty roomProperty() { return room; }
    public String getDoctor() { return doctor.get(); }
    public StringProperty doctorProperty() { return doctor; }

    public boolean matches(String q) {
        if (q == null || q.isBlank()) return true;
        String all = (getName()+" "+getPatientNumber()+" "+getIllness()+" "+getRoom()+" "+getDoctor()).toLowerCase();
        return all.contains(q.toLowerCase());
    }

    // === Static Data Access Methods (merged from repository) ===
    public static ObservableList<Inpatient> fetchAll() {
        ObservableList<Inpatient> list = FXCollections.observableArrayList();
        String sql = "SELECT id, name, patient_number, illness, room, doctor FROM inpatients ORDER BY id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Inpatient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("patient_number"),
                        rs.getString("illness"),
                        rs.getString("room"),
                        rs.getString("doctor")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Gagal load data inpatients: " + e.getMessage());
        }
        return list;
    }

    public static Inpatient add(String name, String number, String illness, String room, String doctor) {
        String sql = "INSERT INTO inpatients (name, patient_number, illness, room, doctor) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, number);
            ps.setString(3, illness);
            ps.setString(4, room);
            ps.setString(5, doctor);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return new Inpatient(keys.getInt(1), name, number, illness, room, doctor);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal tambah pasien: " + e.getMessage());
        }
        return null;
    }

    public static boolean update(Inpatient ip) {
        String sql = "UPDATE inpatients SET name=?, patient_number=?, illness=?, room=?, doctor=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ip.getName());
            ps.setString(2, ip.getPatientNumber());
            ps.setString(3, ip.getIllness());
            ps.setString(4, ip.getRoom());
            ps.setString(5, ip.getDoctor());
            ps.setInt(6, ip.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal update pasien: " + e.getMessage());
            return false;
        }
    }

    public static boolean delete(Inpatient ip) {
        String sql = "DELETE FROM inpatients WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ip.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal hapus pasien: " + e.getMessage());
            return false;
        }
    }

    public static int count() {
        String sql = "SELECT COUNT(*) FROM inpatients";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Gagal hitung pasien: " + e.getMessage());
        }
        return 0;
    }
}
