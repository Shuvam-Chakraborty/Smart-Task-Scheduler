package com.taskscheduler.manager;

import com.taskscheduler.exception.DeadlineConflictException;
import com.taskscheduler.exception.EmptyTaskException;
import com.taskscheduler.model.Task;
import com.taskscheduler.observer.TaskObserver;
import com.taskscheduler.strategy.SchedulingStrategy;
import com.taskscheduler.strategy.PriorityStrategy;

import java.time.LocalDate;
import java.util.*;

/**
 * Central coordinator for the Smart Task Scheduler.
 *
 * Responsibilities:
 *   1. Maintains the master task list.
 *   2. Applies the active SchedulingStrategy to reorder tasks on every change.
 *   3. Notifies all registered TaskObservers when the list changes.
 *   4. Enforces workload rules (e.g., max 12 hours/day) via DeadlineConflictException.
 *
 * Design Patterns used here:
 *   - Strategy Pattern: strategy field is swappable at runtime.
 *   - Observer Pattern: observers list is notified after every mutation.
 */
public class SchedulerManager {

    private static final int MAX_DAILY_HOURS = 12; // hard workload cap

    // ── Core State ────────────────────────────────────────────────
    private final List<Task>          tasks     = new ArrayList<>();
    private final List<TaskObserver>  observers = new ArrayList<>();
    private       SchedulingStrategy  strategy;

    // ── Constructor ────────────────────────────────────────────────
    public SchedulerManager() {
        this.strategy = new PriorityStrategy(); // default strategy
    }

    public SchedulerManager(SchedulingStrategy strategy) {
        this.strategy = strategy;
    }

    // ── Strategy Pattern ──────────────────────────────────────────

    public void setStrategy(SchedulingStrategy strategy) {
        this.strategy = strategy;
        resort();
        System.out.println("✔ Strategy switched to: " + strategy.getStrategyName());
    }

    public String getCurrentStrategyName() {
        return strategy.getStrategyName();
    }

    private void resort() {
        strategy.sort(tasks);
    }

    // ── Observer Pattern ──────────────────────────────────────────

    public void addObserver(TaskObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(TaskObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(String event) {
        List<Task> snapshot = Collections.unmodifiableList(tasks);
        for (TaskObserver obs : observers) {
            obs.onTaskListUpdated(snapshot, event);
        }
    }

    // ── CRUD Operations ───────────────────────────────────────────

    /**
     * Adds a new task. Validates workload cap before insertion.
     */
    public void addTask(Task task) throws DeadlineConflictException {
        checkWorkloadCap(task, -1);
        tasks.add(task);
        resort();
        notifyObservers("TASK_ADDED");
    }

    /**
     * Returns the full sorted task list (unmodifiable view).
     */
    public List<Task> getAllTasks() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * Returns only pending (incomplete) tasks.
     */
    public List<Task> getPendingTasks() {
        List<Task> pending = new ArrayList<>();
        for (Task t : tasks) {
            if (!t.isCompleted()) pending.add(t);
        }
        return pending;
    }

    /**
     * Finds a task by its ID.
     */
    public Task getTaskById(int id) throws EmptyTaskException {
        for (Task t : tasks) {
            if (t.getId() == id) return t;
        }
        throw new EmptyTaskException("No task found with ID: " + id, id);
    }

    /**
     * Updates the title, description, priority, deadline, or hours of a task.
     * Pass null / -1 to leave a field unchanged.
     */
    public void updateTask(int id, String newTitle, String newDescription,
                           int newPriority, LocalDate newDeadline,
                           int newHours)
            throws EmptyTaskException, DeadlineConflictException {

        Task task = getTaskById(id);

        if (newTitle       != null && !newTitle.isEmpty())  task.setTitle(newTitle);
        if (newDescription != null)                         task.setDescription(newDescription);
        if (newPriority    != -1)                           task.setPriority(newPriority);
        if (newDeadline    != null)                         task.setDeadline(newDeadline);
        if (newHours       != -1)                           task.setEstimatedHours(newHours);

        checkWorkloadCap(task, id); // re-check after update
        resort();
        notifyObservers("TASK_UPDATED");
    }

    /**
     * Removes a task by ID.
     */
    public void deleteTask(int id) throws EmptyTaskException {
        Task task = getTaskById(id);
        tasks.remove(task);
        notifyObservers("TASK_DELETED");
    }

    /**
     * Marks a task as complete and triggers a re-sort.
     */
    public void completeTask(int id) throws EmptyTaskException {
        Task task = getTaskById(id);
        task.markCompleted();
        resort();
        notifyObservers("TASK_COMPLETED");
    }

    /**
     * Loads tasks from persistence layer (bypasses workload check on startup).
     */
    public void loadTasks(List<Task> loaded) {
        tasks.clear();
        tasks.addAll(loaded);
        resort();
        // No observer notification on load — avoids noisy startup alerts
    }

    // ── Workload Validation ───────────────────────────────────────

    /**
     * Sums estimated hours for all tasks on the same deadline day.
     * excludeId allows updating an existing task without double-counting it.
     */
    private void checkWorkloadCap(Task incoming, int excludeId)
            throws DeadlineConflictException {

        int hoursOnDay = incoming.getEstimatedHours();

        for (Task t : tasks) {
            if (t.getId() == excludeId) continue;
            if (!t.isCompleted() && t.getDeadline().equals(incoming.getDeadline())) {
                hoursOnDay += t.getEstimatedHours();
            }
        }

        if (hoursOnDay > MAX_DAILY_HOURS) {
            throw new DeadlineConflictException(
                String.format("Adding \"%s\" would schedule %dh on %s (cap: %dh)",
                    incoming.getTitle(), hoursOnDay, incoming.getDeadline(), MAX_DAILY_HOURS),
                incoming.getDeadline(),
                hoursOnDay
            );
        }
    }

    // ── Utility ───────────────────────────────────────────────────

    public int size()      { return tasks.size(); }
    public boolean isEmpty() { return tasks.isEmpty(); }
}
