package com.taskscheduler.exception;

/**
 * Thrown when an operation is attempted on an empty or non-existent task.
 * Examples: updating a task ID that doesn't exist, searching an empty list.
 */
public class EmptyTaskException extends Exception {

    private final int attemptedId;

    public EmptyTaskException(String message) {
        super(message);
        this.attemptedId = -1;
    }

    public EmptyTaskException(String message, int attemptedId) {
        super(message);
        this.attemptedId = attemptedId;
    }

    public int getAttemptedId() { return attemptedId; }

    @Override
    public String toString() {
        if (attemptedId == -1)
            return "EmptyTaskException: " + getMessage();
        return String.format("EmptyTaskException: %s [Attempted ID: %d]",
            getMessage(), attemptedId);
    }
}
