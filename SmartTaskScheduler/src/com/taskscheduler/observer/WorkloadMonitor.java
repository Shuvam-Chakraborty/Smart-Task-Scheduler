package com.taskscheduler.observer;

import com.taskscheduler.model.Task;
import java.util.List;

/**
 * Observer: Workload Monitor
 *
 * Tracks aggregate workload statistics across the entire task list.
 * On every update, it reports:
 *   - Total pending tasks and their combined estimated hours
 *   - Number of high-priority tasks (priority >= 4)
 *   - A simple workload health label: LIGHT / MODERATE / HEAVY / OVERLOADED
 *
 * This helps users quickly understand whether they are taking on too much
 * before adding new tasks.
 */
public class WorkloadMonitor implements TaskObserver {

    // Thresholds for workload health labels (in total pending hours)
    private static final int LIGHT_THRESHOLD      = 10;
    private static final int MODERATE_THRESHOLD   = 25;
    private static final int HEAVY_THRESHOLD      = 50;

    private int    lastPendingCount = 0;
    private int    lastTotalHours   = 0;
    private String lastHealthLabel  = "LIGHT";

    // ── Observer Callback ─────────────────────────────────────────

    @Override
    public void onTaskListUpdated(List<Task> tasks, String event) {
        int pendingCount   = 0;
        int totalHours     = 0;
        int highPriorityCount = 0;

        for (Task t : tasks) {
            if (!t.isCompleted()) {
                pendingCount++;
                totalHours += t.getEstimatedHours();
                if (t.getPriority() >= 4) highPriorityCount++;
            }
        }

        lastPendingCount = pendingCount;
        lastTotalHours   = totalHours;
        lastHealthLabel  = computeHealthLabel(totalHours);

        printSummary(pendingCount, totalHours, highPriorityCount, event);
    }

    // ── Private Helpers ───────────────────────────────────────────

    private String computeHealthLabel(int totalHours) {
        if (totalHours <= LIGHT_THRESHOLD)    return "🟢 LIGHT";
        if (totalHours <= MODERATE_THRESHOLD) return "🟡 MODERATE";
        if (totalHours <= HEAVY_THRESHOLD)    return "🟠 HEAVY";
        return "🔴 OVERLOADED";
    }

    private void printSummary(int pending, int hours, int highPri, String event) {
        System.out.printf(
            "%n[WorkloadMonitor] Event: %-15s | Pending: %2d task(s) | " +
            "Est. Hours: %3dh | High-Priority: %d | Status: %s%n",
            event, pending, hours, highPri, computeHealthLabel(hours)
        );
    }

    // ── Accessors ─────────────────────────────────────────────────
    public int    getPendingCount()   { return lastPendingCount; }
    public int    getTotalHours()     { return lastTotalHours; }
    public String getHealthLabel()    { return lastHealthLabel; }
}
