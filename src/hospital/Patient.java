package hospital;

import java.util.Objects;

/**
 * Represents a patient in the hospital queue.
 *
 * Patients are identified by their ID and ordered by priority,
 * with tie-breaking by arrival order.
 */
public class Patient {

    private final String id;
    private final String name;
    private final Priority priority;

    /**
     * Arrival order: a monotonically increasing sequence number used for tie-breaking.
     * When two patients have the same priority level, the one with the lower arrivalOrder
     * (earlier arrival) is treated first.
     */
    private final long arrivalOrder;

    /**
     * Creates a new patient.
     *
     * @param id           unique identifier for this patient
     * @param name         patient's name
     * @param priority     triage priority level
     * @param arrivalOrder sequence number for tie-breaking (lower = arrived earlier)
     */
    public Patient(String id, String name, Priority priority, long arrivalOrder) {
        this.id = id;
        this.name = name;
        this.priority = priority;
        this.arrivalOrder = arrivalOrder;
    }

    /**
     * Returns the unique identifier for this patient.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the patient's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the triage priority level.
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * Returns the arrival order sequence number.
     * Used for tie-breaking when two patients have the same priority.
     */
    public long getArrivalOrder() {
        return arrivalOrder;
    }

    @Override
    public String toString() {
        return "#" + id + " " + name + " (" + priority + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return Objects.equals(id, patient.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
