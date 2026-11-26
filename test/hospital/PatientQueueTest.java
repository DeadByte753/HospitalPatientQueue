package hospital;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for PatientQueue, written as a plain main method because the project uses
 * no external libraries (so no JUnit).
 *
 * Compile and run from the project folder:
 *   javac -d out src/hospital/*.java test/hospital/*.java
 *   java -cp out hospital.PatientQueueTest
 *
 * Prints PASS/FAIL for every check and exits with status 1 if any check fails.
 */
public class PatientQueueTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        serveOrderFollowsPriorityThenArrival();
        rankingMatchesServeOrder();
        duplicateIdIsRejected();
        blankFieldsAreRejected();
        servingFromEmptyQueueIsSafe();
        emptyQueueStatisticsAreZero();

        System.out.println();
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        System.exit(failed == 0 ? 0 : 1);
    }

    /** Registers A..E in the given order and checks they are served A, D, C, B, E. */
    private static PatientQueue sampleQueue() {
        PatientQueue queue = new PatientQueue();
        queue.admit("A", "Patient A", Priority.HIGH);
        queue.admit("B", "Patient B", Priority.LOW);
        queue.admit("C", "Patient C", Priority.MEDIUM);
        queue.admit("D", "Patient D", Priority.HIGH);
        queue.admit("E", "Patient E", Priority.LOW);
        return queue;
    }

    private static void serveOrderFollowsPriorityThenArrival() {
        section("Serve order: A(High), B(Low), C(Medium), D(High), E(Low)");
        PatientQueue queue = sampleQueue();

        List<String> served = new ArrayList<>();
        Patient next;
        while ((next = queue.serveNext()) != null) {
            served.add(next.getId());
            check(next.getStatus() == Patient.Status.SERVED, next.getId() + " is marked Served");
        }

        check(served.equals(List.of("A", "D", "C", "B", "E")),
                "serve order is A, D, C, B, E (got " + served + ")");
        check(queue.isEmpty() && queue.getServed().size() == 5, "queue is empty and 5 patients are served");
    }

    private static void rankingMatchesServeOrder() {
        section("Ranking shown in the table matches the serve order");
        PatientQueue queue = sampleQueue();
        List<String> ranked = queue.inOrder().stream().map(Patient::getId).toList();
        check(ranked.equals(List.of("A", "D", "C", "B", "E")), "Rank 1..5 is A, D, C, B, E (got " + ranked + ")");
        check(queue.size() == 5, "showing the ranking does not remove anyone from the queue");
    }

    private static void duplicateIdIsRejected() {
        section("Duplicate ID is rejected");
        PatientQueue queue = sampleQueue();

        check(!queue.validateRegistration("A", "Someone Else", Priority.LOW).isEmpty(),
                "ID of a waiting patient is rejected");
        check(!queue.validateRegistration("  a  ", "Someone Else", Priority.LOW).isEmpty(),
                "same ID in lower case with spaces is rejected");

        queue.serveNext(); // A is now served
        check(!queue.validateRegistration("A", "Someone Else", Priority.LOW).isEmpty(),
                "ID of a served patient is rejected");

        int before = queue.size();
        check(throwsIllegalArgument(() -> queue.admit("D", "Duplicate D", Priority.HIGH)),
                "admit() throws for a duplicate ID");
        check(queue.size() == before, "queue unchanged after a rejected duplicate");
    }

    private static void blankFieldsAreRejected() {
        section("Blank fields are rejected");
        PatientQueue queue = new PatientQueue();

        check(hasErrorContaining(queue.validateRegistration("", "Name", Priority.HIGH), "ID is required"),
                "empty ID is rejected");
        check(hasErrorContaining(queue.validateRegistration("   ", "Name", Priority.HIGH), "ID is required"),
                "ID of only spaces is rejected");
        check(hasErrorContaining(queue.validateRegistration("X1", "   ", Priority.HIGH), "name is required"),
                "name of only spaces is rejected");
        check(hasErrorContaining(queue.validateRegistration("X1", "Name", null), "Priority is required"),
                "missing priority is rejected");
        check(queue.validateRegistration(" ", " ", null).size() == 3,
                "all three blank gives three separate errors");

        check(throwsIllegalArgument(() -> queue.admit("X1", " ", Priority.HIGH)),
                "admit() throws for a blank name");
        check(queue.isEmpty(), "nothing was added");

        Patient ok = queue.admit("  X1 ", "  Valid Name  ", Priority.MEDIUM);
        check(ok.getId().equals("X1") && ok.getName().equals("Valid Name"),
                "valid input is accepted and trimmed");
    }

    private static void servingFromEmptyQueueIsSafe() {
        section("Serving from an empty queue is safe");
        PatientQueue queue = new PatientQueue();

        Patient result = null;
        boolean threw = false;
        try {
            result = queue.serveNext();
            queue.serveNext(); // twice in a row is also fine
        } catch (RuntimeException e) {
            threw = true;
        }
        check(!threw, "serveNext() on an empty queue does not throw");
        check(result == null, "serveNext() returns null");
        check(queue.getServed().isEmpty(), "nothing is added to the served list");
        check(queue.peek() == null && queue.inOrder().isEmpty(), "peek() is null and the ranking is empty");

        PatientQueue drained = sampleQueue();
        while (drained.serveNext() != null) {
            // serve everyone
        }
        int servedBefore = drained.getServed().size();
        check(drained.serveNext() == null && drained.getServed().size() == servedBefore,
                "serving again after the queue empties changes nothing");
    }

    private static void emptyQueueStatisticsAreZero() {
        section("Empty queue statistics");
        PatientQueue queue = new PatientQueue();
        int waiting = queue.size();
        int served = queue.getServed().size();
        check(waiting == 0 && served == 0 && waiting + served == 0,
                "Total Registered, Waiting and Served are all 0");
    }

    // ---------------------------------------------------------------- helpers

    private static void section(String title) {
        System.out.println();
        System.out.println("== " + title);
    }

    private static void check(boolean condition, String description) {
        if (condition) {
            passed++;
            System.out.println("  PASS  " + description);
        } else {
            failed++;
            System.out.println("  FAIL  " + description);
        }
    }

    private static boolean hasErrorContaining(List<String> errors, String text) {
        return errors.stream().anyMatch(e -> e.contains(text));
    }

    private static boolean throwsIllegalArgument(Runnable action) {
        try {
            action.run();
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}
