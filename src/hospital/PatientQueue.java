package hospital;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;

/**
 * Priority queue of patients: lowest priority rank first (HIGH before LOW),
 * then earliest arrival within the same priority. Also keeps a history of
 * patients who have been served.
 */
public class PatientQueue {

    /** A patient who has been served, and when. */
    public record ServedPatient(Patient patient, LocalTime servedAt) {
    }

    private static final Comparator<Patient> ORDER =
            Comparator.comparingInt((Patient p) -> p.getPriority().getRank())
                      .thenComparingLong(Patient::getArrivalOrder);

    private final PriorityQueue<Patient> waiting = new PriorityQueue<>(ORDER);
    private final List<ServedPatient> served = new ArrayList<>();

    /** Incremented for every registration; gives each patient the next arrival order. */
    private long arrivalCounter = 0;

    /**
     * Checks registration input and returns one error message per problem.
     * An empty list means the input is valid. Values are trimmed before checking.
     */
    public List<String> validateRegistration(String id, String name, Priority priority) {
        List<String> errors = new ArrayList<>();
        String trimmedId = id == null ? "" : id.trim();
        String trimmedName = name == null ? "" : name.trim();

        if (trimmedId.isEmpty()) {
            errors.add("Patient ID is required.");
        } else if (isIdTaken(trimmedId)) {
            errors.add("Patient ID \"" + trimmedId + "\" is already used by another patient.");
        }
        if (trimmedName.isEmpty()) {
            errors.add("Patient name is required.");
        }
        if (priority == null) {
            errors.add("Priority is required. Choose High, Medium or Low.");
        }
        return errors;
    }

    /** True if any waiting or served patient already has this ID (ignoring case and spaces). */
    public boolean isIdTaken(String id) {
        String wanted = id.trim();
        return waiting.stream().anyMatch(p -> p.getId().equalsIgnoreCase(wanted))
                || served.stream().anyMatch(s -> s.patient().getId().equalsIgnoreCase(wanted));
    }

    /**
     * Registers a new patient with the next arrival order and adds them to the waiting queue.
     *
     * @throws IllegalArgumentException if the input fails {@link #validateRegistration}
     */
    public Patient admit(String id, String name, Priority priority) {
        List<String> errors = validateRegistration(id, name, priority);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }
        Patient patient = new Patient(id.trim(), name.trim(), priority, ++arrivalCounter);
        waiting.add(patient);
        return patient;
    }

    /**
     * Removes the next patient from the waiting queue, records them as served,
     * and returns them. Returns null if nobody is waiting.
     */
    public Patient serveNext() {
        Patient next = waiting.poll();
        if (next != null) {
            served.add(new ServedPatient(next, LocalTime.now()));
        }
        return next;
    }

    public Patient peek() {
        return waiting.peek();
    }

    /** Removes a patient from the waiting queue without serving them. */
    public boolean remove(Patient patient) {
        return waiting.remove(patient);
    }

    public int size() {
        return waiting.size();
    }

    public boolean isEmpty() {
        return waiting.isEmpty();
    }

    /** Returns the waiting patients in the order they will be served. */
    public List<Patient> inOrder() {
        List<Patient> list = new ArrayList<>(waiting);
        list.sort(ORDER);
        return list;
    }

    /** Returns the 1-based position of a waiting patient, or -1 if they are not waiting. */
    public int positionOf(Patient patient) {
        int index = inOrder().indexOf(patient);
        return index < 0 ? -1 : index + 1;
    }

    /** Returns served patients in the order they were served. */
    public List<ServedPatient> getServed() {
        return List.copyOf(served);
    }

    /** Waiting patients whose ID matches exactly or whose name contains the text (case-insensitive). */
    public List<Patient> searchWaiting(String text) {
        return inOrder().stream().filter(p -> matches(p, text)).toList();
    }

    /** Served patients whose ID matches exactly or whose name contains the text (case-insensitive). */
    public List<ServedPatient> searchServed(String text) {
        return served.stream().filter(s -> matches(s.patient(), text)).toList();
    }

    private static boolean matches(Patient patient, String text) {
        String needle = text.trim().toLowerCase(Locale.ROOT);
        return patient.getId().toLowerCase(Locale.ROOT).equals(needle)
                || patient.getName().toLowerCase(Locale.ROOT).contains(needle);
    }
}
