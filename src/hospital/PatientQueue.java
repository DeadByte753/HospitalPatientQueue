package hospital;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Priority queue of patients: lowest priority rank first (HIGH before LOW),
 * then earliest arrival within the same priority.
 */
public class PatientQueue {

    private static final Comparator<Patient> ORDER =
            Comparator.comparingInt((Patient p) -> p.getPriority().getRank())
                      .thenComparingLong(Patient::getArrivalOrder);

    private final PriorityQueue<Patient> queue = new PriorityQueue<>(ORDER);

    /** Incremented for every admission; gives each patient a unique id and arrival order. */
    private long arrivalCounter = 0;

    public Patient admit(String name, Priority priority) {
        long order = ++arrivalCounter;
        Patient patient = new Patient(String.format("P%03d", order), name, priority, order);
        queue.add(patient);
        return patient;
    }

    /** Removes and returns the next patient to be seen, or null if the queue is empty. */
    public Patient callNext() {
        return queue.poll();
    }

    public Patient peek() {
        return queue.peek();
    }

    public boolean remove(Patient patient) {
        return queue.remove(patient);
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /** Returns the patients in the order they will be seen. */
    public List<Patient> inOrder() {
        List<Patient> list = new ArrayList<>(queue);
        list.sort(ORDER);
        return list;
    }
}
