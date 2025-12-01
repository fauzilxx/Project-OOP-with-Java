package projectpbo;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.mindrot.jbcrypt.BCrypt;

public class AccountService {

    // Password hashing (BCrypt)
    public static String hashPassword(String plain) {
        if (plain == null) {
            return null;
        }
        return BCrypt.hashpw(plain, BCrypt.gensalt(10));
    }

    public static boolean verifyPassword(String plain, String hashed) {
        if (plain == null || hashed == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plain, hashed);
        } catch (IllegalArgumentException ex) {
            // Occurs if 'hashed' is not a valid BCrypt hash (legacy plaintext in DB)
            return false;
        }
    }

    // Detect whether a string looks like a BCrypt hash
    public static boolean isBcryptHash(String value) {
        if (value == null) {
            return false;
        }
        return value.matches("^\\$2[aby]\\$\\d{2}\\$.*");
    }

    // OTP generation (6 digits)
    public static String generateOtp() {
        Random r = new Random();
        int n = 100000 + r.nextInt(900000);
        return String.valueOf(n);
    }

    // Request password reset: generate OTP, store hashed, send email
    public static boolean requestPasswordReset(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        if (!DBConnection.isEmailRegistered(email)) {
            return false;
        }

        String otp = generateOtp();
        String otpHash = hashPassword(otp);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);
        boolean saved = DBConnection.savePasswordResetOtp(email, otpHash, expiresAt);
        if (!saved) {
            return false;
        }
        return sendOtpEmail(email, otp, expiresAt);
    }

    // Reset password with OTP
    public static boolean resetPassword(String email, String otp, String newPassword) {
        if (email == null || otp == null || newPassword == null) {
            return false;
        }
        boolean ok = DBConnection.verifyAndConsumeOtp(email, otp);
        if (!ok) {
            return false;
        }
        return DBConnection.updatePassword(email, newPassword);
    }

    // Email sending via JavaMail with SMTP configuration from environment variables
    // Required ENV: SMTP_HOST, SMTP_PORT(587), SMTP_USER, SMTP_PASS, SMTP_FROM
    // If SMTP_FROM missing, default to SMTP_USER. TLS is enabled by default.
    public static boolean sendOtpEmail(String toEmail, String otp, LocalDateTime expiresAt) {
        try {
            String host = envOr("SMTP_HOST", "smtp.gmail.com");
            String port = envOr("SMTP_PORT", "587");
            String user = envOr("SMTP_USER", "fauzilazhim12@student.uns.ac.id");
            String pass = envOr("SMTP_PASS", null);
            String from = envOr("SMTP_FROM", user);

            if (host == null || pass == null) {
                System.err.println("SMTP configuration missing. Set SMTP_HOST, SMTP_USER, SMTP_PASS via Environment Variables (Control Panel > System > Advanced > Environment Variables). Tidak mengirim email.");
                return false;
            }

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Kode OTP Reset Password - Nasihuy Hospital");
            String body = "Halo,\n\n" +
                    "Berikut adalah kode OTP untuk reset password Anda: " + otp + "\n" +
                    "Kode berlaku hingga: " + expiresAt + "\n\n" +
                    "Jangan bagikan kode ini kepada siapa pun.\n\n" +
                    "Terima kasih.";
            message.setText(body);

            Transport.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String envOr(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }
}
