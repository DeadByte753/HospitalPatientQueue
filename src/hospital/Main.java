package hospital;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point for the Hospital Patient Queue application.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to the default look and feel.
            }
            new QueueFrame(new PatientQueue()).setVisible(true);
        });
    }
}
