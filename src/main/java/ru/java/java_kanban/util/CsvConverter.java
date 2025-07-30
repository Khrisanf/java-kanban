package ru.java.java_kanban.util;

import ru.java.java_kanban.model.*;

public final class CsvConverter {

    private CsvConverter() {}

    public static String toString(Task task) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
                .append(task.getId()).append(",")
                .append(task.getType()).append(",")
                .append(task.getName()).append(",")
                .append(task.getStatus()).append(",")
                .append(task.getDescription()).append(",");

        stringBuilder.append(task.toCsvString());

        return stringBuilder.toString();
    }

    public static Task fromString(String line) {
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
                        TaskStatus.valueOf(status),
                        TaskType.TASK);
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

    public static String getHeader() {
        return "id,type,name,status,description,epic";
    }

}
