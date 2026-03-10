package com.taskscheduler.persistence;

import com.taskscheduler.model.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all file I/O for the Smart Task Scheduler.
 *
 * Saves and loads tasks using a lightweight hand-written JSON format
 * (no external library required — pure Java).
 *
 * File location: data/tasks.json (relative to project root).
 *
 * JSON structure per task:
 * {
 *   "id": 1,
 *   "type": "ONE_TIME",
 *   "title": "...",
 *   "description": "...",
 *   "priority": 3,
 *   "deadline": "2025-12-01",
 *   "estimatedHours": 4,
 *   "completed": false,
 *   "intervalDays": 0,         // RECURRING only
 *   "occurrencesLeft": 0,      // RECURRING only
 *   "urgencyMultiplier": 1.0,  // URGENT only
 *   "escalationReason": ""     // URGENT only
 * }
 */
public class TaskFileHandler {

    private final String filePath;

    public TaskFileHandler(String filePath) {
        this.filePath = filePath;
    }

    // ── Save ──────────────────────────────────────────────────────

    public void saveTasks(List<Task> tasks) throws IOException {
        StringBuilder sb = new StringBuilder("[\n");

        for (int i = 0; i < tasks.size(); i++) {
            sb.append(taskToJson(tasks.get(i)));
            if (i < tasks.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("]");
        Files.writeString(Path.of(filePath), sb.toString());
    }

    // ── Load ──────────────────────────────────────────────────────

    public List<Task> loadTasks() throws IOException {
        List<Task> tasks = new ArrayList<>();
        Path path = Path.of(filePath);

        if (!Files.exists(path) || Files.size(path) == 0) return tasks;

        String content = Files.readString(path).trim();
        if (content.equals("[]") || content.isEmpty()) return tasks;

        // Strip outer brackets and split by "}, {" boundaries
        content = content.substring(1, content.length() - 1).trim();
        String[] entries = content.split("(?<=\\}),\\s*(?=\\{)");

        for (String entry : entries) {
            try {
                Task task = jsonToTask(entry.trim());
                if (task != null) tasks.add(task);
            } catch (Exception e) {
                System.err.println("Warning: Skipped malformed task entry → " + e.getMessage());
            }
        }

        return tasks;
    }

    // ── Serialization Helpers ─────────────────────────────────────

    private String taskToJson(Task t) {
        StringBuilder sb = new StringBuilder("  {\n");
        sb.append(kv("id",             String.valueOf(t.getId())));
        sb.append(kv("type",           t.getTaskType()));
        sb.append(kv("title",          escape(t.getTitle())));
        sb.append(kv("description",    escape(t.getDescription() == null ? "" : t.getDescription())));
        sb.append(kv("priority",       String.valueOf(t.getPriority())));
        sb.append(kv("deadline",       t.getDeadline().toString()));
        sb.append(kv("estimatedHours", String.valueOf(t.getEstimatedHours())));
        sb.append(kv("completed",      String.valueOf(t.isCompleted())));

        if (t instanceof RecurringTask rt) {
            sb.append(kv("intervalDays",    String.valueOf(rt.getIntervalDays())));
            sb.append(kv("occurrencesLeft", String.valueOf(rt.getOccurrencesLeft())));
        } else {
            sb.append(kv("intervalDays",    "0"));
            sb.append(kv("occurrencesLeft", "0"));
        }

        if (t instanceof UrgentTask ut) {
            sb.append(kv("urgencyMultiplier", String.valueOf(ut.getUrgencyMultiplier())));
            sb.append(kv("escalationReason",  escape(ut.getEscalationReason())));
        } else {
            sb.append(kv("urgencyMultiplier", "1.0"));
            sb.append(kv("escalationReason",  ""));
        }

        // Remove trailing comma from last field
        int lastComma = sb.lastIndexOf(",");
        if (lastComma != -1) sb.deleteCharAt(lastComma);

        sb.append("  }");
        return sb.toString();
    }

    private Task jsonToTask(String json) {
        int    id               = Integer.parseInt(extractValue(json, "id"));
        String type             = extractValue(json, "type");
        String title            = extractValue(json, "title");
        String description      = extractValue(json, "description");
        int    priority         = Integer.parseInt(extractValue(json, "priority"));
        LocalDate deadline      = LocalDate.parse(extractValue(json, "deadline"));
        int    estimatedHours   = Integer.parseInt(extractValue(json, "estimatedHours"));
        boolean completed       = Boolean.parseBoolean(extractValue(json, "completed"));
        int    intervalDays     = Integer.parseInt(extractValue(json, "intervalDays"));
        int    occurrencesLeft  = Integer.parseInt(extractValue(json, "occurrencesLeft"));
        double urgencyMult      = Double.parseDouble(extractValue(json, "urgencyMultiplier"));
        String escalationReason = extractValue(json, "escalationReason");

        return switch (type) {
            case "RECURRING" -> new RecurringTask(id, title, description, priority,
                                    deadline, estimatedHours, completed,
                                    intervalDays, occurrencesLeft);
            case "URGENT"    -> new UrgentTask(id, title, description, priority,
                                    deadline, estimatedHours, completed,
                                    urgencyMult, escalationReason);
            default          -> new OneTimeTask(id, title, description, priority,
                                    deadline, estimatedHours, completed);
        };
    }

    /** Extracts a value for a given key from a simple flat JSON string. */
    private String extractValue(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx == -1) return "";

        int colonIdx = json.indexOf(":", keyIdx);
        int start    = colonIdx + 1;

        // Skip whitespace
        while (start < json.length() && json.charAt(start) == ' ') start++;

        if (json.charAt(start) == '"') {
            // String value
            int end = json.indexOf("\"", start + 1);
            return json.substring(start + 1, end);
        } else {
            // Numeric / boolean value
            int end = start;
            while (end < json.length() && ",\n}".indexOf(json.charAt(end)) == -1) end++;
            return json.substring(start, end).trim();
        }
    }

    private String kv(String key, String value) {
        boolean isNumericOrBool = value.matches("-?\\d+(\\.\\d+)?|true|false");
        if (isNumericOrBool)
            return String.format("    \"%s\": %s,\n", key, value);
        else
            return String.format("    \"%s\": \"%s\",\n", key, value);
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
