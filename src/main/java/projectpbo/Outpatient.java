package projectpbo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Outpatient {
    // id removed, patientNumber is PK
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty patientNumber = new SimpleStringProperty();
    private final StringProperty complaint = new SimpleStringProperty();
    private final StringProperty schedule = new SimpleStringProperty(); // formatted string "dd MMMM yyyy HH:mm"
    private final StringProperty doctor = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm");

    public Outpatient(String name, String patientNumber, String complaint, String scheduleFormatted, String doctor, String status) {
        this.name.set(name);
        this.patientNumber.set(patientNumber);
        this.complaint.set(complaint);
        this.schedule.set(scheduleFormatted);
        this.doctor.set(doctor);
        this.status.set(status);
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

    public String getComplaint() {
        return complaint.get();
    }

    public StringProperty complaintProperty() {
        return complaint;
    }

    public String getSchedule() {
        return schedule.get();
    }

    public StringProperty scheduleProperty() {
        return schedule;
    }

    public String getDoctor() {
        return doctor.get();
    }

    public StringProperty doctorProperty() {
        return doctor;
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public boolean matches(String q) {
        if (q == null || q.isBlank()) {
            return true;
        }
        String all = (getName() + " " + getPatientNumber() + " " + getComplaint() + " " + getSchedule() + " " + getDoctor() + " " + getStatus()).toLowerCase();
        return all.contains(q.toLowerCase());
    }

    // === Static CRUD operations ===
    public static ObservableList<Outpatient> fetchAll() {
        DBConnection.migrateToNaturalKeys(); // Ensure migration
        DBConnection.ensureOutpatientStatusColumnExists();
        ObservableList<Outpatient> list = FXCollections.observableArrayList();
        String sql = "SELECT name, patient_number, complaint, schedule, doctor, status FROM outpatients ORDER BY patient_number ASC";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("name");
                String number = rs.getString("patient_number");
                String complaint = rs.getString("complaint");
                java.sql.Timestamp schedTs = rs.getTimestamp("schedule");
                String schedFormatted = schedTs != null ? DISPLAY_FMT.format(schedTs.toLocalDateTime()) : "";
                String doctor = rs.getString("doctor");
                String status = rs.getString("status");
                if (status == null) status = "Menunggu";
                list.add(new Outpatient(name, number, complaint, schedFormatted, doctor, status));
            }
        } catch (SQLException e) {
            System.err.println("Gagal load data outpatients: " + e.getMessage());
        }
        return list;
    }

    public static Outpatient add(String name, String number, String complaint, LocalDateTime schedule, String doctor, String status) {
        String sql = "INSERT INTO outpatients (name, patient_number, complaint, schedule, doctor, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, number);
            ps.setString(3, complaint);
            ps.setTimestamp(4, schedule != null ? java.sql.Timestamp.valueOf(schedule) : null);
            ps.setString(5, doctor);
            ps.setString(6, status);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                return new Outpatient(name, number, complaint, schedule != null ? DISPLAY_FMT.format(schedule) : "", doctor, status);
            }
        } catch (SQLException e) {
            System.err.println("Gagal tambah pasien rawat jalan: " + e.getMessage());
        }
        return null;
    }

    public static boolean update(Outpatient op) {
        String sql = "UPDATE outpatients SET name=?, complaint=?, schedule=?, doctor=?, status=? WHERE patient_number=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, op.getName());
            ps.setString(2, op.getComplaint());
            LocalDateTime parsed = parseSchedule(op.getSchedule());
            ps.setTimestamp(3, parsed != null ? java.sql.Timestamp.valueOf(parsed) : null);
            ps.setString(4, op.getDoctor());
            ps.setString(5, op.getStatus());
            ps.setString(6, op.getPatientNumber());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal update pasien rawat jalan: " + e.getMessage());
            return false;
        }
    }

    public static boolean delete(Outpatient op) {
        String sql = "DELETE FROM outpatients WHERE patient_number=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, op.getPatientNumber());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal hapus pasien rawat jalan: " + e.getMessage());
            return false;
        }
    }

    public static int count() {
        String sql = "SELECT COUNT(*) FROM outpatients";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Gagal hitung pasien rawat jalan: " + e.getMessage());
        }
        return 0;
    }

    private static LocalDateTime parseSchedule(String display) {
        if (display == null || display.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(display, DISPLAY_FMT);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isPatientNumberExists(String number) {
        String sql = "SELECT COUNT(*) FROM outpatients WHERE patient_number = ?";
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
            System.err.println("Error checking outpatient number: " + e.getMessage());
        }
        return false;
    }

    public static String getStatusByPatientNumber(String number) {
        String sql = "SELECT status FROM outpatients WHERE patient_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, number);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("status");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking status: " + e.getMessage());
        }
        return null;
    }
}
