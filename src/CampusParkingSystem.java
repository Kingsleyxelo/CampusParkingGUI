import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class CampusParkingSystem {

    static ArrayList<ParkingRecord> records = new ArrayList<>();

    static JTable      parkingTable;
    static DefaultTableModel tableModel;
    static JLabel      availableLabel;
    static final int TOTAL_SPACES = 1;


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> buildAndShowGUI());
    }

    static void buildAndShowGUI() {
        JFrame frame = new JFrame("Campus Parking Management System");
        frame.setSize(900, 640);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        Color darkBlue   = new Color(13, 27, 62);
        Color accentBlue = new Color(0, 123, 255);
        Color lightGray  = new Color(245, 247, 250);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(darkBlue);
        headerPanel.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel titleLabel = new JLabel("Campus Parking Management");
        titleLabel.setFont(new Font("Segue UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        availableLabel = new JLabel();
        availableLabel.setFont(new Font("Segue UI", Font.PLAIN, 13));
        availableLabel.setForeground(new Color(160, 210, 255));
        updateAvailableLabel();

        headerPanel.add(titleLabel,     BorderLayout.WEST);
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

        JButton clearBtn = outlineButton("Clear Fields");
        sidePanel.add(clearBtn);
        sidePanel.add(Box.createVerticalStrut(24));

        sidePanel.add(sectionLabel("REMOVE VEHICLE", darkBlue));
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
            @Override public boolean isCellEditable(int row, int col) {
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
            String type  = (String) typeCombo.getSelectedItem();
            String slot  = (String) SlotsCombo.getSelectedItem();

           if (plate.isEmpty() || owner.isEmpty()) {
                return;
           }

            for (ParkingRecord r : records) {
                if (r.plate.equals(plate) && r.status.equals("Parked")) {
                    return;
                }
            }

            int parked = 0;
            for (ParkingRecord r : records) {
                if (r.status.equals("Parked")) {
                    parked++;
                }
            }
            if (parked >= TOTAL_SPACES) {
                JOptionPane.showMessageDialog(frame, "Parking lot is full!");
                return;
            }


            ParkingRecord rec = new ParkingRecord( plate, owner, type, slot);
            records.add(rec);
            refreshTable(records);
            updateAvailableLabel();


            plateField.setText("");
            ownerField.setText("");
        });

        clearBtn.addActionListener(e -> {
            plateField.setText("");
            ownerField.setText("");
        });

        removeBtn.addActionListener(e -> {
                    int selectedRow = parkingTable.getSelectedRow();
                    if (selectedRow < 0)
                        return;

                String plate = tableModel.getValueAt(selectedRow, 0).toString();

                for (ParkingRecord r : records) {
                    if (r.plate.equals(plate)) {
                        r.status = "Removed";
                        break;
                    }
                }

                refreshTable(records);
                updateAvailableLabel();
            });

        frame.setVisible(true);
    }

    static void refreshTable(ArrayList<ParkingRecord> list) {
        tableModel.setRowCount(0);
        for (ParkingRecord r : list) {
            tableModel.addRow(new Object[]{
                    r.plate, r.owner, r.type, r.slot, r.status
            });
        }
    }

    static void updateAvailableLabel() {
        long parked = records.stream().filter(r -> r.status.equals("Parked")).count();
        int available = (int)(TOTAL_SPACES - parked);
        availableLabel.setText("Available: " + available + " / " + TOTAL_SPACES + "  ");
    }

    static JLabel sectionLabel(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segue UI", Font.BOLD, 11));
        lbl.setForeground(color);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    static JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segue UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(80, 80, 100));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    static JTextField styledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segue UI", Font.PLAIN, 13));
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
        return btn;
    }
    static JButton outlineButton(String label) {
        JButton btn = new JButton(label);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(new Color(60, 80, 140)));
        return btn;
    }
    static class ParkingRecord {
        String plate;
        String owner;
        String type;
        String slot;
        String status;

        ParkingRecord( String plate, String owner, String type, String slot) {
            this.plate  = plate;
            this.owner  = owner;
            this.type   = type;
            this.slot   = slot;
            this.status = "Parked";
        }
    }
}
