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

public class Queue {
    // id removed, patient_number is PK
    private final StringProperty queueNumber = new SimpleStringProperty();
    private final StringProperty patientName = new SimpleStringProperty();
    private final StringProperty patientNumber = new SimpleStringProperty();
    private final StringProperty complaint = new SimpleStringProperty();
    private final StringProperty arrivalTime = new SimpleStringProperty(); // formatted "dd MMMM yyyy HH:mm"
    private final StringProperty doctorName = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty waitingTime = new SimpleStringProperty();

    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm");

    public Queue(String queueNumber, String patientName, String patientNumber, String complaint, String arrivalTimeFormatted, String doctorName, String status) {
        this.queueNumber.set(queueNumber);
        this.patientName.set(patientName);
        this.patientNumber.set(patientNumber);
        this.complaint.set(complaint);
        this.arrivalTime.set(arrivalTimeFormatted);
        this.doctorName.set(doctorName);
        this.status.set(status);
        this.waitingTime.set("-");
    }

    // getId removed

    public String getQueueNumber() {
        return queueNumber.get();
    }
    public StringProperty queueNumberProperty() {
        return queueNumber;
    }
    public String getPatientName() {
        return patientName.get();
    }
    public StringProperty patientNameProperty() {
        return patientName;
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
    public String getArrivalTime() {
        return arrivalTime.get();
    }
    public StringProperty arrivalTimeProperty() {
        return arrivalTime;
    }
    public String getDoctorName() {
        return doctorName.get();
    }
    public StringProperty doctorNameProperty() {
        return doctorName;
    }
    public String getStatus() {
        return status.get();
    }
    public StringProperty statusProperty() {
        return status;
    }
    public String getWaitingTime() {
        return waitingTime.get();
    }
    public StringProperty waitingTimeProperty() {
        return waitingTime;
    }

    public boolean matches(String q) {
        if (q == null || q.isBlank()) return true;
        String all = (getQueueNumber()+" "+getPatientName()+" "+getPatientNumber()+" "+getComplaint()+" "+getArrivalTime()+" "+getDoctorName()+" "+getStatus()).toLowerCase();
        return all.contains(q.toLowerCase());
    }

    // === CRUD Static Methods ===
    public static ObservableList<Queue> fetchAll() {
        DBConnection.migrateToNaturalKeys(); // Ensure migration
        ObservableList<Queue> list = FXCollections.observableArrayList();
        String sql = "SELECT queue_number, patient_name, patient_number, " +
                     "COALESCE(symptoms, complaint) as complaint, " +
                     "COALESCE(registration_time, arrival_time) as arrival_time, " +
                     "doctor_name, status FROM queues ORDER BY arrival_time ASC";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            
            // Map to track active patients count per day to calculate wait time
            java.util.Map<java.time.LocalDate, Integer> dailyQueueCount = new java.util.HashMap<>();

            while (rs.next()) {
                String qn = rs.getString("queue_number");
                String pname = rs.getString("patient_name");
                String prm = rs.getString("patient_number");
                String comp = rs.getString("complaint");
                java.sql.Timestamp ts = rs.getTimestamp("arrival_time");
                String timeStr = "";
                String waitStr = "-";
                
                String stat = rs.getString("status");
                String doc = rs.getString("doctor_name");

                if (ts != null) {
                    LocalDateTime arrival = ts.toLocalDateTime();
                    timeStr = arrival.format(DISPLAY_FMT);
                    java.time.LocalDate date = arrival.toLocalDate();
                    
                    // Initialize count for this day if not exists
                    dailyQueueCount.putIfAbsent(date, 0);
                    int currentCount = dailyQueueCount.get(date);

                    if ("Menunggu".equalsIgnoreCase(stat)) {
                        // Calculate wait time: count * 2 minutes
                        int waitMinutes = currentCount * 2;
                        waitStr = waitMinutes + " menit";
                        // Increment count for next person
                        dailyQueueCount.put(date, currentCount + 1);
                    } else if ("Sedang Diperiksa".equalsIgnoreCase(stat)) {
                        waitStr = "Sedang Diperiksa";
                        // This person occupies the doctor, so adds to wait time for others
                        dailyQueueCount.put(date, currentCount + 1);
                    }
                    // "Selesai" does not contribute to wait time
                }

                Queue q = new Queue(qn, pname, prm, comp, timeStr, doc, stat);
                q.waitingTime.set(waitStr);
                list.add(q);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }



    public static Queue add(String patientName, String patientNumber, String complaint, LocalDateTime arrival, String doctorName, String status) {
        String queueNumber = generateNextQueueNumber();
        String sql = "INSERT INTO queues (queue_number, patient_number, patient_name, complaint, arrival_time, doctor_name, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, queueNumber);
            ps.setString(2, patientNumber);
            ps.setString(3, patientName);
            ps.setString(4, complaint);
            ps.setTimestamp(5, arrival != null ? java.sql.Timestamp.valueOf(arrival) : null);
            ps.setString(6, doctorName);
            ps.setString(7, status);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                return new Queue(queueNumber, patientName, patientNumber, complaint,
                        arrival != null ? DISPLAY_FMT.format(arrival) : "", doctorName, status);
            }
        } catch (SQLException e) {
            System.err.println("Gagal tambah antrian: " + e.getMessage());
        }
        return null;
    }

    private static LocalDateTime parseDisplayTime(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) return null;
        try {
            return LocalDateTime.parse(timeStr, DISPLAY_FMT);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean update(Queue q) {
        String sql = "UPDATE queues SET patient_name=?, complaint=?, arrival_time=?, doctor_name=?, status=? WHERE patient_number=?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q.getPatientName());
            ps.setString(2, q.getComplaint());
            LocalDateTime parsed = parseDisplayTime(q.getArrivalTime());
            ps.setTimestamp(3, parsed != null ? java.sql.Timestamp.valueOf(parsed) : null);
            ps.setString(4, q.getDoctorName());
            ps.setString(5, q.getStatus());
            ps.setString(6, q.getPatientNumber());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal update antrian: " + e.getMessage());
            return false;
        }
    }

    public static boolean delete(Queue q) {
        String sql = "DELETE FROM queues WHERE patient_number=?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q.getPatientNumber());
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
        String sql = "SELECT DISTINCT patient_name, patient_number FROM queues";
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
        String sql = "SELECT queue_number FROM queues ORDER BY queue_number DESC LIMIT 1";
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

    // Removed isPatientNumberExists(String number, int excludeId)
    public static boolean isPatientNumberExists(String number) {
        String sql = "SELECT COUNT(*) FROM queues WHERE patient_number = ?";
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
            System.err.println("Error checking queue patient number: " + e.getMessage());
        }
        return false;
    }
}
