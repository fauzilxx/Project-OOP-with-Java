package projectpbo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Revenue {

    public static long getTodayRevenue() {
        long total = 0;
        LocalDate today = LocalDate.now();
        
        // Drug Orders
        String sqlDrugs = "SELECT SUM(total_price) FROM drug_orders WHERE DATE(order_date) = CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlDrugs);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                total += rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Bookings
        String sqlBookings = "SELECT cost FROM bookings WHERE check_out = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlBookings)) {
            ps.setString(1, today.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Since cost is stored as double/numeric in DB now
                    double val = rs.getDouble("cost");
                    total += (long) val;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    public static List<RevenueRecord> getRevenueHistory() {
        Map<LocalDate, Long> dailyRevenue = new TreeMap<>();

        // 1. Fetch Drug Orders
        String sqlDrugs = "SELECT DATE(order_date) as date, SUM(total_price) as total FROM drug_orders GROUP BY DATE(order_date)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlDrugs);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                java.sql.Date d = rs.getDate("date");
                if (d != null) {
                    dailyRevenue.merge(d.toLocalDate(), rs.getLong("total"), Long::sum);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 2. Fetch Room Bookings
        String sqlBookings = "SELECT check_out, cost FROM bookings WHERE check_out IS NOT NULL AND check_out != '' AND cost IS NOT NULL AND cost != ''";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlBookings);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String checkOutStr = rs.getString("check_out");
                try {
                    LocalDate date = LocalDate.parse(checkOutStr);
                    double val = rs.getDouble("cost");
                    dailyRevenue.merge(date, (long) val, Long::sum);
                } catch (Exception e) {
                    // ignore parsing errors
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<RevenueRecord> history = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        // Generate for last 14 days
        for (int i = 13; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long total = dailyRevenue.getOrDefault(date, 0L);
            history.add(new RevenueRecord(date, total, 0));
        }

        // Calculate percentage changes
        for (int i = 1; i < history.size(); i++) {
            RevenueRecord prev = history.get(i - 1);
            RevenueRecord curr = history.get(i);
            if (prev.totalRevenue > 0) {
                double change = ((double) (curr.totalRevenue - prev.totalRevenue) / prev.totalRevenue) * 100;
                curr.percentChange = change;
            } else if (curr.totalRevenue > 0) {
                curr.percentChange = 100.0;
            } else {
                curr.percentChange = 0;
            }
        }
        
        return history;
    }

    public static class RevenueRecord {
        public LocalDate date;
        public long totalRevenue;
        public double percentChange;

        public RevenueRecord(LocalDate d, long t, double p) {
            date = d;
            totalRevenue = t;
            percentChange = p;
        }
    }
}
