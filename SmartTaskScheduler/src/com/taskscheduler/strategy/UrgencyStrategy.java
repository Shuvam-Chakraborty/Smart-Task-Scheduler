package com.taskscheduler.strategy;

import com.taskscheduler.model.Task;
import java.util.Comparator;
import java.util.List;

/**
 * Scheduling Strategy: Combined Urgency Score
 *
 * Uses each task's polymorphic getUrgencyScore() method to rank tasks.
 * This is the most intelligent strategy — it considers priority, deadline
 * proximity, AND task-type-specific factors (e.g., recurrence frequency,
 * urgency multipliers) all at once.
 *
 * This strategy demonstrates polymorphism: the same sort call handles
 * OneTimeTask, RecurringTask, and UrgentTask objects differently because
 * each overrides getUrgencyScore() with its own formula.
 */
public class UrgencyStrategy implements SchedulingStrategy {

    @Override
    public void sort(List<Task> tasks) {
        tasks.sort(
            Comparator.comparingDouble(Task::getUrgencyScore).reversed() // highest urgency first
        );
    }

    @Override
    public String getStrategyName() {
        return "Combined Urgency Score";
    }
}
