package com.taskscheduler.model;

import java.time.LocalDate;

/**
 * Represents a task that repeats on a fixed interval (e.g., every 7 days).
 * Urgency accounts for both deadline proximity AND recurrence frequency —
 * a daily recurring task is inherently more urgent than a monthly one.
 */
public class RecurringTask extends Task {

    // ── Additional Fields ─────────────────────────────────────────
    private int intervalDays;   // How often the task repeats (e.g., 7 = weekly)
    private int occurrencesLeft; // How many times it must still repeat (-1 = infinite)

    // ── Constructor ────────────────────────────────────────────────
    public RecurringTask(String title, String description, int priority,
                         LocalDate deadline, int estimatedHours,
                         int intervalDays, int occurrencesLeft) {
        super(title, description, priority, deadline, estimatedHours);

        if (intervalDays <= 0)
            throw new IllegalArgumentException("Interval days must be positive.");

        this.intervalDays    = intervalDays;
        this.occurrencesLeft = occurrencesLeft;
    }

    // Constructor for loading from file
    public RecurringTask(int id, String title, String description, int priority,
                         LocalDate deadline, int estimatedHours, boolean completed,
                         int intervalDays, int occurrencesLeft) {
        super(id, title, description, priority, deadline, estimatedHours, completed);
        this.intervalDays    = intervalDays;
        this.occurrencesLeft = occurrencesLeft;
    }

    // ── Abstract Implementations ──────────────────────────────────

    /**
     * Urgency = priority * deadlineFactor * frequencyBoost
     * More frequent tasks (smaller interval) get a higher urgency boost.
     */
    @Override
    public double getUrgencyScore() {
        long days = daysUntilDeadline();
        double deadlineFactor   = 1.0 / (days + 1);
        double frequencyBoost   = 30.0 / intervalDays; // daily=30x, weekly=~4x, monthly=1x
        return getPriority() * (1 + deadlineFactor * 10) * frequencyBoost;
    }

    @Override
    public String getTaskType() {
        return "RECURRING";
    }

    /**
     * Advances the deadline by one interval after the current occurrence is done.
     * Decrements occurrences if not infinite.
     */
    public void scheduleNextOccurrence() {
        if (occurrencesLeft == 0)
            throw new IllegalStateException("No more occurrences left for: " + getTitle());

        setDeadline(getDeadline().plusDays(intervalDays));
        if (occurrencesLeft > 0) occurrencesLeft--;
        markPending();
    }

    public boolean hasOccurrencesLeft() {
        return occurrencesLeft != 0;
    }

    // ── Getters & Setters ─────────────────────────────────────────
    public int getIntervalDays()    { return intervalDays; }
    public int getOccurrencesLeft() { return occurrencesLeft; }

    public void setIntervalDays(int days) {
        if (days <= 0) throw new IllegalArgumentException("Interval must be positive.");
        this.intervalDays = days;
    }

    @Override
    public String toString() {
        String base = super.toString();
        String recInfo = String.format(" | Every %dd | Remaining: %s",
            intervalDays, occurrencesLeft == -1 ? "∞" : String.valueOf(occurrencesLeft));
        return base + recInfo;
    }
}
