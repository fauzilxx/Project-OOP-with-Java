package projectpbo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Inpatient {
    // id removed, patientNumber is PK
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty patientNumber = new SimpleStringProperty();
    private final StringProperty illness = new SimpleStringProperty();
    private final StringProperty room = new SimpleStringProperty();
    private final StringProperty doctor = new SimpleStringProperty();
    private final StringProperty address = new SimpleStringProperty();

    public Inpatient(String name, String patientNumber, String illness, String room, String doctor, String address) {
        this.name.set(name);
        this.patientNumber.set(patientNumber);
        this.illness.set(illness);
        this.room.set(room);
        this.doctor.set(doctor);
        this.address.set(address);
    }

    // getId removed

    public String getName() {
        return name.get();
    }
    public StringProperty nameProperty() {
        return name;
    }
    public String getPatientNumber() {
        return patientNumber.get();
    }
    public StringProperty patientNumberProperty() {
        return patientNumber;
    }
    public String getIllness() {
        return illness.get();
    }
    public StringProperty illnessProperty() {
        return illness;
        }
    public String getRoom() {
        return room.get();
    }
    public StringProperty roomProperty() {
        return room;
    }
    public String getDoctor() {
        return doctor.get();
    }
    public StringProperty doctorProperty() {
        return doctor;
    }
    public String getAddress() {
        return address.get();
    }
    public StringProperty addressProperty() {
        return address;
    }

    public boolean matches(String q) {
        if (q == null || q.isBlank()) return true;
        String all = (getName()+" "+getPatientNumber()+" "+getIllness()+" "+getRoom()+" "+getDoctor()+" "+getAddress()).toLowerCase();
        return all.contains(q.toLowerCase());
    }

    // === Static Data Access Methods (merged from repository) ===
    public static ObservableList<Inpatient> fetchAll() {
        DBConnection.migrateToNaturalKeys(); // Ensure migration
        ObservableList<Inpatient> list = FXCollections.observableArrayList();
        String sql = "SELECT name, patient_number, illness, room, doctor, address FROM inpatients ORDER BY patient_number ASC";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Inpatient(
                        rs.getString("name"),
                        rs.getString("patient_number"),
                        rs.getString("illness"),
                        rs.getString("room"),
                        rs.getString("doctor"),
                        rs.getString("address")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Gagal load data inpatients: " + e.getMessage());
        }
        return list;
    }

    public static Inpatient add(String name, String number, String illness, String room, String doctor, String address) {
        String sql = "INSERT INTO inpatients (name, patient_number, illness, room, doctor, address) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, number);
            ps.setString(3, illness);
            ps.setString(4, room);
            ps.setString(5, doctor);
            ps.setString(6, address);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                return new Inpatient(name, number, illness, room, doctor, address);
            }
        } catch (SQLException e) {
            System.err.println("Gagal tambah pasien: " + e.getMessage());
        }
        return null;
    }

    public static boolean update(Inpatient ip) {
        String sql = "UPDATE inpatients SET name=?, illness=?, room=?, doctor=?, address=? WHERE patient_number=?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ip.getName());
            ps.setString(2, ip.getIllness());
            ps.setString(3, ip.getRoom());
            ps.setString(4, ip.getDoctor());
            ps.setString(5, ip.getAddress());
            ps.setString(6, ip.getPatientNumber());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal update pasien: " + e.getMessage());
            return false;
        }
    }

    public static boolean delete(Inpatient ip) {
        String sql = "DELETE FROM inpatients WHERE patient_number=?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ip.getPatientNumber());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal hapus pasien: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteByName(String name) {
        String sql = "DELETE FROM inpatients WHERE LOWER(name) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal hapus pasien by name: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateRoomByName(String name, String room) {
        String sql = "UPDATE inpatients SET room=? WHERE LOWER(name) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room);
            ps.setString(2, name);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal update room pasien: " + e.getMessage());
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

    public static boolean exists(String name) {
        String sql = "SELECT COUNT(*) FROM inpatients WHERE LOWER(name) = LOWER(?)";
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
            System.err.println("Error checking patient existence: " + e.getMessage());
        }
        return false;
    }

    public static boolean isPatientNumberExists(String number) {
        String sql = "SELECT COUNT(*) FROM inpatients WHERE patient_number = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, number);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking patient number: " + e.getMessage());
        }
        return false;
    }

    // Removed isPatientNumberExists(String number, int excludeId) as ID is removed and PK is patient_number


    public static String getAddress(String name) {
        String sql = "SELECT address FROM inpatients WHERE LOWER(name) = LOWER(?) LIMIT 1";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return "";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("address");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching address: " + e.getMessage());
        }
        return "";
    }
}
