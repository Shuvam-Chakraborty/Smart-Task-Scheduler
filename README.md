# Smart Task Scheduler

A command-line Java application that helps students and professionals manage deadlines dynamically. Tasks are automatically reordered as conditions change using a pluggable scheduling strategy, and observers fire alerts when conflicts or overloads are detected.

---

## Requirements

- **Java 17 or higher**
- No external libraries — pure Java standard library only

To check your Java version:
```bash
java -version
```

---

## Project Structure

```
SmartTaskScheduler/
├── src/com/taskscheduler/
│   ├── Main.java
│   ├── model/
│   │   ├── Task.java                     # Abstract base class
│   │   ├── OneTimeTask.java
│   │   ├── RecurringTask.java
│   │   └── UrgentTask.java
│   ├── strategy/
│   │   ├── SchedulingStrategy.java       # Interface
│   │   ├── DeadlineStrategy.java
│   │   ├── PriorityStrategy.java
│   │   └── UrgencyStrategy.java
│   ├── observer/
│   │   ├── TaskObserver.java             # Interface
│   │   ├── DeadlineConflictNotifier.java
│   │   └── WorkloadMonitor.java
│   ├── manager/
│   │   └── SchedulerManager.java
│   ├── persistence/
│   │   └── TaskFileHandler.java
│   ├── exception/
│   │   ├── InvalidDateException.java
│   │   ├── DeadlineConflictException.java
│   │   └── EmptyTaskException.java
│   └── ui/
│       └── CLI.java
├── data/
│   └── tasks.json
├── docs/
│   └── ClassDiagram.png
├── report/
│   └── ProjectReport.pdf
└── slides/
    └── Presentation.pdf
```

---

## How to Compile

From the root of the project directory (`SmartTaskScheduler/`):

```bash
find src -name "*.java" > sources.txt
mkdir -p out
javac -d out @sources.txt
```

---

## How to Run

```bash
java -cp out com.taskscheduler.Main
```

Tasks are automatically saved to `data/tasks.json` on exit and reloaded on the next launch.

---

## Features

- **3 Task Types** — One-Time, Recurring (with interval + occurrence count), and Urgent (with a custom urgency multiplier)
- **3 Scheduling Strategies** — switch at runtime between:
  - Earliest Deadline First
  - Highest Priority First
  - Combined Urgency Score (accounts for task type, priority, and deadline together)
- **Observer Alerts** — automatic notifications for overdue tasks, upcoming deadlines within 3 days, and days with more than 8 hours of work scheduled
- **Full CRUD** — create, view, update, delete, and mark tasks complete
- **JSON Persistence** — tasks survive between sessions with no database required
- **Input Validation** — custom exceptions for invalid dates, scheduling conflicts, and missing tasks

---

## Task Priority Scale

| Value | Meaning  |
|-------|----------|
| 1     | Low      |
| 2     | Minor    |
| 3     | Normal   |
| 4     | High     |
| 5     | Critical |

---

## Example Session

```
╔══════════════════════════════════════╗
║   Smart Task Scheduler  v1.0         ║
╚══════════════════════════════════════╝

──────────────────────────────────────
  Active Strategy: Highest Priority First
  Tasks loaded:    3
──────────────────────────────────────
  1. Add Task
  2. View All Tasks
  3. Update Task
  4. Delete Task
  5. Mark Task Complete
  6. Switch Scheduling Strategy
  7. Exit
──────────────────────────────────────
```

---

## OOP Concepts Used

| Concept            | Where applied                                                       |
|--------------------|---------------------------------------------------------------------|
| Abstraction        | `Task` — abstract class with abstract `getUrgencyScore()`          |
| Inheritance        | `OneTimeTask`, `RecurringTask`, `UrgentTask` extend `Task`         |
| Polymorphism       | Each subclass overrides `getUrgencyScore()`, sorted uniformly      |
| Exception Handling | Three custom exceptions with meaningful messages and fields        |
| Collections        | `ArrayList` + `PriorityQueue` logic inside `SchedulerManager`      |

## Design Patterns Used

| Pattern  | Implementation                                                      |
|----------|---------------------------------------------------------------------|
| Strategy | `SchedulingStrategy` interface + 3 concrete strategy classes        |
| Observer | `TaskObserver` interface + `DeadlineConflictNotifier` + `WorkloadMonitor` |
