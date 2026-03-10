package com.taskscheduler.strategy;

import com.taskscheduler.model.Task;
import java.util.Comparator;
import java.util.List;

/**
 * Scheduling Strategy: Highest Priority First
 *
 * Tasks are ordered purely by their priority value (5 = critical, 1 = low).
 * This is useful when the user needs to focus on importance over immediacy.
 *
 * Tie-breaking: if two tasks share the same priority, the one with the
 * closest deadline comes first.
 */
public class PriorityStrategy implements SchedulingStrategy {

    @Override
    public void sort(List<Task> tasks) {
        tasks.sort(
            Comparator.comparingInt(Task::getPriority).reversed()  // highest priority first
                      .thenComparing(Task::getDeadline)            // closest deadline on tie
        );
    }

    @Override
    public String getStrategyName() {
        return "Highest Priority First";
    }
}
