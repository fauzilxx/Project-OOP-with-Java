package projectpbo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    // Sesuaikan dengan konfigurasi database Anda
    private static final String DB_NAME = "nasihuy_hospital";
    private static final String URL = "jdbc:mysql://localhost:3306/" + DB_NAME;
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        try {
            // Register JDBC driver (opsional untuk versi baru, tapi aman dilakukan)
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL tidak ditemukan!");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.err.println("Gagal terhubung ke database: " + e.getMessage());
            // Cek apakah error karena database belum ada
            if (e.getErrorCode() == 1049) {
                System.err.println("Database '" + DB_NAME + "' belum dibuat.");
            }
            return null;
        }
    }
    
    // Method helper untuk cek koneksi
    public static boolean checkConnection() {
        try (Connection conn = getConnection()) {
            return conn != null;
        } catch (SQLException e) {
            return false;
        }
    }

    public static void createTables() {
        String[] sqlStatements = {
            // Table Users
            "CREATE TABLE IF NOT EXISTS users (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "username VARCHAR(50) NOT NULL UNIQUE, " +
            "email VARCHAR(100), " +
            "phone VARCHAR(20), " +
            "password VARCHAR(255) NOT NULL, " +
            "role VARCHAR(20) DEFAULT 'admin')",

            // Table Inpatients (Rawat Inap)
            "CREATE TABLE IF NOT EXISTS inpatients (" +
            "patient_number VARCHAR(20) PRIMARY KEY, " +
            "name VARCHAR(100) NOT NULL, " +
            "illness VARCHAR(100), " +
            "room VARCHAR(50), " +
            "doctor VARCHAR(100), " +
            "address VARCHAR(255))",

            // Table Outpatients (Rawat Jalan)
            "CREATE TABLE IF NOT EXISTS outpatients (" +
            "patient_number VARCHAR(20) PRIMARY KEY, " +
            "name VARCHAR(100) NOT NULL, " +
            "complaint VARCHAR(255), " +
            "schedule DATETIME, " +
            "doctor VARCHAR(100), " +
            "status VARCHAR(50) DEFAULT 'Menunggu')",

            // Table Queues (Antrian) - identity PK
            "CREATE TABLE IF NOT EXISTS queues (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "patient_number VARCHAR(20) NOT NULL, " +
            "queue_number INT NOT NULL, " +
            "patient_name VARCHAR(100) NOT NULL, " +
            "complaint VARCHAR(255), " +
            "arrival_time DATETIME, " +
            "doctor_name VARCHAR(100), " +
            "status VARCHAR(20) DEFAULT 'Menunggu', " +
            "UNIQUE KEY uq_queues_patient_number (patient_number))",

            // Table Room Bookings
            "CREATE TABLE IF NOT EXISTS bookings (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "patient_name VARCHAR(100) NOT NULL, " +
            "illness VARCHAR(100), " +
            "address VARCHAR(255), " +
            "room_name VARCHAR(50), " +
            "room_type VARCHAR(50), " +
            "check_in DATE, " +
            "check_out DATE, " +
            "days INT, " +
            "cost DOUBLE)",

            // Table Drugs (Obat)
            "CREATE TABLE IF NOT EXISTS drugs (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "name VARCHAR(100) NOT NULL, " +
            "category VARCHAR(50), " +
            "form VARCHAR(50), " +
            "price DOUBLE)",

            // Table Drug Orders (Transaksi Obat)
            "CREATE TABLE IF NOT EXISTS drug_orders (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "patient_name VARCHAR(100), " +
            "patient_number VARCHAR(20), " +
            "total_price DOUBLE, " +
            "order_date DATETIME DEFAULT CURRENT_TIMESTAMP)"
            ,

            // Table Password Resets (OTP)
            "CREATE TABLE IF NOT EXISTS password_resets (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "email VARCHAR(100) NOT NULL, " +
            "otp_hash VARCHAR(255) NOT NULL, " +
            "expires_at DATETIME NOT NULL, " +
            "used TINYINT(1) DEFAULT 0, " +
            "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
            "INDEX (email))"
        };

        try (Connection conn = getConnection()) {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                for (String sql : sqlStatements) {
                    stmt.execute(sql);
                }
                System.out.println("Tabel berhasil dibuat atau sudah ada.");
                
                // Ensure schema updates (migration)
                ensureEmailColumnExists(stmt);
                ensurePhoneColumnExists(stmt);
                ensureQueueExtraColumnsExists(stmt);
                // Migrate existing 'queues' table to use patient_number PK and drop legacy patient_rm
                migrateQueuesPatientNumber(stmt);
                ensureAddressColumnExists(stmt);
                ensureOutpatientStatusColumnExists(stmt);

                // Seed initial data if empty
                seedUsers(stmt);
                seedDrugs(stmt);
                
            } else {
                System.out.println("Koneksi ke database gagal. Pastikan MySQL aktif dan database 'nasihuy_hospital' sudah dibuat.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void ensureEmailColumnExists(Statement stmt) {
        try {
            // Cek apakah kolom email sudah ada
            java.sql.ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM users LIKE 'email'");
            if (!rs.next()) {
                // Jika tidak ada, tambahkan kolom
                stmt.execute("ALTER TABLE users ADD COLUMN email VARCHAR(100) AFTER username");
                System.out.println("Kolom 'email' berhasil ditambahkan ke tabel users.");
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengecek/menambahkan kolom email: " + e.getMessage());
        }
    }

    private static void seedUsers(Statement stmt) throws SQLException {
        // Cek apakah user admin sudah ada
        if (!stmt.executeQuery("SELECT * FROM users WHERE username='admin'").next()) {
            String hashed = projectpbo.AccountService.hashPassword("admin123");
            stmt.execute("INSERT INTO users (username, password, role) VALUES ('admin', '" + hashed + "', 'admin')");
            System.out.println("User admin default ditambahkan (user: admin, pass: admin123).");
        }
    }

    private static void ensurePhoneColumnExists(Statement stmt) {
        try {
            java.sql.ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM users LIKE 'phone'");
            if (!rs.next()) {
                stmt.execute("ALTER TABLE users ADD COLUMN phone VARCHAR(20) AFTER email");
                System.out.println("Kolom 'phone' berhasil ditambahkan ke tabel users.");
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengecek/menambahkan kolom phone: " + e.getMessage());
        }
    }

    private static void ensureQueueExtraColumnsExists(Statement stmt) {
        try {
            // complaint
            java.sql.ResultSet rs1 = stmt.executeQuery("SHOW COLUMNS FROM queues LIKE 'complaint'");
            if (!rs1.next()) {
                stmt.execute("ALTER TABLE queues ADD COLUMN complaint VARCHAR(255) AFTER patient_name");
                System.out.println("Kolom 'complaint' ditambahkan ke tabel queues.");
            }
            // arrival_time
            java.sql.ResultSet rs2 = stmt.executeQuery("SHOW COLUMNS FROM queues LIKE 'arrival_time'");
            if (!rs2.next()) {
                stmt.execute("ALTER TABLE queues ADD COLUMN arrival_time DATETIME AFTER complaint");
                System.out.println("Kolom 'arrival_time' ditambahkan ke tabel queues.");
            }
            // phone
            java.sql.ResultSet rs4 = stmt.executeQuery("SHOW COLUMNS FROM queues LIKE 'phone'");
            if (!rs4.next()) {
                stmt.execute("ALTER TABLE queues ADD COLUMN phone VARCHAR(20) AFTER patient_name");
                System.out.println("Kolom 'phone' ditambahkan ke tabel queues.");
            }
            // email
            java.sql.ResultSet rs5 = stmt.executeQuery("SHOW COLUMNS FROM queues LIKE 'email'");
            if (!rs5.next()) {
                stmt.execute("ALTER TABLE queues ADD COLUMN email VARCHAR(100) AFTER phone");
                System.out.println("Kolom 'email' ditambahkan ke tabel queues.");
            }
            // address
            java.sql.ResultSet rs6 = stmt.executeQuery("SHOW COLUMNS FROM queues LIKE 'address'");
            if (!rs6.next()) {
                stmt.execute("ALTER TABLE queues ADD COLUMN address VARCHAR(255) AFTER email");
                System.out.println("Kolom 'address' ditambahkan ke tabel queues.");
            }
            // symptoms
            java.sql.ResultSet rs7 = stmt.executeQuery("SHOW COLUMNS FROM queues LIKE 'symptoms'");
            if (!rs7.next()) {
                stmt.execute("ALTER TABLE queues ADD COLUMN symptoms VARCHAR(255) AFTER address");
                System.out.println("Kolom 'symptoms' ditambahkan ke tabel queues.");
            }
            // registration_time
            java.sql.ResultSet rs9 = stmt.executeQuery("SHOW COLUMNS FROM queues LIKE 'registration_time'");
            if (!rs9.next()) {
                stmt.execute("ALTER TABLE queues ADD COLUMN registration_time DATETIME DEFAULT CURRENT_TIMESTAMP AFTER phone");
                System.out.println("Kolom 'registration_time' ditambahkan ke tabel queues.");
            }
        } catch (SQLException e) {
            System.err.println("Gagal migrasi tambahan kolom queues: " + e.getMessage());
        }
    }

    private static void ensureAddressColumnExists(Statement stmt) {
        try {
            java.sql.ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM inpatients LIKE 'address'");
            if (!rs.next()) {
                stmt.execute("ALTER TABLE inpatients ADD COLUMN address VARCHAR(255)");
                System.out.println("Kolom 'address' ditambahkan ke tabel inpatients.");
            }
        } catch (SQLException e) {
            System.err.println("Gagal cek/tambah kolom address: " + e.getMessage());
        }
    }

    // Ensure existing databases migrate from legacy patient_rm to patient_number as PK
    private static void migrateQueuesPatientNumber(Statement stmt) {
        try {
            // 1) Ensure patient_number column exists
            ResultSet cNum = stmt.executeQuery("SHOW COLUMNS FROM queues LIKE 'patient_number'");
            if (!cNum.next()) {
                stmt.execute("ALTER TABLE queues ADD COLUMN patient_number VARCHAR(20)");
                System.out.println("Kolom 'patient_number' ditambahkan (migrasi).");
            }

            // 2) If legacy patient_rm exists, backfill patient_number
            boolean hasPrm = false;
            ResultSet cRm = stmt.executeQuery("SHOW COLUMNS FROM queues LIKE 'patient_rm'");
            if (cRm.next()) {
                hasPrm = true;
                stmt.executeUpdate(
                    "UPDATE queues SET patient_number = patient_rm " +
                    "WHERE (patient_number IS NULL OR patient_number = '') AND patient_rm IS NOT NULL AND patient_rm <> ''"
                );
            }

            // 3) Ensure identity 'id' PK exists
            ResultSet cId = stmt.executeQuery("SHOW COLUMNS FROM queues LIKE 'id'");
            if (!cId.next()) {
                // Add id column first
                stmt.execute("ALTER TABLE queues ADD COLUMN id INT NULL");
            }

            // Drop existing PK (whether on patient_number or others) and create PK on id
            try { stmt.execute("ALTER TABLE queues DROP PRIMARY KEY"); } catch (SQLException ignore) { }
            // Ensure id is auto increment and not null
            try { stmt.execute("ALTER TABLE queues MODIFY id INT NOT NULL AUTO_INCREMENT"); } catch (SQLException ignore) { }
            try { stmt.execute("ALTER TABLE queues ADD PRIMARY KEY (id)"); } catch (SQLException ignore) { }

            // 4) Enforce constraints for patient_number
            try { stmt.execute("ALTER TABLE queues MODIFY patient_number VARCHAR(20) NOT NULL"); } catch (SQLException ignore) { }
            try { stmt.execute("CREATE UNIQUE INDEX uq_queues_patient_number ON queues(patient_number)"); } catch (SQLException ignore) { }

            // 5) Drop or relax legacy patient_rm to avoid NOT NULL errors in strict mode
            if (hasPrm) {
                try { stmt.execute("ALTER TABLE queues MODIFY patient_rm VARCHAR(20) NULL"); } catch (SQLException ignore) { }
                try { stmt.execute("ALTER TABLE queues DROP COLUMN patient_rm"); } catch (SQLException ignore) { }
            }
        } catch (SQLException e) {
            System.err.println("Gagal migrasi queues ke patient_number: " + e.getMessage());
        }
    }

    private static void seedDrugs(Statement stmt) throws SQLException {
        // Hapus semua data lama terlebih dahulu
        try {
            stmt.execute("DELETE FROM drugs");
        } catch (SQLException e) {
            // Abaikan jika tabel kosong atau error
        }

        String[] drugs = {
            "('Paracetamol 500 mg', 'Analgesic', 'Tablet', 2000.0)",
            "('Ibuprofen 200 mg', 'Analgesic', 'Tablet', 3500.0)",
            "('Mefenamic Acid 500 mg', 'Analgesic', 'Tablet', 5000.0)",
            "('Ketorolac Inj 30 mg', 'Analgesic', 'Injection', 25000.0)",
            "('Amoxicillin 500 mg', 'Antibiotic', 'Tablet', 4000.0)",
            "('Ciprofloxacin 500 mg', 'Antibiotic', 'Tablet', 8000.0)",
            "('Cefixime 100 mg', 'Antibiotic', 'Tablet', 10000.0)",
            "('Ceftriaxone 1g Inj', 'Antibiotic', 'Injection', 50000.0)",
            "('Antasida Doen', 'Stomach Medicine', 'Tablet', 3000.0)",
            "('Omeprazole 20 mg', 'Stomach Medicine', 'Capsule', 7000.0)",
            "('Ranitidine 150 mg', 'Stomach Medicine', 'Tablet', 4500.0)",
            "('Cetirizine 10 mg', 'Allergy Medicine', 'Tablet', 4000.0)",
            "('Loratadine 10 mg', 'Allergy Medicine', 'Tablet', 6000.0)",
            "('CTM 4 mg', 'Allergy Medicine', 'Tablet', 1500.0)",
            "('Vitamin C 500 mg', 'Vitamins', 'Tablet', 2500.0)",
            "('Vitamin B Complex', 'Vitamins', 'Tablet', 3000.0)",
            "('OBH Syrup', 'Cough & Flu', 'Syrup', 15000.0)",
            "('Guaifenesin 100 mg', 'Cough & Flu', 'Tablet', 4000.0)",
            "('NaCl 0.9% 500ml', 'Infusion Fluids', 'Infusion', 12000.0)",
            "('Ringer Lactate 500ml', 'Infusion Fluids', 'Infusion', 15000.0)",
            "('D5% 500ml', 'Infusion Fluids', 'Infusion', 10000.0)"
        };
        for (String d : drugs) {
            stmt.execute("INSERT INTO drugs (name, category, form, price) VALUES " + d);
        }
    }

    public static boolean login(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String stored = rs.getString(1);
                    // If stored is BCrypt, verify normally
                    if (projectpbo.AccountService.isBcryptHash(stored)) {
                        return projectpbo.AccountService.verifyPassword(password, stored);
                    }
                    // Legacy plaintext: compare directly, then migrate to BCrypt on success
                    if (stored != null && stored.equals(password)) {
                        String newHash = projectpbo.AccountService.hashPassword(password);
                        try (java.sql.PreparedStatement up = conn.prepareStatement(
                                "UPDATE users SET password=? WHERE username=?")) {
                            up.setString(1, newHash);
                            up.setString(2, username);
                            up.executeUpdate();
                        }
                        return true;
                    }
                    return false;
                }
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean register(String username, String password, String role) {
        return register(username, null, null, password, role);
    }

    public static boolean register(String username, String email, String password, String role) {
        return register(username, email, null, password, role);
    }

    public static boolean register(String username, String email, String phone, String password, String role) {
        String sql = "INSERT INTO users (username, email, phone, password, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            String hashed = projectpbo.AccountService.hashPassword(password);
            pstmt.setString(4, hashed);
            pstmt.setString(5, role);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Register failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isEmailRegistered(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isUsernameRegistered(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updatePassword(String email, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        try (Connection conn = getConnection();
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String hashed = projectpbo.AccountService.hashPassword(newPassword);
            pstmt.setString(1, hashed);
            pstmt.setString(2, email);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean savePasswordResetOtp(String email, String otpHash, java.time.LocalDateTime expiresAt) {
        String sql = "INSERT INTO password_resets (email, otp_hash, expires_at, used) VALUES (?, ?, ?, 0)";
        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, otpHash);
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(expiresAt));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean verifyAndConsumeOtp(String email, String otp) {
        String select = "SELECT id, otp_hash, expires_at, used FROM password_resets WHERE email = ? ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setString(1, email);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                int id = rs.getInt("id");
                String otpHash = rs.getString("otp_hash");
                java.sql.Timestamp expires = rs.getTimestamp("expires_at");
                boolean used = rs.getBoolean("used");
                if (used) {
                    return false;
                }
                if (expires.toInstant().isBefore(java.time.Instant.now())) {
                    return false;
                }
                boolean matched = projectpbo.AccountService.verifyPassword(otp, otpHash);
                if (!matched) {
                    return false;
                }
                // mark used
                try (java.sql.PreparedStatement upd = conn.prepareStatement("UPDATE password_resets SET used=1 WHERE id=?")) {
                    upd.setInt(1, id);
                    upd.executeUpdate();
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void ensureOutpatientStatusColumnExists() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            ensureOutpatientStatusColumnExists(stmt);
        } catch (SQLException e) {
            System.err.println("Error ensuring outpatient status column: " + e.getMessage());
        }
    }

    public static void ensureOutpatientStatusColumnExists(Statement stmt) {
        String sql = "ALTER TABLE outpatients ADD COLUMN status VARCHAR(50) DEFAULT 'Menunggu'";
        try {
            // Check if column exists first to avoid error, or just try-catch the specific error
            // Simple way: try to select the column, if fails, add it.
            // Or just run ALTER and catch exception if it exists.
            // Better: Query information_schema or just try-catch.
            try {
                stmt.executeQuery("SELECT status FROM outpatients LIMIT 1");
            } catch (SQLException e) {
                // Column likely doesn't exist
                System.out.println("Adding 'status' column to outpatients table...");
                stmt.executeUpdate(sql);
                System.out.println("Column 'status' added successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error checking/adding status column: " + e.getMessage());
        }
    }

    public static void migrateToNaturalKeys() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Migrate Outpatients
            try {
                // Check if id column exists
                ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM outpatients LIKE 'id'");
                if (rs.next()) {
                    System.out.println("Migrating outpatients to natural key...");
                    // We need to handle duplicates before adding PK constraint if any
                    // For simplicity in this dev env, we might truncate or assume user handles it.
                    // But let's try to just alter.
                    stmt.executeUpdate("ALTER TABLE outpatients MODIFY id INT"); // Remove auto_increment
                    stmt.executeUpdate("ALTER TABLE outpatients DROP PRIMARY KEY");
                    stmt.executeUpdate("ALTER TABLE outpatients DROP COLUMN id");
                    stmt.executeUpdate("ALTER TABLE outpatients ADD PRIMARY KEY (patient_number)");
                    System.out.println("Migrated outpatients.");
                }
            } catch (SQLException e) {
                System.err.println("Error migrating outpatients: " + e.getMessage());
            }

            // Migrate Inpatients
            try {
                ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM inpatients LIKE 'id'");
                if (rs.next()) {
                    System.out.println("Migrating inpatients to natural key...");
                    stmt.executeUpdate("ALTER TABLE inpatients MODIFY id INT");
                    stmt.executeUpdate("ALTER TABLE inpatients DROP PRIMARY KEY");
                    stmt.executeUpdate("ALTER TABLE inpatients DROP COLUMN id");
                    stmt.executeUpdate("ALTER TABLE inpatients ADD PRIMARY KEY (patient_number)");
                    System.out.println("Migrated inpatients.");
                }
            } catch (SQLException e) {
                System.err.println("Error migrating inpatients: " + e.getMessage());
            }

            // Migrate Queues: keep identity PK on id, ensure unique patient_number
            try {
                // Ensure id exists and is PK
                ResultSet rsId = stmt.executeQuery("SHOW COLUMNS FROM queues LIKE 'id'");
                if (!rsId.next()) {
                    stmt.executeUpdate("ALTER TABLE queues ADD COLUMN id INT NOT NULL AUTO_INCREMENT PRIMARY KEY");
                } else {
                    try { stmt.executeUpdate("ALTER TABLE queues DROP PRIMARY KEY"); } catch (SQLException ignore) { }
                    try { stmt.executeUpdate("ALTER TABLE queues MODIFY id INT NOT NULL AUTO_INCREMENT"); } catch (SQLException ignore) { }
                    try { stmt.executeUpdate("ALTER TABLE queues ADD PRIMARY KEY (id)"); } catch (SQLException ignore) { }
                }
                try { stmt.executeUpdate("ALTER TABLE queues MODIFY patient_number VARCHAR(20) NOT NULL"); } catch (SQLException ignore) { }
                try { stmt.executeUpdate("CREATE UNIQUE INDEX uq_queues_patient_number ON queues(patient_number)"); } catch (SQLException ignore) { }
            } catch (SQLException e) {
                System.err.println("Error migrating queues (identity): " + e.getMessage());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
