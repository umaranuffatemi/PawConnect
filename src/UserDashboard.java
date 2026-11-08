import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDashboard extends JFrame {

    private String loggedInUser;
    private JTable petTable;
    private DefaultTableModel tableModel;

    // Filter Components
    private JTextField searchNameField;
    private JComboBox<String> typeComboBox;
    private JComboBox<String> genderComboBox;
    private JTextField locationField;

    public UserDashboard(String username) {
        this.loggedInUser = username;

        setTitle("PawConnect - User Dashboard");
        setSize(980, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(new Color(245, 247, 250));

        // --- Top Header Panel ---
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(null);
        headerPanel.setBounds(0, 0, 980, 70);
        headerPanel.setBackground(new Color(40, 167, 69));
        add(headerPanel);

        JLabel welcomeLabel = new JLabel("Welcome, " + loggedInUser + " (USER)!");
        welcomeLabel.setBounds(30, 20, 500, 30);
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(welcomeLabel);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBounds(830, 20, 100, 30);
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        headerPanel.add(logoutButton);

        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
                new Login().setVisible(true);
            }
        });

        // --- Filter & Action Panel ---
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(null);
        controlPanel.setBounds(20, 90, 925, 120);
        controlPanel.setBackground(Color.WHITE);
        controlPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                " Advanced Search & Filter Panel ",
                0, 0, new Font("Arial", Font.BOLD, 13), new Color(40, 167, 69)
        ));
        add(controlPanel);

        JLabel nameLbl = new JLabel("Keyword:");
        nameLbl.setBounds(20, 30, 70, 25);
        controlPanel.add(nameLbl);

        searchNameField = new JTextField();
        searchNameField.setBounds(90, 30, 140, 25);
        controlPanel.add(searchNameField);

        JLabel typeLbl = new JLabel("Type:");
        typeLbl.setBounds(245, 30, 45, 25);
        controlPanel.add(typeLbl);

        typeComboBox = new JComboBox<>(new String[]{"All", "Dog", "Cat", "Rabbit", "Bird"});
        typeComboBox.setBounds(290, 30, 110, 25);
        controlPanel.add(typeComboBox);

        JLabel genderLbl = new JLabel("Gender:");
        genderLbl.setBounds(415, 30, 55, 25);
        controlPanel.add(genderLbl);

        genderComboBox = new JComboBox<>(new String[]{"All", "Male", "Female"});
        genderComboBox.setBounds(475, 30, 100, 25);
        controlPanel.add(genderComboBox);

        JLabel locLbl = new JLabel("Location:");
        locLbl.setBounds(590, 30, 65, 25);
        controlPanel.add(locLbl);

        locationField = new JTextField();
        locationField.setBounds(660, 30, 130, 25);
        controlPanel.add(locationField);

        JButton filterButton = new JButton("Filter");
        filterButton.setBounds(810, 30, 95, 28);
        filterButton.setBackground(new Color(0, 123, 255));
        filterButton.setForeground(Color.WHITE);
        filterButton.setFocusPainted(false);
        controlPanel.add(filterButton);

        JButton resetButton = new JButton("Reset");
        resetButton.setBounds(810, 70, 95, 28);
        resetButton.setBackground(new Color(108, 117, 125));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        controlPanel.add(resetButton);

        // User Action Buttons
        JButton requestButton = new JButton("Adopt / Foster Request");
        requestButton.setBounds(20, 70, 175, 30);
        requestButton.setBackground(new Color(0, 123, 255));
        requestButton.setForeground(Color.WHITE);
        requestButton.setFocusPainted(false);
        controlPanel.add(requestButton);
        requestButton.addActionListener(e -> openRequestDialog());

        JButton trackButton = new JButton("My Requests");
        trackButton.setBounds(205, 70, 130, 30);
        trackButton.setBackground(new Color(23, 162, 184));
        trackButton.setForeground(Color.WHITE);
        trackButton.setFocusPainted(false);
        controlPanel.add(trackButton);
        trackButton.addActionListener(e -> openTrackRequestsDialog());

        filterButton.addActionListener(e -> filterPets());
        resetButton.addActionListener(e -> loadPets());

        // --- Pet List Heading ---
        JLabel petListLabel = new JLabel("Available Pets Catalog");
        petListLabel.setBounds(25, 225, 300, 25);
        petListLabel.setFont(new Font("Arial", Font.BOLD, 15));
        add(petListLabel);

        // --- JTable ---
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Type", "Breed", "Age", "Gender", "Location", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        petTable = new JTable(tableModel);
        petTable.setRowHeight(26);
        petTable.setFont(new Font("Arial", Font.PLAIN, 13));
        petTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(petTable);
        scrollPane.setBounds(20, 255, 925, 365);
        add(scrollPane);

        loadPets();
    }

    private void loadPets() {
        searchNameField.setText("");
        typeComboBox.setSelectedIndex(0);
        genderComboBox.setSelectedIndex(0);
        locationField.setText("");

        tableModel.setRowCount(0);
        String query = "SELECT id, name, pet_type, breed, age, gender, location, status FROM pets";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("name"), rs.getString("pet_type"),
                        rs.getString("breed"), rs.getInt("age"), rs.getString("gender"),
                        rs.getString("location"), rs.getString("status")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterPets() {
        String keyword = searchNameField.getText().trim();
        String type = (String) typeComboBox.getSelectedItem();
        String gender = (String) genderComboBox.getSelectedItem();
        String location = locationField.getText().trim();

        String sql = "SELECT id, name, pet_type, breed, age, gender, location, status FROM pets WHERE (name LIKE ? OR breed LIKE ?)";
        if (!"All".equals(type)) sql += " AND pet_type = ?";
        if (!"All".equals(gender)) sql += " AND gender = ?";
        if (!location.isEmpty()) sql += " AND location LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);

            int idx = 3;
            if (!"All".equals(type)) pstmt.setString(idx++, type);
            if (!"All".equals(gender)) pstmt.setString(idx++, gender);
            if (!location.isEmpty()) pstmt.setString(idx++, "%" + location + "%");

            ResultSet rs = pstmt.executeQuery();
            tableModel.setRowCount(0);

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("name"), rs.getString("pet_type"),
                        rs.getString("breed"), rs.getInt("age"), rs.getString("gender"),
                        rs.getString("location"), rs.getString("status")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openRequestDialog() {
        int selectedRow = petTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a pet first!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int petId = (int) tableModel.getValueAt(selectedRow, 0);
        String petName = (String) tableModel.getValueAt(selectedRow, 1);
        String status = (String) tableModel.getValueAt(selectedRow, 7);

        if (!"Available".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "This pet is currently " + status + "!", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog reqDialog = new JDialog(this, "Adopt / Foster Request", true);
        reqDialog.setSize(350, 240);
        reqDialog.setLocationRelativeTo(this);
        reqDialog.setLayout(null);

        JLabel infoLabel = new JLabel("Selected Pet: " + petName);
        infoLabel.setBounds(30, 25, 260, 25);
        reqDialog.add(infoLabel);

        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Adopt", "Foster"});
        typeComboBox.setBounds(140, 70, 150, 25);
        reqDialog.add(typeComboBox);

        JButton submitButton = new JButton("Submit Request");
        submitButton.setBounds(100, 130, 140, 35);
        submitButton.setBackground(new Color(40, 167, 69));
        submitButton.setForeground(Color.WHITE);
        reqDialog.add(submitButton);

        submitButton.addActionListener(e -> {
            String requestType = (String) typeComboBox.getSelectedItem();
            String query = "INSERT INTO requests (username, pet_id, pet_name, request_type, status) VALUES (?, ?, ?, ?, 'Pending')";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, loggedInUser);
                pstmt.setInt(2, petId);
                pstmt.setString(3, petName);
                pstmt.setString(4, requestType);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(reqDialog, "Request submitted successfully!");
                reqDialog.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        reqDialog.setVisible(true);
    }

    private void openTrackRequestsDialog() {
        JDialog trackDialog = new JDialog(this, "My Requests Status", true);
        trackDialog.setSize(550, 350);
        trackDialog.setLocationRelativeTo(this);
        trackDialog.setLayout(null);

        DefaultTableModel reqModel = new DefaultTableModel(new String[]{"ID", "Pet Name", "Type", "Status"}, 0);
        JTable reqTable = new JTable(reqModel);
        JScrollPane scroll = new JScrollPane(reqTable);
        scroll.setBounds(20, 20, 490, 270);
        trackDialog.add(scroll);

        String query = "SELECT id, pet_name, request_type, status FROM requests WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, loggedInUser);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                reqModel.addRow(new Object[]{rs.getInt("id"), rs.getString("pet_name"), rs.getString("request_type"), rs.getString("status")});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        trackDialog.setVisible(true);
    }
}