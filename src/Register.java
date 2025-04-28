import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;

/**
 * Register class - allows new users to sign up.
 */
public class Register extends JFrame {
    private JPanel panel1;
    private JTextField textField2; // Username
    private JTextField textField3; // Email
    private JPasswordField passwordField1; // Password
    private JButton registerButton;
    private JButton tapHereToLogButton;
    private JLabel registerLabel;
    private JLabel Username;
    private JLabel Email;
    private JLabel Password;
    private JLabel alreadyRegistered;

    /**
     * Constructor to initialize the registration window.
     */
    public Register() {
        setTitle("Register Now!");
        setContentPane(panel1);
        setSize(450, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeComponents();
        setVisible(true);
    }

    /**
     * Initializes components and loads labels dynamically from the database.
     */
    private void initializeComponents() {
        loadRegisterLabels();

        // Button texts
        registerButton.setText("Register");
        tapHereToLogButton.setText("Tap here to log in!");

        // Button actions
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });

        tapHereToLogButton.addActionListener(e -> {
            dispose();
            new Login();
        });
    }

    /**
     * Loads labels dynamically for the Register form from the database.
     */
    private void loadRegisterLabels() {
        try {
            ResultSet rs = Connect.loadLabels("register_labels");
            while (rs.next()) {
                String name = rs.getString("label_name");
                String text = rs.getString("label_text");

                switch (name) {
                    case "registerLabel" -> registerLabel.setText(text);
                    case "username" -> Username.setText(text + ":");
                    case "email" -> Email.setText(text + ":");
                    case "password" -> Password.setText(text + ":");
                    case "alreadyRegistered" -> alreadyRegistered.setText(text);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Could not load register labels:\n" + e.getMessage());
        }

        // Font styling
        registerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        Username.setFont(new Font("SansSerif", Font.PLAIN, 14));
        Email.setFont(new Font("SansSerif", Font.PLAIN, 14));
        Password.setFont(new Font("SansSerif", Font.PLAIN, 14));
        alreadyRegistered.setFont(new Font("SansSerif", Font.ITALIC, 12));
    }

    /**
     * Handles the register button click: validates inputs and registers the user.
     */
    private void handleRegister() {
        String username = textField2.getText().trim();
        String email = textField3.getText().trim();
        String password = String.valueOf(passwordField1.getPassword()).trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be completed.");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Enter a valid email address.");
            return;
        }

        if (Connect.registerUser(username, email, password)) {
            JOptionPane.showMessageDialog(this, "✅ Registration successful!");
            dispose();
            new Login();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Registration failed. Email or username may already be in use.");
        }
    }
}