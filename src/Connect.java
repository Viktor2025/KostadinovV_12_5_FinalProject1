import java.sql.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Image;
import javax.swing.ImageIcon;
import org.mindrot.jbcrypt.BCrypt;

public class Connect {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/candlee_shop";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "viktor"; // Change if needed

    public static String loggedInEmail = null;

    // 🛒 Shared cart between classes
    public static List<String> cartItems = new ArrayList<>();

    // 🔗 Connect to database
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // 🔐 Register new user
    public static boolean registerUser(String username, String email, String password) {
        String salt = BCrypt.gensalt();
        String encryptedPassword = BCrypt.hashpw(password, salt);

        String sql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, encryptedPassword);
            return stmt.executeUpdate() > 0;

        } catch (SQLException ex) {
            if (ex.getSQLState().startsWith("23")) {
                System.out.println("Email or username already in use.");
            } else {
                System.err.println("Registration error: " + ex.getMessage());
            }
            return false;
        }
    }

    // 🔐 Login existing user
    public static boolean loginUser(String email, String password) {
        String sql = "SELECT password_hash FROM users WHERE email = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashed = rs.getString("password_hash");
                if (BCrypt.checkpw(password, hashed)) {
                    loggedInEmail = email;
                    return true;
                }
            }

        } catch (SQLException ex) {
            System.err.println("Login error: " + ex.getMessage());
        }

        return false;
    }

    // 📤 Upload image from file into database (BLOB)
    public static void uploadImageToLabel(String labelName, String imagePath) {
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE product_catalog_labels SET image_data = ? WHERE label_name = ?")) {

            File file = new File(imagePath);
            try (FileInputStream fis = new FileInputStream(file)) {
                stmt.setBinaryStream(1, fis, (int) file.length());
                stmt.setString(2, labelName);
                stmt.executeUpdate();
                System.out.println("✅ Image uploaded for: " + labelName);
            }
        } catch (Exception e) {
            System.err.println("❌ Image upload failed: " + e.getMessage());
        }
    }

    // 📥 Load catalog labels and images
    public static ResultSet loadProductCatalogData() throws SQLException {
        String sql = "SELECT label_name, label_text, image_data FROM product_catalog_labels";
        Connection conn = connect(); // ResultSet stays open
        PreparedStatement stmt = conn.prepareStatement(sql);
        return stmt.executeQuery();
    }

    // 📥 Load labels for Login / Register
    public static ResultSet loadLabels(String tableName) throws SQLException {
        String sql = "SELECT label_name, label_text FROM " + tableName;
        Connection conn = connect();
        PreparedStatement stmt = conn.prepareStatement(sql);
        return stmt.executeQuery();
    }

    // 🖼️ Load single image from DB as ImageIcon (used in GUIs)
    public static ImageIcon getImageIcon(String labelName, int width, int height) {
        String sql = "SELECT image_data FROM product_catalog_labels WHERE label_name = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, labelName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                InputStream imgData = rs.getBinaryStream("image_data");
                if (imgData != null) {
                    BufferedImage img = ImageIO.read(imgData);
                    if (img != null) {
                        Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaled);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Image load error for label '" + labelName + "': " + e.getMessage());
        }

        return null;
    }
}