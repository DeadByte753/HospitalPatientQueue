package hospital;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main window: admit patients, view the waiting queue, and call the next patient.
 */
public class QueueFrame extends JFrame {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final PatientQueue queue;
    private final QueueTableModel tableModel = new QueueTableModel();
    private final DefaultListModel<String> servedModel = new DefaultListModel<>();
    private final JTable table = new JTable(tableModel);

    private final JTextField nameField = new JTextField(16);
    private final JSpinner ageSpinner = new JSpinner(new SpinnerNumberModel(30, 0, 130, 1));
    private final JTextField conditionField = new JTextField(16);
    private final JComboBox<Priority> priorityBox = new JComboBox<>(Priority.values());
    private final JLabel statusLabel = new JLabel();

    public QueueFrame(PatientQueue queue) {
        super("Hospital Patient Queue");
        this.queue = queue;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        add(buildFormPanel(), BorderLayout.WEST);
        add(buildQueuePanel(), BorderLayout.CENTER);
        add(buildServedPanel(), BorderLayout.EAST);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(statusLabel, BorderLayout.SOUTH);

        priorityBox.setSelectedItem(Priority.STANDARD);
        refresh();

        setSize(980, 480);
        setLocationRelativeTo(null);
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Admit Patient"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        addRow(panel, c, 0, "Name:", nameField);
        addRow(panel, c, 1, "Age:", ageSpinner);
        addRow(panel, c, 2, "Condition:", conditionField);
        addRow(panel, c, 3, "Priority:", priorityBox);

        JButton admitButton = new JButton("Admit");
        admitButton.addActionListener(e -> admitPatient());
        getRootPane().setDefaultButton(admitButton);

        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        panel.add(admitButton, c);

        // Push content to the top.
        c.gridy = 5;
        c.weighty = 1;
        panel.add(new JPanel(), c);

        return panel;
    }

    private static void addRow(JPanel panel, GridBagConstraints c, int row, String label, java.awt.Component field) {
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 1;
        c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(field, c);
    }

    private JPanel buildQueuePanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Waiting Queue (in order of treatment)"));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton callNextButton = new JButton("Call Next Patient");
        callNextButton.setFont(callNextButton.getFont().deriveFont(Font.BOLD));
        callNextButton.addActionListener(e -> callNext());

        JButton removeButton = new JButton("Remove Selected");
        removeButton.addActionListener(e -> removeSelected());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(removeButton);
        buttons.add(callNextButton);
        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildServedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Called"));
        JScrollPane scroll = new JScrollPane(new javax.swing.JList<>(servedModel));
        scroll.setPreferredSize(new java.awt.Dimension(220, 0));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void admitPatient() {
        String name = nameField.getText().trim();
        String condition = conditionField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the patient's name.",
                    "Missing information", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocusInWindow();
            return;
        }
        int age = (Integer) ageSpinner.getValue();
        Priority priority = (Priority) priorityBox.getSelectedItem();

        queue.admit(name, age, condition.isEmpty() ? "-" : condition, priority);

        nameField.setText("");
        conditionField.setText("");
        priorityBox.setSelectedItem(Priority.STANDARD);
        nameField.requestFocusInWindow();
        refresh();
    }

    private void callNext() {
        Patient next = queue.callNext();
        if (next == null) {
            JOptionPane.showMessageDialog(this, "No patients are waiting.",
                    "Queue empty", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        servedModel.add(0, TIME_FORMAT.format(java.time.LocalTime.now()) + "  " + next);
        JOptionPane.showMessageDialog(this,
                "Now calling: " + next.getName() + "\nPriority: " + next.getPriority()
                        + "\nCondition: " + next.getCondition(),
                "Next Patient", JOptionPane.INFORMATION_MESSAGE);
        refresh();
    }

    private void removeSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a patient in the queue first.",
                    "Nothing selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Patient patient = tableModel.getPatientAt(table.convertRowIndexToModel(row));
        int choice = JOptionPane.showConfirmDialog(this,
                "Remove " + patient.getName() + " from the queue?",
                "Confirm removal", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            queue.remove(patient);
            refresh();
        }
    }

    private void refresh() {
        tableModel.setPatients(queue.inOrder());
        Patient next = queue.peek();
        statusLabel.setText("Patients waiting: " + queue.size()
                + (next != null ? "   |   Next: " + next.getName() + " (" + next.getPriority() + ")" : ""));
    }

    /** Table model backed by the ordered list of waiting patients. */
    private static class QueueTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {"Pos", "ID", "Name", "Age", "Condition", "Priority", "Arrived"};
        private List<Patient> patients = List.of();

        void setPatients(List<Patient> patients) {
            this.patients = patients;
            fireTableDataChanged();
        }

        Patient getPatientAt(int row) {
            return patients.get(row);
        }

        @Override
        public int getRowCount() {
            return patients.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            Patient p = patients.get(row);
            return switch (column) {
                case 0 -> row + 1;
                case 1 -> p.getId();
                case 2 -> p.getName();
                case 3 -> p.getAge();
                case 4 -> p.getCondition();
                case 5 -> p.getPriority();
                case 6 -> TIME_FORMAT.format(p.getArrivalTime());
                default -> "";
            };
        }
    }
}
