package com.taskscheduler.model;

import java.time.LocalDate;

/**
 * Represents a task that occurs only once.
 * Urgency score is purely deadline-driven — the fewer days remaining,
 * the higher the urgency relative to its priority.
 */
public class OneTimeTask extends Task {

    // ── Constructor ────────────────────────────────────────────────
    public OneTimeTask(String title, String description, int priority,
                       LocalDate deadline, int estimatedHours) {
        super(title, description, priority, deadline, estimatedHours);
    }

    // Constructor for loading from file
    public OneTimeTask(int id, String title, String description, int priority,
                       LocalDate deadline, int estimatedHours, boolean completed) {
        super(id, title, description, priority, deadline, estimatedHours, completed);
    }

    // ── Abstract Implementations ──────────────────────────────────

    /**
     * Urgency = priority * (1 / daysRemaining + 1)
     * A task due tomorrow with priority 5 scores much higher than
     * one due in 30 days with the same priority.
     */
    @Override
    public double getUrgencyScore() {
        long days = daysUntilDeadline();
        double deadlineFactor = 1.0 / (days + 1); // +1 avoids division by zero
        return getPriority() * (1 + deadlineFactor * 10);
    }

    @Override
    public String getTaskType() {
        return "ONE_TIME";
    }
}
