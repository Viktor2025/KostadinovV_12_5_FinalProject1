import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class ProductCatalog extends JFrame {
    private JButton Cart;
    private JButton addToCartButton2;
    private JButton addToCartButton3;
    private JButton addToCartButton1;
    private JPanel panel1;
    private JLabel candle1;
    private JLabel candle2;
    private JLabel candle3;
    private JLabel description2;
    private JLabel description1;
    private JLabel description3;
    private JLabel description4;
    private JLabel description5;
    private JLabel description6;
    private JLabel price1;
    private JLabel price2;
    private JLabel price3;
    private JLabel title2;
    private JLabel title1;

    public ProductCatalog() {
        setTitle("Product Catalog");
        setContentPane(panel1);
        setSize(750, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ✅ Load labels and images from database
        loadCatalogLabelsFromDB();

        // ✅ Add to Cart button actions
        addToCartButton1.addActionListener(e -> {
            Connect.cartItems.add("Smoke Dreams - " + price1.getText());
            JOptionPane.showMessageDialog(this, "Smoke Dreams added to cart!");
        });

        addToCartButton2.addActionListener(e -> {
            Connect.cartItems.add("Amber Light - " + price2.getText());
            JOptionPane.showMessageDialog(this, "Amber Light added to cart!");
        });

        addToCartButton3.addActionListener(e -> {
            Connect.cartItems.add("Frosted Garden - " + price3.getText());
            JOptionPane.showMessageDialog(this, "Frosted Garden added to cart!");
        });

        Cart.addActionListener(e -> new Checkout());

        setVisible(true);
    }

    // ✅ Load labels and images method
    private void loadCatalogLabelsFromDB() {
        try (Connection conn = Connect.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT label_name, label_text, image_data FROM product_catalog_labels");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String labelName = rs.getString("label_name");
                String labelText = rs.getString("label_text");
                InputStream imgData = rs.getBinaryStream("image_data");

                switch (labelName) {
                    case "title1" -> setLabelContent(title1, labelText, imgData);
                    case "title2" -> setLabelContent(title2, labelText, imgData);
                    case "candle1" -> setLabelContent(candle1, labelText, imgData);
                    case "candle2" -> setLabelContent(candle2, labelText, imgData);
                    case "candle3" -> setLabelContent(candle3, labelText, imgData);
                    case "price1" -> setLabelContent(price1, labelText, imgData);
                    case "price2" -> setLabelContent(price2, labelText, imgData);
                    case "price3" -> setLabelContent(price3, labelText, imgData);
                    case "description1" -> setLabelContent(description1, labelText, imgData);
                    case "description2" -> setLabelContent(description2, labelText, imgData);
                    case "description3" -> setLabelContent(description3, labelText, imgData);
                    case "description4" -> setLabelContent(description4, labelText, imgData);
                    case "description5" -> setLabelContent(description5, labelText, imgData);
                    case "description6" -> setLabelContent(description6, labelText, imgData);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "❌ Error loading catalog data:\n" + e.getMessage());
        }
    }

    // ✅ Helper method to set text and icon
    private void setLabelContent(JLabel label, String text, InputStream imgData) {
        if (text != null && !text.isEmpty()) {
            label.setText(text);
        }
        if (imgData != null) {
            try {
                BufferedImage img = ImageIO.read(imgData);
                if (img != null) {
                    Image scaled = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    label.setIcon(new ImageIcon(scaled));

                    // 👉 Important to make label visible and sized properly
                    label.setPreferredSize(new Dimension(100, 100));
                    label.revalidate();
                    label.repaint();
                }
            } catch (Exception ex) {
                System.err.println("⚠️ Error setting image for label: " + ex.getMessage());
            }
        }
    }
}