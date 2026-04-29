import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

static JTable parkingTable;
static DefaultTableModel tableModel;
static JLabel availableLabel;
static final int TOTAL_SPACES = 10;


void main() {

    SwingUtilities.invokeLater(() -> buildAndShowGUI());
}

static void buildAndShowGUI() {
    JFrame frame = new JFrame("Campus Parking Management System");
    frame.setSize(900, 640);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new BorderLayout());

    Color darkBlue = new Color(13, 27, 62);
    Color accentBlue = new Color(0, 123, 255);
    Color lightGray = new Color(245, 247, 250);

    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setBackground(darkBlue);
    headerPanel.setBorder(new EmptyBorder(14, 20, 14, 20));

    JLabel titleLabel = new JLabel("Campus Parking Management");
    titleLabel.setFont(new Font("Segue UI", Font.BOLD, 20));
    titleLabel.setForeground(Color.WHITE);

    availableLabel = new JLabel();
    availableLabel.setFont(new Font("Segue UI", Font.PLAIN, 13));
    availableLabel.setForeground(new Color(160, 210, 255));

    headerPanel.add(titleLabel, BorderLayout.WEST);
    headerPanel.add(availableLabel, BorderLayout.EAST);

    frame.add(headerPanel, BorderLayout.NORTH);

    JPanel sidePanel = new JPanel();
    sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
    sidePanel.setBackground(Color.WHITE);
    sidePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)),
            new EmptyBorder(20, 18, 20, 18)
    ));
    sidePanel.setPreferredSize(new Dimension(240, 0));

    sidePanel.add(fieldLabel("Plate Number"));
    JTextField plateField = styledTextField();
    sidePanel.add(plateField);

    sidePanel.add(fieldLabel("Owner Name"));
    JTextField ownerField = styledTextField();
    sidePanel.add(ownerField);

    sidePanel.add(fieldLabel("Vehicle Type"));
    String[] types = {"Car", "Motorcycle", "Van", "Truck", "Bus"};
    JComboBox<String> typeCombo = new JComboBox<>(types);
    typeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
    typeCombo.setFont(new Font("Segue UI", Font.PLAIN, 13));
    sidePanel.add(typeCombo);
    sidePanel.add(Box.createVerticalStrut(10));

    sidePanel.add(fieldLabel("Slots"));
    String[] Slots = {"Staff", "Students", "Visitors", "Disabled"};
    JComboBox<String> SlotsCombo = new JComboBox<>(Slots);
    SlotsCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
    SlotsCombo.setFont(new Font("Segue UI", Font.PLAIN, 13));
    sidePanel.add(SlotsCombo);
    sidePanel.add(Box.createVerticalStrut(18));

    JButton addBtn = primaryButton("Add Vehicle", accentBlue);
    sidePanel.add(addBtn);
    sidePanel.add(Box.createVerticalStrut(8));

    JButton clearBtn = outlineButton();
    sidePanel.add(clearBtn);
    sidePanel.add(Box.createVerticalStrut(24));

    sidePanel.add(sectionLabel(darkBlue));
    sidePanel.add(Box.createVerticalStrut(10));
    sidePanel.add(fieldLabel("Select row in table, then:"));
    JButton removeBtn = primaryButton("Remove Selected", new Color(220, 53, 69));
    sidePanel.add(removeBtn);
    sidePanel.add(Box.createVerticalStrut(24));

    sidePanel.add(Box.createVerticalGlue());

    frame.add(sidePanel, BorderLayout.WEST);

    String[] columns = {"Plate No.", "Owner", "Type", "Slots", "Status"};
    tableModel = new DefaultTableModel(columns, 0) {
        // Make all cells non-editable directly in the table
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    parkingTable = new JTable(tableModel);

    parkingTable.setRowHeight(30);
    parkingTable.setFont(new Font("Segue UI", Font.PLAIN, 13));
    parkingTable.getTableHeader().setFont(new Font("Segue UI", Font.BOLD, 13));
    parkingTable.getTableHeader().setBackground(darkBlue);
    parkingTable.getTableHeader().setForeground(Color.WHITE);

    JScrollPane scrollPane = new JScrollPane(parkingTable);

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.setBackground(lightGray);
    centerPanel.setBorder(new EmptyBorder(16, 16, 0, 16));

    JLabel tableTitle = new JLabel("Parked Vehicles");
    tableTitle.setFont(new Font("Segue UI", Font.BOLD, 15));
    tableTitle.setForeground(darkBlue);
    tableTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

    centerPanel.add(tableTitle, BorderLayout.NORTH);
    centerPanel.add(scrollPane, BorderLayout.CENTER);

    frame.add(centerPanel, BorderLayout.CENTER);

    addBtn.addActionListener(e -> {
        String plate = plateField.getText().trim().toUpperCase();
        String owner = ownerField.getText().trim();
        String type = (String) typeCombo.getSelectedItem();
        String slot = (String) SlotsCombo.getSelectedItem();

        if (plate.isEmpty() || owner.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Plate and Owner are required.");
            return;
        }

        if (isAlreadyParked(plate)) {
            JOptionPane.showMessageDialog(frame, "Vehicle already parked!");
            return;
        }

        // Check available spaces
        int parkedCount = countParked();
        if (parkedCount >= TOTAL_SPACES) {
            JOptionPane.showMessageDialog(frame, "Parking lot is full!");
            return;
        }
        String sql = "INSERT INTO parking_records (plate, owner, type, slot, status) VALUES (?, ?, ?, ?, 'Parked')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plate);
            ps.setString(2, owner);
            ps.setString(3, type);
            ps.setString(4, slot);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "DB error: " + ex.getMessage());
            return;
        }
        refreshTable();
        plateField.setText("");
        ownerField.setText("");
    });

    clearBtn.addActionListener(e -> {
        plateField.setText("");
        ownerField.setText("");
    });

    removeBtn.addActionListener(e -> {
        int selectedRow = parkingTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(frame, "Select a row first.");
            return;
        }
        String plate = tableModel.getValueAt(selectedRow, 0).toString();
        String updateSql = "UPDATE parking_records SET status = 'Removed' WHERE plate = ? AND status = 'Parked'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, plate);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "DB error: " + ex.getMessage());
        }
        refreshTable();
    });

    refreshTable();
    frame.setVisible(true);
}

