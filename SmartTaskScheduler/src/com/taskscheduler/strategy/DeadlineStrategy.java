package com.taskscheduler.strategy;

import com.taskscheduler.model.Task;
import java.util.Comparator;
import java.util.List;

/**
 * Scheduling Strategy: Earliest Deadline First (EDF)
 *
 * Tasks are ordered so that whatever is due soonest appears at the top.
 * This is ideal when the user's primary concern is "what is about to expire?"
 *
 * Tie-breaking: if two tasks share a deadline, higher priority comes first.
 */
public class DeadlineStrategy implements SchedulingStrategy {

    @Override
    public void sort(List<Task> tasks) {
        tasks.sort(
            Comparator.comparing(Task::getDeadline)             // closest deadline first
                      .thenComparing(Comparator.comparingInt(Task::getPriority).reversed()) // higher priority first on tie
        );
    }

    @Override
    public String getStrategyName() {
        return "Earliest Deadline First";
    }
}
