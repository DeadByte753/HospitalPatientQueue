package hospital;

import java.awt.Color;

/**
 * Triage priority of a patient.
 *
 * Each level has a rank (a lower rank is served first), a display label,
 * and a color used to highlight the patient in the UI.
 */
public enum Priority {
    HIGH(1, "High", new Color(0xD3, 0x2F, 0x2F)),    // red
    MEDIUM(2, "Medium", new Color(0xF5, 0x7C, 0x00)), // orange
    LOW(3, "Low", new Color(0x38, 0x8E, 0x3C));       // green

    private final int rank;
    private final String label;
    private final Color color;

    Priority(int rank, String label, Color color) {
        this.rank = rank;
        this.label = label;
        this.color = color;
    }

    /** Returns the rank; a lower rank is served first (HIGH = 1). */
    public int getRank() {
        return rank;
    }

    /** Returns the human-readable label shown in the UI. */
    public String getLabel() {
        return label;
    }

    /** Returns the color used to display this priority in the UI. */
    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return label;
    }
}