static void refreshTable() {
    tableModel.setRowCount(0);
    String sql = "SELECT plate, owner, type, slot, status FROM parking_records ORDER BY id DESC";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            tableModel.addRow(new Object[]{
                    rs.getString("plate"),
                    rs.getString("owner"),
                    rs.getString("type"),
                    rs.getString("slot"),
                    rs.getString("status")
            });
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}

static boolean isAlreadyParked(String plate) {
    String sql = "SELECT id FROM parking_records WHERE plate = ? AND status = 'Parked'";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, plate);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    } catch (SQLException ex) {
        ex.printStackTrace();
        return false;
    }
}

static int countParked() {
    String sql = "SELECT COUNT(*) FROM parking_records WHERE status = 'Parked'";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getInt(1);
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
    return 0;
}

static JLabel sectionLabel(Color color) {
    JLabel lbl = new JLabel("REMOVE VEHICLE");
    lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
    lbl.setForeground(color);
    lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
    return lbl;
}

static JLabel fieldLabel(String text) {
    JLabel lbl = new JLabel(text);
    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    lbl.setForeground(new Color(80, 80, 100));
    lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
    return lbl;
}

static JTextField styledTextField() {
    JTextField tf = new JTextField();
    tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
    tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 215)),
            new EmptyBorder(4, 8, 4, 8)
    ));
    return tf;
}

static JButton primaryButton(String label, Color bg) {
    JButton btn = new JButton(label);
    btn.setBackground(bg);
    btn.setForeground(Color.WHITE);
    btn.setFocusPainted(false);
    return btn;
}

static JButton outlineButton() {
    JButton btn = new JButton("Clear Fields");
    btn.setBackground(Color.WHITE);
    btn.setBorder(BorderFactory.createLineBorder(new Color(60, 80, 140)));
    btn.setFocusPainted(false);
    return btn;
}

static void styleCombo(JComboBox<String> combo) {
    combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
    combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
}
