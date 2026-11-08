import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Login extends JFrame {

    private JTextField userTextField;
    private JPasswordField passwordField;

    public Login() {
        setTitle("PawConnect - Login");
        setSize(420, 320);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(new Color(245, 247, 250));

        // --- Header Panel ---
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(null);
        headerPanel.setBounds(0, 0, 420, 60);
        headerPanel.setBackground(new Color(40, 167, 69));
        add(headerPanel);

        JLabel titleLabel = new JLabel("PawConnect Login");
        titleLabel.setBounds(130, 15, 200, 30);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(titleLabel);

        // --- Form Components ---
        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(50, 90, 100, 25);
        userLabel.setFont(new Font("Arial", Font.BOLD, 13));
        add(userLabel);

        userTextField = new JTextField();
        userTextField.setBounds(150, 90, 200, 28);
        add(userTextField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(50, 140, 100, 25);
        passLabel.setFont(new Font("Arial", Font.BOLD, 13));
        add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(150, 140, 200, 28);
        add(passLabel); // Wait, fix bounds/add properly
        // Let's correct passwordField add:
        add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(150, 200, 200, 35);
        loginButton.setBackground(new Color(0, 123, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        add(loginButton);

        // Login Action Listener
        loginButton.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String username = userTextField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String dbUsername = rs.getString("username");
                JOptionPane.showMessageDialog(this, "Login Successful! Welcome, " + dbUsername + " (USER)");

                // Open UserDashboard and close Login window
                this.dispose();
                new UserDashboard(dbUsername).setVisible(true);

            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Login().setVisible(true);
        });
    }
}