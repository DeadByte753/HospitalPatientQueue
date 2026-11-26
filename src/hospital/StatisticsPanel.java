package hospital;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * Shows queue statistics as three tiles: Total Registered, Waiting and Served.
 * Total Registered is always Waiting + Served.
 */
public class StatisticsPanel extends JPanel {

    private final JLabel totalValue = new JLabel("0", SwingConstants.CENTER);
    private final JLabel waitingValue = new JLabel("0", SwingConstants.CENTER);
    private final JLabel servedValue = new JLabel("0", SwingConstants.CENTER);

    public StatisticsPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 2));
        add(createTile("Total Registered", totalValue, new Color(0x15, 0x65, 0xC0)));
        add(createTile("Waiting", waitingValue, new Color(0xF5, 0x7C, 0x00)));
        add(createTile("Served", servedValue, new Color(0x38, 0x8E, 0x3C)));
    }

    private static JPanel createTile(String title, JLabel value, Color color) {
        JPanel tile = new JPanel(new BorderLayout());
        tile.setBackground(Color.WHITE);
        tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, color),
                BorderFactory.createEmptyBorder(2, 10, 2, 10)));
        tile.setPreferredSize(new Dimension(130, 44));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(Color.DARK_GRAY);
        titleLabel.setFont(titleLabel.getFont().deriveFont(11f));

        value.setForeground(color);
        value.setFont(value.getFont().deriveFont(Font.BOLD, 18f));

        tile.add(titleLabel, BorderLayout.NORTH);
        tile.add(value, BorderLayout.CENTER);
        return tile;
    }

    /** Updates the three numbers. Total is computed here so it always equals waiting + served. */
    public void update(int waiting, int served) {
        totalValue.setText(String.valueOf(waiting + served));
        waitingValue.setText(String.valueOf(waiting));
        servedValue.setText(String.valueOf(served));
    }
}
