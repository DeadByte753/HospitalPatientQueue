package hospital;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * Opening screen: a hospital-themed background with a title and a Start button.
 * Clicking Start closes this window and opens the patient queue dashboard.
 */
public class WelcomeFrame extends JFrame {

    public WelcomeFrame() {
        super("Hospital Patient Queue");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(new BackgroundPanel());
        setSize(640, 420);
        setLocationRelativeTo(null);
    }

    private void start() {
        dispose();
        new QueueFrame(new PatientQueue()).setVisible(true);
    }

    /** Panel that paints a light blue gradient with a white medical cross, then the title and button. */
    private class BackgroundPanel extends JPanel {

        BackgroundPanel() {
            setLayout(new BorderLayout());
            setOpaque(true);

            JLabel title = new JLabel("Hospital Patient Queue", SwingConstants.CENTER);
            title.setFont(new Font("SansSerif", Font.BOLD, 30));
            title.setForeground(new Color(0x0D, 0x47, 0xA1));
            title.setBorder(BorderFactory.createEmptyBorder(40, 10, 0, 10));

            JButton startButton = new JButton("Start");
            startButton.setFont(new Font("SansSerif", Font.BOLD, 18));
            startButton.setFocusPainted(false);
            startButton.setBackground(Color.WHITE);
            startButton.addActionListener(e -> start());

            JPanel bottom = new JPanel();
            bottom.setOpaque(false);
            bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 60, 0));
            bottom.add(startButton);

            add(title, BorderLayout.NORTH);
            add(bottom, BorderLayout.SOUTH);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                // Light blue gradient background.
                g2.setPaint(new GradientPaint(0, 0, new Color(0xE3, 0xF2, 0xFD), 0, h, new Color(0x90, 0xCA, 0xF9)));
                g2.fill(new Rectangle2D.Double(0, 0, w, h));

                // White medical cross, centred in the lower-middle area behind the button.
                double size = Math.min(w, h) * 0.30;
                double cx = w / 2.0;
                double cy = h * 0.55;
                double arm = size / 3.0;
                g2.setColor(new Color(255, 255, 255, 200));
                g2.fill(new Rectangle2D.Double(cx - arm / 2, cy - size / 2, arm, size));
                g2.fill(new Rectangle2D.Double(cx - size / 2, cy - arm / 2, size, arm));

                // Soft ring around the cross.
                g2.setColor(new Color(255, 255, 255, 110));
                g2.setStroke(new BasicStroke(6f));
                g2.draw(new Ellipse2D.Double(cx - size * 0.8, cy - size * 0.8, size * 1.6, size * 1.6));
            } finally {
                g2.dispose();
            }
        }
    }
}
