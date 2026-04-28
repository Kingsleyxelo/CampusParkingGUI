import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class GUI {

    static ArrayList<ParkingRecord> records = new ArrayList<>();
    static int nextId = 1;
    static final int TOTAL_SPACES = 5;

    static DefaultTableModel tableModel;
    static JTable table;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::buildGUI);
    }

    static void buildGUI() {
        JFrame frame = new JFrame("Parking System");
        frame.setSize(700, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // ==== TOP PANEL ====
        JPanel top = new JPanel();
        JTextField plateField = new JTextField(8);
        JTextField ownerField = new JTextField(8);

        String[] types = {"Car", "Motorcycle", "Van"};
        JComboBox<String> typeBox = new JComboBox<>(types);

        JButton addBtn = new JButton("Add");
        JButton removeBtn = new JButton("Remove");

        top.add(new JLabel("Plate:"));
        top.add(plateField);
        top.add(new JLabel("Owner:"));
        top.add(ownerField);
        top.add(typeBox);
        top.add(addBtn);
        top.add(removeBtn);

        frame.add(top, BorderLayout.NORTH);

        // ==== TABLE ====
        String[] cols = {"ID", "Plate", "Owner", "Type", "Status"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);

        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        // ==== BUTTON ACTIONS ====
        addBtn.addActionListener(e -> {
            String plate = plateField.getText().trim();
            String owner = ownerField.getText().trim();
            String type = (String) typeBox.getSelectedItem();

            if (plate.isEmpty() || owner.isEmpty()) return;

            long count = records.stream().filter(r -> r.status.equals("Parked")).count();
            if (count >= TOTAL_SPACES) return;

            ParkingRecord r = new ParkingRecord(nextId++, plate, owner, type);
            records.add(r);
            refreshTable();

            plateField.setText("");
            ownerField.setText("");
        });

        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;

            int id = (int) tableModel.getValueAt(row, 0);
            for (ParkingRecord r : records) {
                if (r.id == id) {
                    r.status = "Removed";
                }
            }
            refreshTable();
        });

        frame.setVisible(true);
    }

    static void refreshTable() {
        tableModel.setRowCount(0);
        for (ParkingRecord r : records) {
            tableModel.addRow(new Object[]{r.id, r.plate, r.owner, r.type, r.status});
        }
    }

    static class ParkingRecord {
        int id;
        String plate;
        String owner;
        String type;
        String status;

        ParkingRecord(int id, String plate, String owner, String type) {
            this.id = id;
            this.plate = plate;
            this.owner = owner;
            this.type = type;
            this.status = "Parked";
        }
    }
}
