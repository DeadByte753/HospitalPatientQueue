package hospital;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Compact header for the dashboard: the app title on the left and a small badge for
 * each group member on the right (a drawn staff character, their full name and role).
 */
public class StaffPanel extends JPanel {

    /** Staff role shown for a group member; decides how the character is drawn. */
    public enum Role {
        DOCTOR("Doctor"),
        NURSE("Nurse"),
        RECEPTIONIST("Receptionist");

        private final String label;

        Role(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /** One group member: their full name and the staff role they appear as. */
    public record Member(String fullName, Role role) {
    }

    /**
     * EDIT THIS LIST: replace each placeholder with a group member's full name and
     * pick their role. Add or remove lines to match the size of your group.
     */
    public static final List<Member> GROUP_MEMBERS = List.of(
            new Member("[Member 1 Full Name]", Role.DOCTOR),
            new Member("[Member 2 Full Name]", Role.NURSE),
            new Member("[Member 3 Full Name]", Role.RECEPTIONIST),
            new Member("[Member 4 Full Name]", Role.DOCTOR)
    );

    private static final Color HEADER_DARK = new Color(0x0D, 0x47, 0xA1);
    private static final Color HEADER_LIGHT = new Color(0x19, 0x76, 0xD2);
    private static final Color SUBTLE_TEXT = new Color(0xBB, 0xDE, 0xFB);

    /** Size of each staff character in the header (the full drawing is 90 x 100). */
    private static final double FIGURE_SCALE = 0.42;

    public StaffPanel(List<Member> members) {
        setLayout(new BorderLayout(16, 0));
        setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        add(createTitle(), BorderLayout.WEST);

        // A single right-aligned row. Unlike FlowLayout it never wraps a badge onto a
        // hidden second line; if space runs out, long names are shortened with "...".
        JPanel badges = new JPanel();
        badges.setOpaque(false);
        badges.setLayout(new BoxLayout(badges, BoxLayout.X_AXIS));
        badges.add(javax.swing.Box.createHorizontalGlue());
        for (int i = 0; i < members.size(); i++) {
            if (i > 0) {
                badges.add(javax.swing.Box.createHorizontalStrut(8));
            }
            badges.add(createBadge(members.get(i)));
        }
        add(badges, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setPaint(new GradientPaint(0, 0, HEADER_DARK, getWidth(), 0, HEADER_LIGHT));
            g2.fillRect(0, 0, getWidth(), getHeight());
        } finally {
            g2.dispose();
        }
    }

    private static JPanel createTitle() {
        JLabel title = new JLabel("Hospital Patient Queue", new MedicalCrossIcon(), JLabel.LEFT);
        title.setIconTextGap(10);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JLabel subtitle = new JLabel("Priority triage dashboard");
        subtitle.setForeground(SUBTLE_TEXT);
        subtitle.setFont(subtitle.getFont().deriveFont(12f));
        subtitle.setBorder(BorderFactory.createEmptyBorder(0, 42, 0, 0)); // line up under the title text

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(title, BorderLayout.CENTER);
        panel.add(subtitle, BorderLayout.SOUTH);
        return panel;
    }

    /** A member badge: small character on the left, name and role on the right. */
    private static JPanel createBadge(Member member) {
        JPanel badge = new JPanel(new BorderLayout(6, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 34));
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
                } finally {
                    g2.dispose();
                }
            }
        };
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 8));

        JLabel name = new JLabel(member.fullName());
        name.setForeground(Color.WHITE);
        name.setFont(name.getFont().deriveFont(Font.BOLD, 11.5f));

        JLabel role = new JLabel(member.role().toString());
        role.setForeground(SUBTLE_TEXT);
        role.setFont(role.getFont().deriveFont(11f));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(javax.swing.Box.createVerticalGlue());
        text.add(name);
        text.add(role);
        text.add(javax.swing.Box.createVerticalGlue());

        badge.add(new StaffFigure(member.role(), FIGURE_SCALE), BorderLayout.WEST);
        badge.add(text, BorderLayout.CENTER);
        badge.setMaximumSize(badge.getPreferredSize()); // keep BoxLayout from stretching the badge
        return badge;
    }

    /** White circle with a red medical cross, shown before the title. */
    private static class MedicalCrossIcon implements Icon {
        private static final int SIZE = 32;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new Ellipse2D.Double(x, y, SIZE, SIZE));
                g2.setColor(new Color(0xD3, 0x2F, 0x2F));
                g2.fill(new Rectangle2D.Double(x + 13, y + 7, 6, 18));
                g2.fill(new Rectangle2D.Double(x + 7, y + 13, 18, 6));
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

    /**
     * A simple staff character drawn with Graphics2D shapes. The drawing is designed
     * on a 90 x 100 grid and scaled to the requested size.
     */
    private static class StaffFigure extends JComponent {

        private static final int BASE_WIDTH = 90;
        private static final int BASE_HEIGHT = 100;
        private static final Color SKIN = new Color(0xF1, 0xC2, 0x7D);
        private static final Color HAIR = new Color(0x4E, 0x34, 0x2E);
        private static final Color OUTLINE = new Color(0x60, 0x60, 0x60);

        private final Role role;
        private final double scale;

        StaffFigure(Role role, double scale) {
            this.role = role;
            this.scale = scale;
            Dimension size = new Dimension((int) Math.ceil(BASE_WIDTH * scale), (int) Math.ceil(BASE_HEIGHT * scale));
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.scale(scale, scale);
                double cx = BASE_WIDTH / 2.0;

                drawBody(g2, cx);
                drawHead(g2, cx);

                if (role == Role.NURSE) {
                    drawNurseCap(g2, cx);
                } else if (role == Role.RECEPTIONIST) {
                    drawHeadset(g2, cx);
                }
            } finally {
                g2.dispose();
            }
        }

        private void drawBody(Graphics2D g2, double cx) {
            RoundRectangle2D torso = new RoundRectangle2D.Double(cx - 32, 46, 64, 54, 28, 28);
            int x = (int) cx;

            switch (role) {
                case DOCTOR -> {
                    // White coat over a light blue shirt, with a stethoscope.
                    g2.setColor(Color.WHITE);
                    g2.fill(torso);
                    g2.setColor(OUTLINE);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(torso);
                    g2.setColor(new Color(0x90, 0xCA, 0xF9));
                    g2.fillPolygon(new Polygon(new int[]{x - 9, x + 9, x}, new int[]{46, 46, 64}, 3));
                    g2.setColor(OUTLINE);
                    g2.draw(new Line2D.Double(cx, 64, cx, 100));

                    g2.setColor(new Color(0x37, 0x47, 0x4F));
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.draw(new Arc2D.Double(cx - 13, 36, 26, 34, 180, 180, Arc2D.OPEN));
                    g2.draw(new Line2D.Double(cx + 13, 53, cx + 15, 66));
                    g2.fill(new Ellipse2D.Double(cx + 10, 66, 10, 10));
                }
                case NURSE -> {
                    // Teal scrubs with a V-neck.
                    g2.setColor(new Color(0x26, 0xA6, 0x9A));
                    g2.fill(torso);
                    g2.setColor(SKIN);
                    g2.fillPolygon(new Polygon(new int[]{x - 9, x + 9, x}, new int[]{46, 46, 60}, 3));
                    g2.setColor(new Color(0x00, 0x79, 0x6B));
                    g2.fill(new Rectangle2D.Double(cx + 10, 66, 12, 9)); // pocket
                }
                case RECEPTIONIST -> {
                    // Navy blazer, white shirt and red tie.
                    g2.setColor(new Color(0x28, 0x35, 0x93));
                    g2.fill(torso);
                    g2.setColor(Color.WHITE);
                    g2.fillPolygon(new Polygon(new int[]{x - 10, x + 10, x}, new int[]{46, 46, 68}, 3));
                    g2.setColor(new Color(0xC6, 0x28, 0x28));
                    g2.fillPolygon(new Polygon(
                            new int[]{x - 3, x + 3, x + 4, x, x - 4},
                            new int[]{48, 48, 62, 67, 62}, 5));
                }
            }
        }

        private void drawHead(Graphics2D g2, double cx) {
            g2.setColor(SKIN);
            g2.fill(new Ellipse2D.Double(cx - 15, 13, 30, 30));
            g2.setColor(HAIR);
            g2.fill(new Arc2D.Double(cx - 15, 13, 30, 22, 0, 180, Arc2D.CHORD));

            g2.setColor(Color.BLACK);
            g2.fill(new Ellipse2D.Double(cx - 7, 27, 3.5, 3.5));
            g2.fill(new Ellipse2D.Double(cx + 3.5, 27, 3.5, 3.5));
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new Arc2D.Double(cx - 5, 30, 10, 7, 200, 140, Arc2D.OPEN));
        }

        private void drawNurseCap(Graphics2D g2, double cx) {
            RoundRectangle2D cap = new RoundRectangle2D.Double(cx - 14, 6, 28, 12, 6, 6);
            g2.setColor(Color.WHITE);
            g2.fill(cap);
            g2.setColor(OUTLINE);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(cap);
            g2.setColor(new Color(0xD3, 0x2F, 0x2F));
            g2.fill(new Rectangle2D.Double(cx - 1.5, 8, 3, 8));
            g2.fill(new Rectangle2D.Double(cx - 4, 10.5, 8, 3));
        }

        private void drawHeadset(Graphics2D g2, double cx) {
            g2.setColor(new Color(0x21, 0x21, 0x21));
            g2.setStroke(new BasicStroke(3f));
            g2.draw(new Arc2D.Double(cx - 17, 10, 34, 34, 0, 180, Arc2D.OPEN));
            g2.fill(new RoundRectangle2D.Double(cx - 20, 24, 6, 10, 3, 3));
            g2.fill(new RoundRectangle2D.Double(cx + 14, 24, 6, 10, 3, 3));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new Line2D.Double(cx + 15, 33, cx + 6, 38));
            g2.fill(new Ellipse2D.Double(cx + 3, 36, 5, 5));
        }
    }
}
