package com.taskscheduler.observer;

import com.taskscheduler.model.Task;
import java.util.List;

/**
 * Observer interface for the Smart Task Scheduler.
 *
 * Any component that needs to react to changes in the task list
 * implements this interface and registers with SchedulerManager.
 * When tasks are added, updated, or removed, all registered observers
 * are automatically notified (Observer Pattern).
 */
public interface TaskObserver {

    /**
     * Called by SchedulerManager whenever the task list changes.
     *
     * @param tasks    the current full list of tasks after the change
     * @param event    a description of what triggered the update
     *                 (e.g., "TASK_ADDED", "TASK_UPDATED", "TASK_DELETED")
     */
    void onTaskListUpdated(List<Task> tasks, String event);
}
