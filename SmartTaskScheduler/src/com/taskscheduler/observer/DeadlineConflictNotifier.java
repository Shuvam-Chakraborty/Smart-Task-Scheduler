package com.taskscheduler.observer;

import com.taskscheduler.model.Task;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Observer: Deadline Conflict Notifier
 *
 * Monitors the task list for two types of conflicts:
 *   1. OVERDUE TASKS     — tasks whose deadline has already passed.
 *   2. OVERLOAD DAYS     — days where estimated work hours exceed a safe threshold.
 *
 * Whenever SchedulerManager notifies this observer of a change,
 * it scans the task list and prints actionable alerts to the console.
 */
public class DeadlineConflictNotifier implements TaskObserver {

    private static final int MAX_HOURS_PER_DAY   = 8;  // configurable workload cap
    private static final int URGENT_WINDOW_DAYS  = 3;  // tasks due within 3 days = urgent alert

    private final List<String> alertLog = new ArrayList<>(); // stores alert history

    // ── Observer Callback ─────────────────────────────────────────

    @Override
    public void onTaskListUpdated(List<Task> tasks, String event) {
        alertLog.clear();
        checkOverdueTasks(tasks);
        checkUpcomingDeadlines(tasks);
        checkWorkloadConflicts(tasks);
        printAlerts(event);
    }

    // ── Private Helpers ───────────────────────────────────────────

    private void checkOverdueTasks(List<Task> tasks) {
        for (Task t : tasks) {
            if (!t.isCompleted() && t.getDeadline().isBefore(LocalDate.now())) {
                alertLog.add(String.format(
                    "⛔ OVERDUE: \"%s\" (ID %d) was due on %s",
                    t.getTitle(), t.getId(), t.getDeadline()
                ));
            }
        }
    }

    private void checkUpcomingDeadlines(List<Task> tasks) {
        LocalDate warningCutoff = LocalDate.now().plusDays(URGENT_WINDOW_DAYS);
        for (Task t : tasks) {
            if (!t.isCompleted()
                    && !t.getDeadline().isBefore(LocalDate.now())
                    && !t.getDeadline().isAfter(warningCutoff)) {
                alertLog.add(String.format(
                    "⚠  URGENT DEADLINE: \"%s\" (ID %d) is due in %d day(s) on %s",
                    t.getTitle(), t.getId(), t.daysUntilDeadline(), t.getDeadline()
                ));
            }
        }
    }

    private void checkWorkloadConflicts(List<Task> tasks) {
        // Group pending tasks by deadline date and sum estimated hours
        java.util.Map<LocalDate, Integer> dailyLoad = new java.util.HashMap<>();
        for (Task t : tasks) {
            if (!t.isCompleted()) {
                dailyLoad.merge(t.getDeadline(), t.getEstimatedHours(), Integer::sum);
            }
        }

        for (java.util.Map.Entry<LocalDate, Integer> entry : dailyLoad.entrySet()) {
            if (entry.getValue() > MAX_HOURS_PER_DAY) {
                alertLog.add(String.format(
                    "🔴 OVERLOAD on %s: %d hours of work scheduled (limit: %dh/day)",
                    entry.getKey(), entry.getValue(), MAX_HOURS_PER_DAY
                ));
            }
        }
    }

    private void printAlerts(String event) {
        if (alertLog.isEmpty()) return;

        System.out.println("\n┌─── CONFLICT NOTIFIER [triggered by: " + event + "] ───");
        for (String alert : alertLog) {
            System.out.println("│  " + alert);
        }
        System.out.println("└────────────────────────────────────────────────────\n");
    }

    // ── Accessors ─────────────────────────────────────────────────
    public List<String> getAlertLog() { return new ArrayList<>(alertLog); }
    public boolean hasAlerts()        { return !alertLog.isEmpty(); }
}
