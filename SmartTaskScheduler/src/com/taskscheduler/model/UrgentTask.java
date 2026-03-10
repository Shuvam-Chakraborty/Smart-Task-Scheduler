package com.taskscheduler.model;

import java.time.LocalDate;

/**
 * Represents a high-stakes task with an explicit urgency multiplier.
 * This models real-world scenarios like "exam tomorrow" or "client demo in 2 hours"
 * where standard priority alone doesn't capture the true urgency.
 *
 * Inherits from Task and adds an urgencyMultiplier field to amplify scheduling weight.
 */
public class UrgentTask extends Task {

    // ── Additional Fields ─────────────────────────────────────────
    private double urgencyMultiplier;  // e.g., 1.5 = 50% more urgent than a normal task
    private String escalationReason;   // Why this task was marked urgent

    // ── Constructor ────────────────────────────────────────────────
    public UrgentTask(String title, String description, int priority,
                      LocalDate deadline, int estimatedHours,
                      double urgencyMultiplier, String escalationReason) {
        super(title, description, priority, deadline, estimatedHours);

        if (urgencyMultiplier < 1.0)
            throw new IllegalArgumentException("Urgency multiplier must be >= 1.0");

        this.urgencyMultiplier = urgencyMultiplier;
        this.escalationReason  = escalationReason;
    }

    // Constructor for loading from file
    public UrgentTask(int id, String title, String description, int priority,
                      LocalDate deadline, int estimatedHours, boolean completed,
                      double urgencyMultiplier, String escalationReason) {
        super(id, title, description, priority, deadline, estimatedHours, completed);
        this.urgencyMultiplier = urgencyMultiplier;
        this.escalationReason  = escalationReason;
    }

    // ── Abstract Implementations ──────────────────────────────────

    /**
     * Urgency = base score × urgencyMultiplier
     * The multiplier directly inflates scheduling priority above all other task types.
     */
    @Override
    public double getUrgencyScore() {
        long days = daysUntilDeadline();
        double baseScore = getPriority() * (1.0 + (1.0 / (days + 1)) * 10);
        return baseScore * urgencyMultiplier;
    }

    @Override
    public String getTaskType() {
        return "URGENT";
    }

    // ── Getters & Setters ─────────────────────────────────────────
    public double getUrgencyMultiplier()  { return urgencyMultiplier; }
    public String getEscalationReason()   { return escalationReason; }

    public void setUrgencyMultiplier(double multiplier) {
        if (multiplier < 1.0)
            throw new IllegalArgumentException("Multiplier must be >= 1.0");
        this.urgencyMultiplier = multiplier;
    }

    public void setEscalationReason(String reason) {
        this.escalationReason = reason;
    }

    @Override
    public String toString() {
        return super.toString()
            + String.format(" | ⚠ URGENT x%.1f (%s)", urgencyMultiplier, escalationReason);
    }
}
