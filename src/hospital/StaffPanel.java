package hospital;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
 * Top panel of the dashboard: one drawn hospital staff character per group member,
 * with the member's full name and role beneath it.
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

    public StaffPanel(List<Member> members) {
        setLayout(new FlowLayout(FlowLayout.CENTER, 28, 6));
        setBorder(BorderFactory.createTitledBorder("Hospital Staff"));
        setBackground(new Color(0xE3, 0xF2, 0xFD));
        for (Member member : members) {
            add(createCard(member));
        }
    }

    private static JPanel createCard(Member member) {
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        StaffFigure figure = new StaffFigure(member.role());
        figure.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel(member.fullName());
        name.setFont(name.getFont().deriveFont(Font.BOLD, 13f));
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel role = new JLabel(member.role().toString());
        role.setForeground(Color.DARK_GRAY);
        role.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(figure);
        card.add(name);
        card.add(role);
        return card;
    }

    /** A simple staff character drawn with Graphics2D shapes. */
    private static class StaffFigure extends JComponent {

        private static final Color SKIN = new Color(0xF1, 0xC2, 0x7D);
        private static final Color HAIR = new Color(0x4E, 0x34, 0x2E);
        private static final Color OUTLINE = new Color(0x60, 0x60, 0x60);

        private final Role role;

        StaffFigure(Role role) {
            this.role = role;
            Dimension size = new Dimension(90, 100);
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                double cx = getWidth() / 2.0;

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
