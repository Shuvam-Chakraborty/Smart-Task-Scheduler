package com.taskscheduler.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Abstract base class representing a generic Task.
 * All task types must extend this class and implement
 * the getUrgencyScore() and getTaskType() methods.
 */
public abstract class Task implements Comparable<Task> {

    // ── Core Fields ────────────────────────────────────────────────
    private static int idCounter = 1;

    private final int    id;
    private       String title;
    private       String description;
    private       int    priority;      // 1 (low) → 5 (critical)
    private       LocalDate deadline;
    private       int    estimatedHours;
    private       boolean completed;

    // ── Constructor ────────────────────────────────────────────────
    public Task(String title, String description, int priority,
                LocalDate deadline, int estimatedHours) {

        if (title == null || title.trim().isEmpty())
            throw new IllegalArgumentException("Task title cannot be empty.");
        if (priority < 1 || priority > 5)
            throw new IllegalArgumentException("Priority must be between 1 and 5.");
        if (deadline == null || deadline.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Deadline must be a future date.");
        if (estimatedHours <= 0)
            throw new IllegalArgumentException("Estimated hours must be positive.");

        this.id             = idCounter++;
        this.title          = title.trim();
        this.description    = description;
        this.priority       = priority;
        this.deadline       = deadline;
        this.estimatedHours = estimatedHours;
        this.completed      = false;
    }

    // Constructor used during file loading (preserves original ID)
    public Task(int id, String title, String description, int priority,
                LocalDate deadline, int estimatedHours, boolean completed) {
        this.id             = id;
        this.title          = title;
        this.description    = description;
        this.priority       = priority;
        this.deadline       = deadline;
        this.estimatedHours = estimatedHours;
        this.completed      = completed;
        if (id >= idCounter) idCounter = id + 1;
    }

    // ── Abstract Methods ───────────────────────────────────────────

    /**
     * Each subclass calculates urgency differently.
     * Used by the UrgencyStrategy for combined scoring.
     */
    public abstract double getUrgencyScore();

    /**
     * Returns a label for the task type (e.g., "ONE_TIME", "RECURRING").
     */
    public abstract String getTaskType();

    // ── Shared Behaviour ──────────────────────────────────────────

    /**
     * Days remaining until deadline. Returns 0 if already due.
     */
    public long daysUntilDeadline() {
        return Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), deadline));
    }

    /**
     * Natural ordering: higher priority first; ties broken by closer deadline.
     */
    @Override
    public int compareTo(Task other) {
        if (this.priority != other.priority)
            return Integer.compare(other.priority, this.priority); // descending
        return this.deadline.compareTo(other.deadline);            // ascending
    }

    /**
     * Human-readable summary of the task.
     */
    @Override
    public String toString() {
        return String.format(
            "[%s] #%d %-25s | Priority: %d | Deadline: %s (%d days) | Est: %dh | %s",
            getTaskType(), id, title, priority, deadline,
            daysUntilDeadline(), estimatedHours,
            completed ? "DONE" : "PENDING"
        );
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public int        getId()             { return id; }
    public String     getTitle()          { return title; }
    public String     getDescription()    { return description; }
    public int        getPriority()       { return priority; }
    public LocalDate  getDeadline()       { return deadline; }
    public int        getEstimatedHours() { return estimatedHours; }
    public boolean    isCompleted()       { return completed; }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty())
            throw new IllegalArgumentException("Title cannot be empty.");
        this.title = title.trim();
    }

    public void setDescription(String description) { this.description = description; }

    public void setPriority(int priority) {
        if (priority < 1 || priority > 5)
            throw new IllegalArgumentException("Priority must be 1–5.");
        this.priority = priority;
    }

    public void setDeadline(LocalDate deadline) {
        if (deadline == null || deadline.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Deadline must be a future date.");
        this.deadline = deadline;
    }

    public void setEstimatedHours(int hours) {
        if (hours <= 0) throw new IllegalArgumentException("Hours must be positive.");
        this.estimatedHours = hours;
    }

    public void markCompleted() { this.completed = true; }
    public void markPending()   { this.completed = false; }
}
