package com.taskscheduler.ui;

import com.taskscheduler.exception.*;
import com.taskscheduler.manager.SchedulerManager;
import com.taskscheduler.model.*;
import com.taskscheduler.strategy.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Command-Line Interface for the Smart Task Scheduler.
 *
 * Provides a menu-driven loop for all CRUD operations,
 * strategy switching, and task completion.
 *
 * All user inputs are validated here before being passed to SchedulerManager.
 * InvalidDateException is thrown and caught here to provide clear feedback.
 */
public class CLI {

    private final SchedulerManager manager;
    private final Scanner          scanner;

    public CLI(SchedulerManager manager) {
        this.manager = manager;
        this.scanner = new Scanner(System.in);
    }

    // ── Main Menu Loop ─────────────────────────────────────────────

    public void start() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║   Smart Task Scheduler  v1.0         ║");
        System.out.println("╚══════════════════════════════════════╝");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter choice: ");

            try {
                switch (choice) {
                    case 1  -> addTask();
                    case 2  -> listTasks();
                    case 3  -> updateTask();
                    case 4  -> deleteTask();
                    case 5  -> completeTask();
                    case 6  -> switchStrategy();
                    case 7  -> running = false;
                    default -> System.out.println("✘ Invalid option. Try again.");
                }
            } catch (DeadlineConflictException e) {
                System.out.println("\n⛔ Scheduling Conflict: " + e.getMessage());
            } catch (EmptyTaskException e) {
                System.out.println("\n✘ Not Found: " + e.getMessage());
            } catch (InvalidDateException e) {
                System.out.println("\n✘ Date Error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("\n✘ Input Error: " + e.getMessage());
            }
        }

        System.out.println("\nGoodbye! Your tasks have been saved.");
    }

    // ── Menu Screens ──────────────────────────────────────────────

    private void printMenu() {
        System.out.println("\n──────────────────────────────────────");
        System.out.printf("  Active Strategy: %s%n", manager.getCurrentStrategyName());
        System.out.printf("  Tasks loaded:    %d%n", manager.size());
        System.out.println("──────────────────────────────────────");
        System.out.println("  1. Add Task");
        System.out.println("  2. View All Tasks");
        System.out.println("  3. Update Task");
        System.out.println("  4. Delete Task");
        System.out.println("  5. Mark Task Complete");
        System.out.println("  6. Switch Scheduling Strategy");
        System.out.println("  7. Exit");
        System.out.println("──────────────────────────────────────");
    }

    // ── Add Task ──────────────────────────────────────────────────

    private void addTask() throws InvalidDateException, DeadlineConflictException {
        System.out.println("\n--- Add New Task ---");
        System.out.println("  Type: 1=OneTime  2=Recurring  3=Urgent");
        int type = readInt("  Choice: ");

        String title       = readString("  Title: ");
        String description = readString("  Description (or press Enter to skip): ");
        int priority       = readInt("  Priority (1-5): ");
        LocalDate deadline = readDate("  Deadline (YYYY-MM-DD): ");
        int hours          = readInt("  Estimated hours: ");

        Task task = switch (type) {
            case 2 -> {
                int interval     = readInt("  Repeat every N days: ");
                int occurrences  = readInt("  Total occurrences (-1 for infinite): ");
                yield new RecurringTask(title, description, priority, deadline, hours,
                                        interval, occurrences);
            }
            case 3 -> {
                double multiplier = readDouble("  Urgency multiplier (e.g. 1.5): ");
                String reason     = readString("  Escalation reason: ");
                yield new UrgentTask(title, description, priority, deadline, hours,
                                     multiplier, reason);
            }
            default -> new OneTimeTask(title, description, priority, deadline, hours);
        };

        manager.addTask(task);
        System.out.println("✔ Task added: " + task);
    }

    // ── List Tasks ────────────────────────────────────────────────

    private void listTasks() {
        List<Task> tasks = manager.getAllTasks();
        if (tasks.isEmpty()) {
            System.out.println("\n  No tasks found.");
            return;
        }

        System.out.println("\n─── Task List (" + manager.getCurrentStrategyName() + ") ───");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.printf("  %2d. %s%n", i + 1, tasks.get(i));
        }
    }

    // ── Update Task ───────────────────────────────────────────────

    private void updateTask()
            throws EmptyTaskException, DeadlineConflictException, InvalidDateException {
        int id = readInt("  Enter task ID to update: ");

        System.out.println("  (Press Enter to keep current value)");
        String title       = readString("  New title: ");
        String description = readString("  New description: ");

        String priorityStr = readString("  New priority (1-5): ");
        int priority       = priorityStr.isEmpty() ? -1 : Integer.parseInt(priorityStr);

        String deadlineStr = readString("  New deadline (YYYY-MM-DD): ");
        LocalDate deadline = deadlineStr.isEmpty() ? null : parseDate(deadlineStr);

        String hoursStr    = readString("  New estimated hours: ");
        int hours          = hoursStr.isEmpty() ? -1 : Integer.parseInt(hoursStr);

        manager.updateTask(id,
            title.isEmpty()       ? null : title,
            description.isEmpty() ? null : description,
            priority, deadline, hours
        );
        System.out.println("✔ Task updated.");
    }

    // ── Delete / Complete ─────────────────────────────────────────

    private void deleteTask() throws EmptyTaskException {
        int id = readInt("  Enter task ID to delete: ");
        manager.deleteTask(id);
        System.out.println("✔ Task deleted.");
    }

    private void completeTask() throws EmptyTaskException {
        int id = readInt("  Enter task ID to mark complete: ");
        manager.completeTask(id);
        System.out.println("✔ Task marked as completed.");
    }

    // ── Strategy Switch ───────────────────────────────────────────

    private void switchStrategy() {
        System.out.println("\n  1. Earliest Deadline First");
        System.out.println("  2. Highest Priority First");
        System.out.println("  3. Combined Urgency Score");
        int choice = readInt("  Choice: ");

        SchedulingStrategy strategy = switch (choice) {
            case 1  -> new DeadlineStrategy();
            case 2  -> new PriorityStrategy();
            case 3  -> new UrgencyStrategy();
            default -> { System.out.println("Invalid choice — keeping current."); yield null; }
        };

        if (strategy != null) manager.setStrategy(strategy);
    }

    // ── Input Helpers ─────────────────────────────────────────────

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  ✘ Please enter a valid number.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  ✘ Please enter a valid decimal number.");
            }
        }
    }

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private LocalDate readDate(String prompt) throws InvalidDateException {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        return parseDate(input);
    }

    private LocalDate parseDate(String input) throws InvalidDateException {
        try {
            LocalDate date = LocalDate.parse(input);
            if (date.isBefore(LocalDate.now()))
                throw new InvalidDateException("Date must be today or in the future.", input);
            return date;
        } catch (DateTimeParseException e) {
            throw new InvalidDateException(
                "Could not parse date. Use format YYYY-MM-DD (e.g. 2025-12-01).", input, e);
        }
    }
}
