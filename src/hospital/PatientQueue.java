package hospital;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Priority queue of patients: highest triage priority first, then earliest arrival.
 */
public class PatientQueue {

    private static final Comparator<Patient> ORDER =
            Comparator.comparingInt((Patient p) -> p.getPriority().getLevel())
                      .thenComparingInt(Patient::getId);

    private final PriorityQueue<Patient> queue = new PriorityQueue<>(ORDER);
    private int nextId = 1;

    public Patient admit(String name, int age, String condition, Priority priority) {
        Patient patient = new Patient(nextId++, name, age, condition, priority);
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
