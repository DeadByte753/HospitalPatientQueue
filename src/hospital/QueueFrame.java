package hospital;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main dashboard: staff panel at the top, a button bar for every action, and the
 * waiting and served patient tables side by side in the center.
 */
public class QueueFrame extends JFrame {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int PRIORITY_COLUMN = 3;
    private static final int ROW_HEIGHT = 34;

    /** How long a served patient takes to move from the waiting panel to the served panel. */
    private static final double ANIMATION_SECONDS = 1.0;
    /** Timer tick in milliseconds (about 60 frames per second). */
    private static final int ANIMATION_TICK_MS = 16;

    private final PatientQueue queue;

    private final WaitingTableModel waitingModel = new WaitingTableModel();
    private final ServedTableModel servedModel = new ServedTableModel();
    private final JTable waitingTable = new JTable(waitingModel);
    private final JTable servedTable = new JTable(servedModel);

    private JSplitPane splitPane;
    private JButton serveButton;
    private final JLabel statusLabel = new JLabel();
    private final StatisticsPanel statisticsPanel = new StatisticsPanel();

    /** The running serve animation, or null when none is running. */
    private Timer serveAnimation;

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

            @Override
            public void windowOpened(WindowEvent e) {
                // A proportional divider position only works once the split pane has a real size.
                splitPane.setDividerLocation(0.58);
            }
        });

        setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new BorderLayout());
        top.add(new StaffPanel(StaffPanel.GROUP_MEMBERS), BorderLayout.NORTH);
        top.add(buildButtonBar(), BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        add(buildCenterPanel(), BorderLayout.CENTER);

        // Bottom: statistics tiles on the left, "Next / Just served" text beside them.
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
        bottom.add(statisticsPanel, BorderLayout.WEST);
        bottom.add(statusLabel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        refresh();

        setSize(1150, 660);
        setLocationRelativeTo(null);
    }

    // ---------------------------------------------------------------- layout

    private JPanel buildButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        bar.add(createButton("Register Patient", this::registerPatient));
        bar.add(createButton("Display Waiting Patients", this::displayWaitingPatients));
        serveButton = createButton("Serve Next Patient", this::serveNextPatient);
        bar.add(serveButton);
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

    private JComponent buildCenterPanel() {
        configureTable(waitingTable, WaitingTableModel.AVATAR_COLUMN);
        configureTable(servedTable, ServedTableModel.AVATAR_COLUMN);
        waitingTable.getColumnModel().getColumn(4).setPreferredWidth(80); // Status

        // Waiting panel: table plus a footer with the tie-breaking note and Remove Selected.
        JButton removeButton = new JButton("Remove Selected");
        removeButton.addActionListener(e -> removeSelected());

        JLabel tieBreakNote = new JLabel(
                "Note: equal priorities are served in arrival order (first come, first served).");
        tieBreakNote.setFont(tieBreakNote.getFont().deriveFont(Font.ITALIC));
        tieBreakNote.setForeground(Color.DARK_GRAY);

        JPanel waitingFooter = new JPanel(new BorderLayout(8, 0));
        waitingFooter.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        waitingFooter.add(tieBreakNote, BorderLayout.CENTER);
        waitingFooter.add(removeButton, BorderLayout.EAST);

        JPanel waitingPanel = new JPanel(new BorderLayout(4, 4));
        waitingPanel.setBorder(BorderFactory.createTitledBorder("Waiting Patients (in order of treatment)"));
        waitingPanel.add(new JScrollPane(waitingTable), BorderLayout.CENTER);
        waitingPanel.add(waitingFooter, BorderLayout.SOUTH);
        waitingPanel.setPreferredSize(new Dimension(680, 300));

        JPanel servedPanel = new JPanel(new BorderLayout());
        servedPanel.setBorder(BorderFactory.createTitledBorder("Served Patients (in order served)"));
        servedPanel.add(new JScrollPane(servedTable), BorderLayout.CENTER);
        servedPanel.setPreferredSize(new Dimension(450, 300));

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, waitingPanel, servedPanel);
        splitPane.setResizeWeight(0.6);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        return splitPane;
    }

    /** Shared table setup: single selection, colored priority, centered first column, avatar column. */
    private static void configureTable(JTable table, int avatarColumn) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(ROW_HEIGHT);

        TableColumnModel columns = table.getColumnModel();
        columns.getColumn(PRIORITY_COLUMN).setCellRenderer(new PriorityRenderer());

        DefaultTableCellRenderer centered = new DefaultTableCellRenderer();
        centered.setHorizontalAlignment(JLabel.CENTER);
        columns.getColumn(0).setCellRenderer(centered);
        columns.getColumn(0).setMaxWidth(50);

        columns.getColumn(avatarColumn).setMinWidth(60);
        columns.getColumn(avatarColumn).setMaxWidth(60);
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
            JOptionPane.showMessageDialog(this,
                    "Registered " + patient.getName() + " (" + patient.getId() + ")"
                            + "\nPriority: " + patient.getPriority()
                            + "\nPosition in queue: " + queue.positionOf(patient),
                    "Patient Registered", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    }

    /** Gives the waiting panel most of the width and scrolls it to Rank 1. */
    private void displayWaitingPatients() {
        splitPane.setDividerLocation(0.7);
        waitingTable.scrollRectToVisible(waitingTable.getCellRect(0, 0, true));
    }

    /**
     * Serves the next patient: PatientQueue.serveNext() calls poll(), marks the patient
     * Served and adds them to the served list. The patient that poll() returned is then
     * animated from its row in the waiting panel to its new row in the served panel.
     * If nobody is waiting, a message is shown and nothing changes.
     */
    private void serveNextPatient() {
        if (serveAnimation != null) {
            return; // the previous patient is still moving; the button is disabled meanwhile
        }

        Patient served = queue.serveNext();
        if (served == null) {
            JOptionPane.showMessageDialog(this, "No patients are waiting.",
                    "Queue empty", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Start point: the row where this patient is drawn right now (the table has not
        // been refreshed yet). This is a lookup by identity, not a re-sort.
        Point from = rowLocationInLayer(waitingTable, waitingModel.rowOf(served));

        // The patient leaves the waiting table as soon as they start moving, and the
        // statistics update straight away (the served table updates when the card lands).
        waitingModel.setPatients(queue.inOrder());
        updateStatistics();

        // End point: the row the patient will occupy at the bottom of the served table.
        Point to = rowLocationInLayer(servedTable, queue.getServed().size() - 1);

        animateToServedPanel(served, from, to);
    }

    /**
     * Moves a card showing {@code patient} from {@code from} to {@code to} over
     * ANIMATION_SECONDS using javax.swing.Timer. The patient is passed in; this
     * method never looks at the queue or its order.
     */
    private void animateToServedPanel(Patient patient, Point from, Point to) {
        JLayeredPane layer = getLayeredPane();
        MovingPatientCard card = new MovingPatientCard(patient);
        card.setSize(card.getPreferredSize());
        card.setLocation(from);
        layer.add(card, JLayeredPane.DRAG_LAYER);

        serveButton.setEnabled(false);
        long startNanos = System.nanoTime();

        serveAnimation = new Timer(ANIMATION_TICK_MS, null);
        serveAnimation.addActionListener(e -> {
            double elapsed = (System.nanoTime() - startNanos) / 1_000_000_000.0;
            double t = Math.min(1.0, elapsed / ANIMATION_SECONDS);
            double eased = t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2; // ease in-out

            int x = (int) Math.round(from.x + (to.x - from.x) * eased);
            int y = (int) Math.round(from.y + (to.y - from.y) * eased - Math.sin(Math.PI * t) * 40); // small arc
            card.setLocation(x, y);

            if (t >= 1.0) {
                serveAnimation.stop();
                serveAnimation = null;
                layer.remove(card);
                layer.repaint();
                finishServing(patient);
            }
        });
        serveAnimation.start();
    }

    /** Runs when the moving card lands: show the patient in the served table and re-enable the button. */
    private void finishServing(Patient patient) {
        refresh();
        int lastRow = servedModel.getRowCount() - 1;
        if (lastRow >= 0) {
            servedTable.setRowSelectionInterval(lastRow, lastRow);
            servedTable.scrollRectToVisible(servedTable.getCellRect(lastRow, 0, true));
        }
        serveButton.setEnabled(true);
        statusLabel.setText(statusLabel.getText() + "   |   Just served: " + patient.getName());
    }

    /**
     * Top-left corner of a table row, converted to layered-pane coordinates. Rows that are
     * scrolled out of view (or not added yet) are clamped to the visible part of the table.
     */
    private Point rowLocationInLayer(JTable table, int row) {
        Rectangle visible = table.getVisibleRect();
        int y = Math.max(row, 0) * table.getRowHeight();
        y = Math.max(visible.y, Math.min(y, visible.y + visible.height - table.getRowHeight()));
        return SwingUtilities.convertPoint(table, visible.x + 6, y, getLayeredPane());
    }

    /** Finds patients by ID or part of their name, in both the waiting and served lists. */
    private void searchPatient() {
        String text = JOptionPane.showInputDialog(this, "Enter a patient ID or name:",
                "Search Patient", JOptionPane.QUESTION_MESSAGE);
        if (text == null || text.isBlank()) {
            return;
        }

        // Highlight the first waiting match in the table.
        List<Patient> waitingMatches = queue.searchWaiting(text);
        if (!waitingMatches.isEmpty()) {
            int row = queue.positionOf(waitingMatches.get(0)) - 1;
            waitingTable.setRowSelectionInterval(row, row);
            waitingTable.scrollRectToVisible(waitingTable.getCellRect(row, 0, true));
        }

        JOptionPane.showMessageDialog(this, formatSearchResults(queue, text),
                "Search Results", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Builds the Search Patient result text. Matches an exact patient ID or part of a
     * name (both case-insensitive) across waiting and served patients. Each match shows
     * its rank (waiting patients only), priority and status; no match gives "Not found".
     */
    static String formatSearchResults(PatientQueue queue, String text) {
        List<Patient> waitingMatches = queue.searchWaiting(text);
        List<PatientQueue.ServedPatient> servedMatches = queue.searchServed(text);
        if (waitingMatches.isEmpty() && servedMatches.isEmpty()) {
            return "Not found";
        }

        StringBuilder message = new StringBuilder();
        for (Patient p : waitingMatches) {
            message.append(p.getId()).append("  ").append(p.getName()).append('\n')
                   .append("    Rank: ").append(queue.positionOf(p))
                   .append("    Priority: ").append(p.getPriority())
                   .append("    Status: ").append(p.getStatus()).append("\n\n");
        }
        for (PatientQueue.ServedPatient s : servedMatches) {
            Patient p = s.patient();
            message.append(p.getId()).append("  ").append(p.getName()).append('\n')
                   .append("    Rank: -")
                   .append("    Priority: ").append(p.getPriority())
                   .append("    Status: ").append(p.getStatus())
                   .append(" at ").append(TIME_FORMAT.format(s.servedAt())).append("\n\n");
        }
        return message.toString().trim();
    }

    /** Gives the served panel most of the width and scrolls to the most recently served patient. */
    private void displayServedPatients() {
        splitPane.setDividerLocation(0.3);
        int lastRow = servedModel.getRowCount() - 1;
        if (lastRow >= 0) {
            servedTable.scrollRectToVisible(servedTable.getCellRect(lastRow, 0, true));
        }
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

    /** Reloads both tables, the statistics and the status text from the queue. */
    private void refresh() {
        waitingModel.setPatients(queue.inOrder());
        servedModel.setServed(queue.getServed());
        updateStatistics();
    }

    /** Updates the statistics tiles and the "Next" text. Called after every action that changes the queue. */
    private void updateStatistics() {
        statisticsPanel.update(queue.size(), queue.getServed().size());
        Patient next = queue.peek();
        statusLabel.setText(next != null
                ? "Next: " + next.getName() + " (" + next.getPriority() + ")"
                : "No patients waiting");
    }

    // ------------------------------------------------------- moving patient

    /** The card that travels across the screen while a patient is being served. */
    private static class MovingPatientCard extends JComponent {

        private final Patient patient;
        private final Icon avatar;

        MovingPatientCard(Patient patient) {
            this.patient = patient;
            this.avatar = new PatientAvatar(patient);
            setPreferredSize(new Dimension(230, ROW_HEIGHT + 4));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                // Soft shadow, then the card itself with a border in the priority color.
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fill(new RoundRectangle2D.Double(3, 3, w - 4, h - 4, 14, 14));
                RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, w - 4, h - 4, 14, 14);
                g2.setColor(Color.WHITE);
                g2.fill(shape);
                g2.setColor(patient.getPriority().getColor());
                g2.setStroke(new BasicStroke(2f));
                g2.draw(shape);

                int iconY = (h - 4 - avatar.getIconHeight()) / 2;
                avatar.paintIcon(this, g2, 6, iconY);

                int textX = 6 + avatar.getIconWidth() + 8;
                g2.setColor(Color.BLACK);
                g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
                g2.drawString(patient.getName(), textX, 15);
                g2.setColor(Color.DARK_GRAY);
                g2.setFont(getFont().deriveFont(Font.PLAIN, 11f));
                g2.drawString(patient.getId() + "  -  " + patient.getPriority() + "  -  Serving...", textX, 29);
            } finally {
                g2.dispose();
            }
        }
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

    /**
     * Waiting patients, in the order they will be served.
     *
     * The list given to setPatients must already be sorted (see PatientQueue.inOrder(),
     * which copies the PriorityQueue into a list and sorts it with the queue's comparator).
     * Row 0 is therefore Rank 1, the next patient to be served.
     */
    private static class WaitingTableModel extends AbstractTableModel {

        static final int AVATAR_COLUMN = 5;
        private static final String[] COLUMNS = {"Rank", "Patient ID", "Name", "Priority", "Status", "Avatar"};
        private List<Patient> patients = List.of();
        private List<Icon> avatars = List.of();

        void setPatients(List<Patient> patients) {
            this.patients = patients;
            this.avatars = patients.stream().<Icon>map(PatientAvatar::new).toList();
            fireTableDataChanged();
        }

        Patient getPatientAt(int row) {
            return patients.get(row);
        }

        /** Row currently showing this exact patient object, or -1. Does not sort anything. */
        int rowOf(Patient patient) {
            for (int i = 0; i < patients.size(); i++) {
                if (patients.get(i) == patient) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            // Icon.class tells JTable to draw the Avatar column as an image, not as text.
            return column == AVATAR_COLUMN ? Icon.class : Object.class;
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
                case 0 -> row + 1;                          // Rank 1 = next to be served
                case 1 -> p.getId();
                case 2 -> p.getName();
                case 3 -> p.getPriority();
                case 4 -> row == 0 ? "Next" : p.getStatus().toString();
                case AVATAR_COLUMN -> avatars.get(row);
                default -> "";
            };
        }
    }

    /** Served patients, in the order they were served. */
    private static class ServedTableModel extends AbstractTableModel {

        static final int AVATAR_COLUMN = 6;
        private static final String[] COLUMNS = {"#", "ID", "Name", "Priority", "Status", "Served At", "Avatar"};
        private List<PatientQueue.ServedPatient> served = List.of();
        private List<Icon> avatars = List.of();

        void setServed(List<PatientQueue.ServedPatient> served) {
            this.served = served;
            this.avatars = served.stream().<Icon>map(s -> new PatientAvatar(s.patient())).toList();
            fireTableDataChanged();
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == AVATAR_COLUMN ? Icon.class : Object.class;
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
                case 4 -> s.patient().getStatus();
                case 5 -> TIME_FORMAT.format(s.servedAt());
                case AVATAR_COLUMN -> avatars.get(row);
                default -> "";
            };
        }
    }
}
