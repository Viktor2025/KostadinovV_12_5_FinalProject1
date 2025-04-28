import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.sql.*;
import javax.imageio.ImageIO;

public class HomePage1 extends JFrame {
    private JPanel panel1;
    private JButton VIEWCATALOGButton;
    private JLabel Title;
    private JLabel UnderTitle;
    private JLabel picture1;
    private JLabel picture2;
    private JLabel pricelabel;
    private JLabel message;

    public HomePage1() {
        setTitle("Home Page");
        setContentPane(panel1);
        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ✅ Load labels and images from database
        loadHomePageLabelsFromDB();

        // ✅ Button to view catalog
        VIEWCATALOGButton.addActionListener(e -> {
            dispose();
            new ProductCatalog();
        });

        setVisible(true);
    }

    // ✅ Load labels and images method
    private void loadHomePageLabelsFromDB() {
        try (Connection conn = Connect.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT label_name, label_text, image_data FROM homepage_labels");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String labelName = rs.getString("label_name");
                String labelText = rs.getString("label_text");
                InputStream imgData = rs.getBinaryStream("image_data");

                switch (labelName) {
                    case "Title" -> setLabelContent(Title, labelText, imgData);
                    case "UnderTitle" -> setLabelContent(UnderTitle, labelText, imgData);
                    case "picture1" -> setLabelContent(picture1, labelText, imgData);
                    case "picture2" -> setLabelContent(picture2, labelText, imgData);
                    case "pricelabel" -> setLabelContent(pricelabel, labelText, imgData);
                    case "message" -> setLabelContent(message, labelText, imgData);
                    default -> System.out.println("⚠️ Unknown label found: " + labelName);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "❌ Error loading homepage data:\n" + e.getMessage());
        }
    }

    // ✅ Helper method to set text and icon
    private void setLabelContent(JLabel label, String text, InputStream imgData) {
        if (label == null) return;

        if (text != null && !text.isEmpty()) {
            label.setText(text);
        }
        if (imgData != null) {
            try {
                BufferedImage img = ImageIO.read(imgData);
                if (img != null) {
                    Image scaled = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    label.setIcon(new ImageIcon(scaled));

                    // ⚡ Force layout update
                    label.setPreferredSize(new Dimension(150, 150));
                    label.revalidate();
                    label.repaint();
                }
            } catch (Exception ex) {
                System.err.println("⚠️ Error setting image for label: " + ex.getMessage());
            }
        }
    }
}