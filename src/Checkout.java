import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.*;
import java.awt.*;
import java.sql.*;
import java.util.*;

public class Checkout extends JFrame {
    private JButton placeOrderButton;
    private JTable table1;
    private JTextField textField1;
    private JTextField textField2;
    private JPanel panel1;
    private JButton removeProductButton;
    private JLabel CheckOut;
    private JLabel shippingAddress;
    private JLabel Telephone;
    private JButton goBackShoppingButton;
    private JTextField searchField;
    private JLabel totalPriceLabel;
    private JComboBox<String> comboBox1;

    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private final Map<String, Double> productPrices = new HashMap<>();

    public Checkout() {
        setTitle("Checkout");
        setContentPane(panel1);
        setSize(600, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        loadCheckoutLabelsFromDB();
        setupTable();
        setupListeners();
        loadProductsIntoTable();

        updateTotalPrice();
        setVisible(true);
    }

    private void setupTable() {
        tableModel = new DefaultTableModel(new Object[]{"Product", "Quantity", "Total Price"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only quantity column editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 1 -> Integer.class;
                    case 2 -> Double.class;
                    default -> String.class;
                };
            }
        };

        table1.setModel(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table1.setRowSorter(sorter);

        comboBox1.addItem("Sort A-Z");
        comboBox1.addItem("Sort Z-A");
        comboBox1.addItem("Price Low-High");
        comboBox1.addItem("Price High-Low");
    }

    private void loadProductsIntoTable() {
        Map<String, Integer> productQuantities = new LinkedHashMap<>();

        for (String item : Connect.cartItems) {
            String[] parts = item.split("-");
            String productName = parts[0].trim();
            double price = Double.parseDouble(parts[1].replace("$", "").trim());

            productQuantities.put(productName, productQuantities.getOrDefault(productName, 0) + 1);
            productPrices.put(productName, price);
        }

        for (var entry : productQuantities.entrySet()) {
            String product = entry.getKey();
            int quantity = entry.getValue();
            double totalPrice = quantity * productPrices.get(product);
            tableModel.addRow(new Object[]{product, quantity, totalPrice});
        }
    }

    private void setupListeners() {
        tableModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 1) {
                int row = e.getFirstRow();
                updateRowTotal(row);
            }
        });

        removeProductButton.addActionListener(e -> {
            int selectedRow = table1.getSelectedRow();
            if (selectedRow >= 0) {
                tableModel.removeRow(selectedRow);
                updateTotalPrice();
            } else {
                JOptionPane.showMessageDialog(this, "Please select a product to remove.");
            }
        });

        placeOrderButton.addActionListener(e -> placeOrder());

        goBackShoppingButton.addActionListener(e -> dispose());

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String searchText = searchField.getText();
                sorter.setRowFilter(searchText.isEmpty() ? null : RowFilter.regexFilter("(?i)" + searchText));
            }
        });

        comboBox1.addActionListener(e -> applySorting());
    }

    private void updateRowTotal(int row) {
        try {
            int quantity = (Integer) tableModel.getValueAt(row, 1);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be at least 1.");
                tableModel.setValueAt(1, row, 1);
                quantity = 1;
            }

            String product = (String) tableModel.getValueAt(row, 0);
            double unitPrice = productPrices.get(product);
            tableModel.setValueAt(quantity * unitPrice, row, 2);

            updateTotalPrice();
        } catch (Exception ex) {
            System.err.println("⚠️ Error updating row total: " + ex.getMessage());
        }
    }

    private void placeOrder() {
        String address = textField1.getText().trim();
        String phone = textField2.getText().trim();

        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Your cart is empty!");
            return;
        }
        if (address.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in both address and phone.");
            return;
        }
        if (!phone.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Phone number must have exactly 10 digits!");
            return;
        }

        JPanel panel = buildOrderSummaryPanel(address, phone);

        String[] options = {"🏠 Go to Home", "❌ Exit"};
        int choice = JOptionPane.showOptionDialog(
                this, panel, "Order Details", JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, options, options[0]
        );

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new HomePage1();
        } else {
            System.exit(0);
        }

        Connect.cartItems.clear();
    }

    private JPanel buildOrderSummaryPanel(String address, String phone) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(350, 300));

        JLabel successLabel = new JLabel("✅ Order Placed Successfully!");
        successLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        successLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel addressLabel = new JLabel("📦 Shipping Address: " + address);
        JLabel phoneLabel = new JLabel("📞 Phone Number: " + phone);
        addressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        phoneLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(successLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(addressLabel);
        panel.add(phoneLabel);
        panel.add(Box.createVerticalStrut(15));

        JTextArea itemsArea = new JTextArea();
        itemsArea.setEditable(false);
        itemsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        double total = 0.0;
        StringBuilder itemText = new StringBuilder("🛒 Items Ordered:\n\n");
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String product = (String) tableModel.getValueAt(i, 0);
            int quantity = (Integer) tableModel.getValueAt(i, 1);
            double price = (Double) tableModel.getValueAt(i, 2);
            total += price;

            itemText.append("- ").append(product)
                    .append(" x").append(quantity)
                    .append(" = ").append(String.format("%.2f", price)).append("$\n");
        }
        itemText.append("\n💰 Total Price: ").append(String.format("%.2f", total)).append("$");

        itemsArea.setText(itemText.toString());

        JScrollPane scrollPane = new JScrollPane(itemsArea);
        scrollPane.setPreferredSize(new Dimension(300, 120));
        panel.add(scrollPane);

        return panel;
    }

    private void applySorting() {
        String selected = (String) comboBox1.getSelectedItem();
        if (selected == null) return;

        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        switch (selected) {
            case "Sort A-Z" -> sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
            case "Sort Z-A" -> sortKeys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));
            case "Price Low-High" -> sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
            case "Price High-Low" -> sortKeys.add(new RowSorter.SortKey(2, SortOrder.DESCENDING));
        }
        sorter.setSortKeys(sortKeys);
        sorter.sort();
    }

    private void updateTotalPrice() {
        double total = 0.0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            total += (Double) tableModel.getValueAt(i, 2);
        }
        totalPriceLabel.setText("Total: " + String.format("%.2f", total) + "$");
    }

    private void loadCheckoutLabelsFromDB() {
        try (Connection conn = Connect.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT label_name, label_text FROM checkout_labels");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("label_name");
                String text = rs.getString("label_text");
                switch (name) {
                    case "Checkout" -> CheckOut.setText(text);
                    case "shippingAddress" -> shippingAddress.setText(text);
                    case "Telephone" -> Telephone.setText(text);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "❌ Error loading checkout labels:\n" + e.getMessage());
        }
    }
}