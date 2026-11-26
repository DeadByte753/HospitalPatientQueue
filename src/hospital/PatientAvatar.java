package hospital;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

/**
 * Small round avatar for a patient: their initials on a circle in their priority's color.
 * Drawn with Graphics2D, so no image files are needed.
 */
public class PatientAvatar implements Icon {

    private static final int SIZE = 28;

    private final String initials;
    private final Color color;

    public PatientAvatar(Patient patient) {
        this.initials = initialsOf(patient.getName());
        this.color = patient.getPriority().getColor();
    }

    /** First letter of the first and last words of the name, e.g. "Alice Mae Smith" gives "AS". */
    static String initialsOf(String name) {
        String[] words = name.trim().split("\\s+");
        if (words.length == 0 || words[0].isEmpty()) {
            return "?";
        }
        String first = words[0].substring(0, 1);
        String last = words.length > 1 ? words[words.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.setColor(color);
            g2.fill(new Ellipse2D.Double(x, y, SIZE, SIZE));

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int textX = x + (SIZE - fm.stringWidth(initials)) / 2;
            int textY = y + (SIZE - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(initials, textX, textY);
        } finally {
            g2.dispose();
        }
    }

    @Override
    public int getIconWidth() {
        return SIZE;
    }

    @Override
    public int getIconHeight() {
        return SIZE;
    }
}
