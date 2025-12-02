package projectpbo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RoomBooking {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty patient_name = new SimpleStringProperty();
    private final StringProperty address = new SimpleStringProperty();
    private final StringProperty room_name = new SimpleStringProperty(); // formatted "dd MMMM yyyy HH:mm"
    private final StringProperty room_type = new SimpleStringProperty();
    private final StringProperty check_in = new SimpleStringProperty();
    private final StringProperty check_out = new SimpleStringProperty();
    private final StringProperty days  = new SimpleStringProperty();
    private final StringProperty cost = new SimpleStringProperty();

    public RoomBooking (int id, String patient_name, String address, String room_name, String room_type, String check_in, String check_out, String days, String cost) {
        this.id.set(id);
        this.patient_name.set(patient_name);
        this.address.set(address);
        this.room_name.set(room_name);
        this.room_type.set(room_type);
        this.check_in.set(check_in);
        this.check_out.set(check_out);
        this.days.set(days);
        this.cost.set(cost);
    }

    public int getId() {
        return id.get();
    }
    public IntegerProperty idProperty() {
        return id;
    }
    public String getpatient_name() {
        return patient_name.get();
    }
    public StringProperty patient_nameProperty() {
        return patient_name;
    }
    public String getAddress() {
        return address.get();
    }
    public StringProperty addressProperty() {
        return address;
    }
    
    public String getRoomName() {
        return room_name.get();
    }
    public StringProperty roomNameProperty() {
        return room_name;
    }
    
    public String getRoomType() {
        return room_type.get();
    }
    public StringProperty roomTypeProperty() {
        return room_type;
    }
    
    public String getCheckIn() {
        return check_in.get();
    }
    public StringProperty checkInProperty() {
        return check_in;
    }
    
    public String getCheckOut() {
        return check_out.get();
    }
    public StringProperty checkOutProperty() {
        return check_out;
    }
    
    public String getDays() {
        return days.get();
    }
    public StringProperty daysProperty() {
        return days;
    }
    public String getCost() {
        return cost.get();
    }
    public StringProperty costProperty() {
        return cost;
    }

    // Logic to get current occupancy from database (linked to Bookings)
    public static int getOccupiedBeds(String roomType) {
        // Count bookings where room_name matches and check_out is empty/null
        String sql = "SELECT COUNT(*) FROM bookings WHERE room_name = ? AND (check_out IS NULL OR check_out = '')";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return 0;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, roomType);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting occupied beds: " + e.getMessage());
        }
        return 0;
    }

    public static boolean hasActiveBooking(String patientName) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE LOWER(patient_name) = LOWER(?) AND (check_out IS NULL OR check_out = '')";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, patientName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking active booking: " + e.getMessage());
        }
        return false;
    }

    // === Static Data Access Methods ===
    public static ObservableList<RoomBooking> fetchAll() {
        ObservableList<RoomBooking> list = FXCollections.observableArrayList();
        String sql = "SELECT id, patient_name, address, room_name, room_type, check_in, check_out, days, cost FROM bookings ORDER BY id DESC";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return list;
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String c = rs.getString("cost");
                    if (c != null && !c.isEmpty()) {
                        try {
                            double v = Double.parseDouble(c);
                            c = new DecimalFormat("Rp ###,###").format(v).replace(",", ".");
                        } catch (NumberFormatException ignored) {}
                    }
                    list.add(new RoomBooking(
                            rs.getInt("id"),
                            rs.getString("patient_name"),
                            rs.getString("address"),
                            rs.getString("room_name"),
                            rs.getString("room_type"),
                            rs.getString("check_in"),
                            rs.getString("check_out"),
                            rs.getString("days"),
                            c
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal load data bookings: " + e.getMessage());
        }
        return list;
    }

    public static RoomBooking add(String name, String address, String roomName, String roomType, String checkIn) {
        String sql = "INSERT INTO bookings (patient_name, address, room_name, room_type, check_in) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return null;
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, address);
                ps.setString(3, roomName);
                ps.setString(4, roomType);
                ps.setString(5, checkIn);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return new RoomBooking(rs.getInt(1), name, address, roomName, roomType, checkIn, "", "", "");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal tambah booking: " + e.getMessage());
        }
        return null;
    }

    public static boolean update(RoomBooking b) {
        String sql = "UPDATE bookings SET patient_name=?, address=?, room_name=?, room_type=?, check_in=?, check_out=?, days=?, cost=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, b.getpatient_name());
                ps.setString(2, b.getAddress());
                ps.setString(3, b.getRoomName());
                ps.setString(4, b.getRoomType());
                ps.setString(5, b.getCheckIn());
                ps.setString(6, b.getCheckOut());
                ps.setString(7, b.getDays());
                
                String rawCost = b.getCost();
                if (rawCost != null) {
                    // Remove "Rp", dots, and commas to get pure number
                    rawCost = rawCost.replace("Rp", "").replace(".", "").replace(",", "").trim();
                }
                ps.setString(8, rawCost);
                
                ps.setInt(9, b.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Gagal update booking: " + e.getMessage());
            return false;
        }
    }
}
