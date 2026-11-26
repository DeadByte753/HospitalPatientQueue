package hospital;

/**
 * Triage priority levels. A lower level is treated sooner.
 */
public enum Priority {
    CRITICAL(1, "Critical"),
    URGENT(2, "Urgent"),
    STANDARD(3, "Standard"),
    NON_URGENT(4, "Non-urgent");

    private final int level;
    private final String label;

    Priority(int level, String label) {
        this.level = level;
        this.label = label;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return label;
    }
}
