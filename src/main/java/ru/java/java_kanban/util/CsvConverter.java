package ru.java.java_kanban.util;

import ru.java.java_kanban.model.*;

import java.time.LocalDateTime;

public final class CsvConverter {

    private CsvConverter() {
    }

    private static String nullParseHelper(Object o) {
        return o == null ? "" : o.toString();
    }

    private static LocalDateTime nullParseHelper(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        if (s.isEmpty() || s.equalsIgnoreCase("null") ) {
            return null;
        }
        return LocalDateTime.parse(s);
    }

    private static Long minutesParseHelper(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        if (s.isEmpty() || s.equalsIgnoreCase("null") ) {
            return null;
        }
        return Long.parseLong(s);
    }

    public static String toCsvString(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",");
        sb.append(task.getType()).append(",");
        sb.append(task.getName()).append(",");
        sb.append(task.getStatus()).append(",");
        sb.append(task.getDescription()).append(",");
        sb.append(nullParseHelper(task.getStartTime())).append(",");
        sb.append(nullParseHelper(task.getEndTime())).append(",");
        sb.append(nullParseHelper(task.getDurationMinutes())).append(",");
        sb.append("");

        return sb.toString();
    }

    public static String toCsvString(Subtask subtask) {

        return toCsvString((Task) subtask)
                + subtask.getEpicId();
    }

    public static Task fromCvsString(String line) {
        String[] parts = line.split(",", -1);

        if (parts.length > 0 && "id".equalsIgnoreCase(parts[0])) {
            return null;
        }

        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        String status = parts[3];
        String description = parts[4];

        LocalDateTime start = nullParseHelper(parts[5]);
        LocalDateTime end   = nullParseHelper(parts[6]);
        Long minutes        = minutesParseHelper(parts[7]);

        switch (type) {
            case TASK:
                Task task = new Task(name,
                        description,
                        TaskStatus.valueOf(status)
                );
                task.setId(id);
                task.setStartTime(start);
                task.setDurationMinutes(minutes);
                return task;

            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(TaskStatus.valueOf(status));
                return epic;

            case SUBTASK:
                int epicId = Integer.parseInt(parts[8]);
                Subtask subtask = new Subtask(name,
                        description,
                        TaskStatus.valueOf(status),
                        epicId);
                subtask.setId(id);
                subtask.setStartTime(start);
                subtask.setDurationMinutes(minutes);
                return subtask;

            default:
                throw new IllegalArgumentException("Unknown task type: " + type);
        }
    }
}
