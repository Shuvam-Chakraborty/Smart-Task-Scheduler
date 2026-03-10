package com.taskscheduler.exception;

import java.time.LocalDate;
import java.util.List;

/**
 * Thrown when adding or updating a task creates an unresolvable scheduling conflict.
 * Examples: too many tasks due on the same day, estimated hours exceed daily limit.
 */
public class DeadlineConflictException extends Exception {

    private final LocalDate conflictDate;
    private final int       totalHoursOnDate;

    public DeadlineConflictException(String message,
                                     LocalDate conflictDate,
                                     int totalHoursOnDate) {
        super(message);
        this.conflictDate     = conflictDate;
        this.totalHoursOnDate = totalHoursOnDate;
    }

    public LocalDate getConflictDate()     { return conflictDate; }
    public int       getTotalHoursOnDate() { return totalHoursOnDate; }

    @Override
    public String toString() {
        return String.format(
            "DeadlineConflictException: %s [Date: %s | Total hours: %dh]",
            getMessage(), conflictDate, totalHoursOnDate
        );
    }
}
