package ru.java.java_kanban.util;

import ru.java.java_kanban.model.*;

public final class CsvConverter {

    private CsvConverter() {
    }

    public static String toCsvString(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",");
        sb.append(task.getType()).append(",");
        sb.append(task.getName()).append(",");
        sb.append(task.getStatus()).append(",");
        sb.append(task.getDescription());

        return sb.toString();
    }

    public static String toCsvString(Subtask subtask) {
        String line = toCsvString((Task) subtask);
        String s = line + subtask.getEpicId();
        return s;
    }

    public static Task fromCvsString(String line) {
        String[] parts = line.split(",");

        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        String status = parts[3];
        String description = parts[4];

        switch (type) {
            case TASK:
                Task task = new Task(name,
                        description,
                        TaskStatus.valueOf(status)
                );
                task.setId(id);
                return task;

            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(TaskStatus.valueOf(status));
                return epic;

            case SUBTASK:
                int epicId = Integer.parseInt(parts[5]);
                Subtask subtask = new Subtask(name,
                        description,
                        TaskStatus.valueOf(status),
                        epicId);
                subtask.setId(id);
                return subtask;

            default:
                throw new IllegalArgumentException("Unknown task type: " + type);
        }
    }
}
