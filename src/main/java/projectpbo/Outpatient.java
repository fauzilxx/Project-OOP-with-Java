package projectpbo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Outpatient {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty patientNumber = new SimpleStringProperty();
    private final StringProperty complaint = new SimpleStringProperty();
    private final StringProperty schedule = new SimpleStringProperty(); // formatted string "dd MMMM yyyy HH:mm"
    private final StringProperty doctor = new SimpleStringProperty();

    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm");

    public Outpatient(int id, String name, String patientNumber, String complaint, String scheduleFormatted, String doctor) {
        this.id.set(id);
        this.name.set(name);
        this.patientNumber.set(patientNumber);
        this.complaint.set(complaint);
        this.schedule.set(scheduleFormatted);
        this.doctor.set(doctor);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }
    public String getPatientNumber() { return patientNumber.get(); }
    public StringProperty patientNumberProperty() { return patientNumber; }
    public String getComplaint() { return complaint.get(); }
    public StringProperty complaintProperty() { return complaint; }
    public String getSchedule() { return schedule.get(); }
    public StringProperty scheduleProperty() { return schedule; }
    public String getDoctor() { return doctor.get(); }
    public StringProperty doctorProperty() { return doctor; }

    public boolean matches(String q) {
        if (q == null || q.isBlank()) return true;
        String all = (getName() + " " + getPatientNumber() + " " + getComplaint() + " " + getSchedule() + " " + getDoctor()).toLowerCase();
        return all.contains(q.toLowerCase());
    }

    // === Static CRUD operations ===
    public static ObservableList<Outpatient> fetchAll() {
        ObservableList<Outpatient> list = FXCollections.observableArrayList();
        String sql = "SELECT id, name, patient_number, complaint, schedule, doctor FROM outpatients ORDER BY id DESC";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String number = rs.getString("patient_number");
                String complaint = rs.getString("complaint");
                java.sql.Timestamp schedTs = rs.getTimestamp("schedule");
                String schedFormatted = schedTs != null ? DISPLAY_FMT.format(schedTs.toLocalDateTime()) : "";
                String doctor = rs.getString("doctor");
                list.add(new Outpatient(id, name, number, complaint, schedFormatted, doctor));
            }
        } catch (SQLException e) {
            System.err.println("Gagal load data outpatients: " + e.getMessage());
        }
        return list;
    }

    public static Outpatient add(String name, String number, String complaint, LocalDateTime schedule, String doctor) {
        String sql = "INSERT INTO outpatients (name, patient_number, complaint, schedule, doctor) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, number);
            ps.setString(3, complaint);
            ps.setTimestamp(4, schedule != null ? java.sql.Timestamp.valueOf(schedule) : null);
            ps.setString(5, doctor);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return new Outpatient(keys.getInt(1), name, number, complaint, schedule != null ? DISPLAY_FMT.format(schedule) : "", doctor);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal tambah pasien rawat jalan: " + e.getMessage());
        }
        return null;
    }

    public static boolean update(Outpatient op) {
        String sql = "UPDATE outpatients SET name=?, patient_number=?, complaint=?, schedule=?, doctor=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, op.getName());
            ps.setString(2, op.getPatientNumber());
            ps.setString(3, op.getComplaint());
            LocalDateTime parsed = parseSchedule(op.getSchedule());
            ps.setTimestamp(4, parsed != null ? java.sql.Timestamp.valueOf(parsed) : null);
            ps.setString(5, op.getDoctor());
            ps.setInt(6, op.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal update pasien rawat jalan: " + e.getMessage());
            return false;
        }
    }

    public static boolean delete(Outpatient op) {
        String sql = "DELETE FROM outpatients WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, op.getId());
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
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Gagal hitung pasien rawat jalan: " + e.getMessage());
        }
        return 0;
    }

    private static LocalDateTime parseSchedule(String display) {
        if (display == null || display.isBlank()) return null;
        try { return LocalDateTime.parse(display, DISPLAY_FMT); } catch (Exception e) { return null; }
    }
}
