package com.taskscheduler;

import com.taskscheduler.manager.SchedulerManager;
import com.taskscheduler.observer.DeadlineConflictNotifier;
import com.taskscheduler.observer.WorkloadMonitor;
import com.taskscheduler.persistence.TaskFileHandler;
import com.taskscheduler.model.Task;
import com.taskscheduler.ui.CLI;

import java.io.IOException;
import java.util.List;

/**
 * Entry point for the Smart Task Scheduler.
 *
 * Boot sequence:
 *   1. Create SchedulerManager with default strategy (Priority)
 *   2. Register observers (DeadlineConflictNotifier, WorkloadMonitor)
 *   3. Load existing tasks from data/tasks.json
 *   4. Launch the CLI
 *   5. On exit, save all tasks back to data/tasks.json
 */
public class Main {

    private static final String DATA_FILE = "data/tasks.json";

    public static void main(String[] args) {
        // 1. Build manager
        SchedulerManager manager = new SchedulerManager();

        // 2. Register observers
        manager.addObserver(new DeadlineConflictNotifier());
        manager.addObserver(new WorkloadMonitor());

        // 3. Load persisted tasks
        TaskFileHandler fileHandler = new TaskFileHandler(DATA_FILE);
        try {
            List<Task> saved = fileHandler.loadTasks();
            manager.loadTasks(saved);
            if (!saved.isEmpty()) {
                System.out.println("✔ Loaded " + saved.size() + " task(s) from " + DATA_FILE);
            }
        } catch (IOException e) {
            System.out.println("⚠ Could not load tasks: " + e.getMessage());
            System.out.println("  Starting with an empty task list.");
        }

        // 4. Launch CLI
        CLI cli = new CLI(manager);
        cli.start();

        // 5. Save on exit
        try {
            fileHandler.saveTasks(manager.getAllTasks());
            System.out.println("✔ Tasks saved to " + DATA_FILE);
        } catch (IOException e) {
            System.out.println("⚠ Could not save tasks: " + e.getMessage());
        }
    }
}
