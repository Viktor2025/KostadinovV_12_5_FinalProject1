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

    /**
     * Constructor to set up the Home Page UI.
     */
    public HomePage1() {
        setTitle("Home Page");
        setContentPane(panel1);
        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeComponents();
        setVisible(true);
    }

    /**
     * Initializes components like loading labels and setting button actions.
     */
    private void initializeComponents() {
        // Load labels and images dynamically
        loadHomePageLabelsFromDB();

        // Button action to view catalog
        VIEWCATALOGButton.addActionListener(e -> {
            dispose();
            new ProductCatalog();
        });
    }

    /**
     * Loads labels and images from the database for the homepage.
     */
    private void loadHomePageLabelsFromDB() {
        String query = "SELECT label_name, label_text, image_data FROM homepage_labels";
        try (Connection conn = Connect.connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String labelName = rs.getString("label_name");
                String labelText = rs.getString("label_text");
                InputStream imgData = rs.getBinaryStream("image_data");

                assignLabelContent(labelName, labelText, imgData);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "❌ Error loading homepage data:\n" + e.getMessage());
        }
    }

    /**
     * Assigns text and images to the appropriate labels based on label name.
     *
     * @param labelName name of the label
     * @param text text content
     * @param imgData image input stream
     */
    private void assignLabelContent(String labelName, String text, InputStream imgData) {
        switch (labelName) {
            case "Title" -> setLabelContent(Title, text, imgData);
            case "UnderTitle" -> setLabelContent(UnderTitle, text, imgData);
            case "picture1" -> setLabelContent(picture1, text, imgData);
            case "picture2" -> setLabelContent(picture2, text, imgData);
            case "pricelabel" -> setLabelContent(pricelabel, text, imgData);
            case "message" -> setLabelContent(message, text, imgData);
            default -> System.out.println("⚠️ Unknown label found: " + labelName);
        }
    }

    /**
     * Helper method to apply both text and images to JLabel.
     *
     * @param label JLabel to update
     * @param text Text to set
     * @param imgData Image input stream
     */
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

                    // Force layout update
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