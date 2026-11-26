package hospital;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main dashboard: staff panel at the top, a button bar for every action,
 * and the patient table in the center.
 */
public class QueueFrame extends JFrame {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int PRIORITY_COLUMN = 3;
    private static final String WAITING_CARD = "waiting";
    private static final String SERVED_CARD = "served";

    private final PatientQueue queue;

    private final WaitingTableModel waitingModel = new WaitingTableModel();
    private final ServedTableModel servedModel = new ServedTableModel();
    private final JTable waitingTable = new JTable(waitingModel);
    private final JTable servedTable = new JTable(servedModel);

    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);
    private final TitledBorder centerBorder = BorderFactory.createTitledBorder("");
    private final JPanel centerPanel = new JPanel(new BorderLayout());
    private final JLabel statusLabel = new JLabel();

    public QueueFrame(PatientQueue queue) {
        super("Hospital Patient Queue");
        this.queue = queue;

        // Closing the window goes through the same confirmation as the Exit button.
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });

        setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new BorderLayout());
        top.add(new StaffPanel(StaffPanel.GROUP_MEMBERS), BorderLayout.NORTH);
        top.add(buildButtonBar(), BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        add(buildCenterPanel(), BorderLayout.CENTER);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(statusLabel, BorderLayout.SOUTH);

        refresh();
        displayWaitingPatients();

        setSize(1000, 640);
        setLocationRelativeTo(null);
    }

    // ---------------------------------------------------------------- layout

    private JPanel buildButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        bar.add(createButton("Register Patient", this::registerPatient));
        bar.add(createButton("Display Waiting Patients", this::displayWaitingPatients));
        bar.add(createButton("Serve Next Patient", this::serveNextPatient));
        bar.add(createButton("Search Patient", this::searchPatient));
        bar.add(createButton("Display Served Patients", this::displayServedPatients));

        JButton exitButton = createButton("Exit", this::exitApplication);
        exitButton.setForeground(new Color(0xC6, 0x28, 0x28));
        bar.add(exitButton);
        return bar;
    }

    private static JButton createButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.addActionListener(e -> action.run());
        return button;
    }

    private JPanel buildCenterPanel() {
        configureTable(waitingTable);
        configureTable(servedTable);

        JButton removeButton = new JButton("Remove Selected");
        removeButton.addActionListener(e -> removeSelected());
        JPanel waitingButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        waitingButtons.add(removeButton);

        JPanel waitingCard = new JPanel(new BorderLayout(4, 4));
        waitingCard.add(new JScrollPane(waitingTable), BorderLayout.CENTER);
        waitingCard.add(waitingButtons, BorderLayout.SOUTH);

        JPanel servedCard = new JPanel(new BorderLayout());
        servedCard.add(new JScrollPane(servedTable), BorderLayout.CENTER);

        cardPanel.add(waitingCard, WAITING_CARD);
        cardPanel.add(servedCard, SERVED_CARD);

        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 8, 0, 8), centerBorder));
        centerPanel.add(cardPanel, BorderLayout.CENTER);
        return centerPanel;
    }

    private static void configureTable(JTable table) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(PRIORITY_COLUMN).setCellRenderer(new PriorityRenderer());
    }

    // --------------------------------------------------------------- actions

    /**
     * Asks for the patient's ID, name and priority, validates them, then adds the
     * patient to the waiting queue. On invalid input the errors are listed and the
     * dialog reopens with what was typed, so nothing has to be re-entered.
     */
    private void registerPatient() {
        JTextField idField = new JTextField(18);
        JTextField nameField = new JTextField(18);
        JComboBox<Priority> priorityBox = new JComboBox<>(Priority.values());
        priorityBox.setSelectedIndex(-1); // no default: the user must choose a priority

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Patient ID:"));
        form.add(idField);
        form.add(new JLabel("Full name:"));
        form.add(nameField);
        form.add(new JLabel("Priority:"));
        form.add(priorityBox);

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, form, "Register Patient",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            Priority priority = (Priority) priorityBox.getSelectedItem();

            List<String> errors = queue.validateRegistration(id, name, priority);
            if (!errors.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please fix the following:\n\n- " + String.join("\n- ", errors),
                        "Cannot register patient", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            Patient patient = queue.admit(id, name, priority);
            refresh();
            displayWaitingPatients();
            JOptionPane.showMessageDialog(this,
                    "Registered " + patient.getName() + " (" + patient.getId() + ")"
                            + "\nPriority: " + patient.getPriority()
                            + "\nPosition in queue: " + queue.positionOf(patient),
                    "Patient Registered", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    }

    /** Shows the waiting patients, in the order they will be served. */
    private void displayWaitingPatients() {
        centerBorder.setTitle("Waiting Patients (in order of treatment)");
        cards.show(cardPanel, WAITING_CARD);
        centerPanel.repaint();
    }

    /** Serves the highest-priority patient who arrived first and moves them to the served list. */
    private void serveNextPatient() {
        Patient next = queue.serveNext();
        if (next == null) {
            JOptionPane.showMessageDialog(this, "No patients are waiting.",
                    "Queue empty", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        refresh();
        JOptionPane.showMessageDialog(this,
                "Now serving: " + next.getName() + " (" + next.getId() + ")"
                        + "\nPriority: " + next.getPriority(),
                "Serve Next Patient", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Finds patients by ID or part of their name, in both the waiting and served lists. */
    private void searchPatient() {
        String text = JOptionPane.showInputDialog(this, "Enter a patient ID (e.g. P001) or name:",
                "Search Patient", JOptionPane.QUESTION_MESSAGE);
        if (text == null || text.isBlank()) {
            return;
        }

        List<Patient> waitingMatches = queue.searchWaiting(text);
        List<PatientQueue.ServedPatient> servedMatches = queue.searchServed(text);

        if (waitingMatches.isEmpty() && servedMatches.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No patient matches \"" + text.trim() + "\".",
                    "Search Patient", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder message = new StringBuilder();
        for (Patient p : waitingMatches) {
            message.append(p.getId()).append("  ").append(p.getName())
                   .append(" (").append(p.getPriority()).append(")")
                   .append(" - waiting, position ").append(queue.positionOf(p)).append('\n');
        }
        for (PatientQueue.ServedPatient s : servedMatches) {
            Patient p = s.patient();
            message.append(p.getId()).append("  ").append(p.getName())
                   .append(" (").append(p.getPriority()).append(")")
                   .append(" - served at ").append(TIME_FORMAT.format(s.servedAt())).append('\n');
        }

        // Highlight the first waiting match in the table.
        if (!waitingMatches.isEmpty()) {
            displayWaitingPatients();
            int row = queue.positionOf(waitingMatches.get(0)) - 1;
            waitingTable.setRowSelectionInterval(row, row);
            waitingTable.scrollRectToVisible(waitingTable.getCellRect(row, 0, true));
        }

        JOptionPane.showMessageDialog(this, message.toString().trim(),
                "Search Results", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Shows every patient who has been served, in the order they were served. */
    private void displayServedPatients() {
        centerBorder.setTitle("Served Patients (in order served)");
        cards.show(cardPanel, SERVED_CARD);
        centerPanel.repaint();
    }

    /** Asks for confirmation, then closes the application. */
    private void exitApplication() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit?", "Exit",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    /** Removes the selected waiting patient without serving them. */
    private void removeSelected() {
        int row = waitingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a patient in the queue first.",
                    "Nothing selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Patient patient = waitingModel.getPatientAt(waitingTable.convertRowIndexToModel(row));
        int choice = JOptionPane.showConfirmDialog(this,
                "Remove " + patient.getName() + " from the queue?",
                "Confirm removal", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            queue.remove(patient);
            refresh();
        }
    }

    /** Reloads both tables and the status bar from the queue. */
    private void refresh() {
        waitingModel.setPatients(queue.inOrder());
        servedModel.setServed(queue.getServed());
        Patient next = queue.peek();
        statusLabel.setText("Waiting: " + queue.size()
                + "   |   Served: " + queue.getServed().size()
                + (next != null ? "   |   Next: " + next.getName() + " (" + next.getPriority() + ")" : ""));
    }

    // ---------------------------------------------------------- table models

    /** Shows the priority label in bold, in that priority's color. */
    private static class PriorityRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            cell.setFont(cell.getFont().deriveFont(Font.BOLD));
            if (!isSelected) {
                cell.setForeground(value instanceof Priority priority ? priority.getColor() : Color.BLACK);
            }
            return cell;
        }
    }

    /** Waiting patients, in the order they will be served. */
    private static class WaitingTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {"Pos", "ID", "Name", "Priority"};
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
                case 3 -> p.getPriority();
                default -> "";
            };
        }
    }

    /** Served patients, in the order they were served. */
    private static class ServedTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {"#", "ID", "Name", "Priority", "Served At"};
        private List<PatientQueue.ServedPatient> served = List.of();

        void setServed(List<PatientQueue.ServedPatient> served) {
            this.served = served;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return served.size();
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
            PatientQueue.ServedPatient s = served.get(row);
            return switch (column) {
                case 0 -> row + 1;
                case 1 -> s.patient().getId();
                case 2 -> s.patient().getName();
                case 3 -> s.patient().getPriority();
                case 4 -> TIME_FORMAT.format(s.servedAt());
                default -> "";
            };
        }
    }
}
