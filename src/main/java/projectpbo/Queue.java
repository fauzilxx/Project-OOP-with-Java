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

public class Queue {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty queueNumber = new SimpleStringProperty();
    private final StringProperty patientName = new SimpleStringProperty();
    private final StringProperty patientNumber = new SimpleStringProperty();
    private final StringProperty complaint = new SimpleStringProperty();
    private final StringProperty arrivalTime = new SimpleStringProperty(); // formatted "dd MMMM yyyy HH:mm"
    private final StringProperty doctorName = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm");

    public Queue(int id, String queueNumber, String patientName, String patientNumber, String complaint, String arrivalTimeFormatted, String doctorName, String status) {
        this.id.set(id);
        this.queueNumber.set(queueNumber);
        this.patientName.set(patientName);
        this.patientNumber.set(patientNumber);
        this.complaint.set(complaint);
        this.arrivalTime.set(arrivalTimeFormatted);
        this.doctorName.set(doctorName);
        this.status.set(status);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public String getQueueNumber() { return queueNumber.get(); }
    public StringProperty queueNumberProperty() { return queueNumber; }
    public String getPatientName() { return patientName.get(); }
    public StringProperty patientNameProperty() { return patientName; }
    public String getPatientNumber() { return patientNumber.get(); }
    public StringProperty patientNumberProperty() { return patientNumber; }
    public String getComplaint() { return complaint.get(); }
    public StringProperty complaintProperty() { return complaint; }
    public String getArrivalTime() { return arrivalTime.get(); }
    public StringProperty arrivalTimeProperty() { return arrivalTime; }
    public String getDoctorName() { return doctorName.get(); }
    public StringProperty doctorNameProperty() { return doctorName; }
    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }

    public boolean matches(String q) {
        if (q == null || q.isBlank()) return true;
        String all = (getQueueNumber()+" "+getPatientName()+" "+getPatientNumber()+" "+getComplaint()+" "+getArrivalTime()+" "+getDoctorName()+" "+getStatus()).toLowerCase();
        return all.contains(q.toLowerCase());
    }

    // === CRUD Static Methods ===
    public static ObservableList<Queue> fetchAll() {
        ObservableList<Queue> list = FXCollections.observableArrayList();
        String sql = "SELECT id, queue_number, patient_name, patient_rm, complaint, arrival_time, doctor_name, status FROM queues ORDER BY id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String qn = rs.getString("queue_number");
                String pname = rs.getString("patient_name");
                String prm = rs.getString("patient_rm");
                String comp = rs.getString("complaint");
                java.sql.Timestamp atTs = rs.getTimestamp("arrival_time");
                String atDisplay = atTs != null ? DISPLAY_FMT.format(atTs.toLocalDateTime()) : "";
                String dname = rs.getString("doctor_name");
                String stat = rs.getString("status");
                list.add(new Queue(id, qn, pname, prm, comp, atDisplay, dname, stat));
            }
        } catch (SQLException e) {
            System.err.println("Gagal load queue: " + e.getMessage());
        }
        return list;
    }

    public static Queue add(String patientName, String patientNumber, String complaint, LocalDateTime arrival, String doctorName, String status) {
        String queueNumber = generateNextQueueNumber();
        String sql = "INSERT INTO queues (queue_number, patient_rm, patient_name, complaint, arrival_time, doctor_name, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, queueNumber);
            ps.setString(2, patientNumber);
            ps.setString(3, patientName);
            ps.setString(4, complaint);
            ps.setTimestamp(5, arrival != null ? java.sql.Timestamp.valueOf(arrival) : null);
            ps.setString(6, doctorName);
            ps.setString(7, status);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return new Queue(keys.getInt(1), queueNumber, patientName, patientNumber, complaint,
                                arrival != null ? DISPLAY_FMT.format(arrival) : "", doctorName, status);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal tambah antrian: " + e.getMessage());
        }
        return null;
    }

    public static boolean update(Queue q) {
        String sql = "UPDATE queues SET patient_name=?, patient_rm=?, complaint=?, arrival_time=?, doctor_name=?, status=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q.getPatientName());
            ps.setString(2, q.getPatientNumber());
            ps.setString(3, q.getComplaint());
            LocalDateTime parsed = parseDisplayTime(q.getArrivalTime());
            ps.setTimestamp(4, parsed != null ? java.sql.Timestamp.valueOf(parsed) : null);
            ps.setString(5, q.getDoctorName());
            ps.setString(6, q.getStatus());
            ps.setInt(7, q.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal update antrian: " + e.getMessage());
            return false;
        }
    }

    public static boolean delete(Queue q) {
        String sql = "DELETE FROM queues WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, q.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal hapus antrian: " + e.getMessage());
            return false;
        }
    }

    public static int countWaiting() {
        String sql = "SELECT COUNT(*) FROM queues WHERE status='Menunggu'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Gagal hitung antrian menunggu: " + e.getMessage());
        }
        return 0;
    }

    public static ObservableList<String> listPatientNames() {
        ObservableList<String> names = FXCollections.observableArrayList();
        String sql = "SELECT DISTINCT patient_name, patient_rm FROM queues";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString(1);
                String rm = rs.getString(2);
                names.add(rm != null && !rm.isBlank() ? name + " - " + rm : name);
            }
        } catch (SQLException e) {
            System.err.println("Gagal ambil nama pasien antrian: " + e.getMessage());
        }
        return names;
    }

    private static String generateNextQueueNumber() {
        String last = null;
        String sql = "SELECT queue_number FROM queues ORDER BY id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) last = rs.getString(1);
        } catch (SQLException e) {
            System.err.println("Gagal ambil nomor antrian terakhir: " + e.getMessage());
        }
        int nextNum = 1;
        if (last != null && last.matches("Q\\d{4}")) {
            nextNum = Integer.parseInt(last.substring(1)) + 1;
        }
        return String.format("Q%04d", nextNum);
    }

    private static LocalDateTime parseDisplayTime(String display) {
        if (display == null || display.isBlank()) return null;
        try { return LocalDateTime.parse(display, DISPLAY_FMT); } catch (Exception e) { return null; }
    }
}
