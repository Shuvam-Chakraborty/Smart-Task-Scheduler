package com.taskscheduler.strategy;

import com.taskscheduler.model.Task;
import java.util.List;

/**
 * Strategy interface for task scheduling algorithms.
 *
 * Any new ordering algorithm simply implements this interface —
 * the SchedulerManager switches between them at runtime without
 * modifying any existing code (Open/Closed Principle).
 */
public interface SchedulingStrategy {

    /**
     * Sorts the given list of tasks according to this strategy's ordering rules.
     * Operates in-place (modifies the list directly).
     *
     * @param tasks the mutable list of tasks to be reordered
     */
    void sort(List<Task> tasks);

    /**
     * Returns a human-readable name for the strategy.
     * Used in the CLI menu and reports.
     */
    String getStrategyName();
}
