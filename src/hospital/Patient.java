package hospital;

import java.util.Objects;

/**
 * Represents a patient in the hospital queue.
 *
 * Patients are identified by their ID and ordered by priority,
 * with tie-breaking by arrival order.
 */
public class Patient {

    /** Where the patient is in the visit: waiting in the queue, or already served. */
    public enum Status {
        WAITING("Waiting"),
        SERVED("Served");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final String id;
    private final String name;
    private final Priority priority;

    /**
     * Arrival order: a monotonically increasing sequence number used for tie-breaking.
     * When two patients have the same priority level, the one with the lower arrivalOrder
     * (earlier arrival) is treated first.
     */
    private final long arrivalOrder;

    /** Every patient starts as WAITING; PatientQueue.serveNext() changes it to SERVED. */
    private Status status = Status.WAITING;

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

    /** Returns whether the patient is waiting or has been served. */
    public Status getStatus() {
        return status;
    }

    /**
     * Updates the patient's status. Status is not part of equals/hashCode
     * (those use the id only), so changing it is safe while in a collection.
     */
    public void setStatus(Status status) {
        this.status = status;
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
