package hospital;

import java.time.LocalDateTime;

/**
 * A patient waiting in the queue.
 */
public class Patient {

    private final int id;
    private final String name;
    private final int age;
    private final String condition;
    private final Priority priority;
    private final LocalDateTime arrivalTime;

    public Patient(int id, String name, int age, String condition, Priority priority) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.condition = condition;
        this.priority = priority;
        this.arrivalTime = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getCondition() {
        return condition;
    }

    public Priority getPriority() {
        return priority;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    @Override
    public String toString() {
        return "#" + id + " " + name + " (" + priority + ")";
    }
}
