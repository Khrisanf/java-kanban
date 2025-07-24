package ru.java.java_kanban.manager.task;

import ru.java.java_kanban.manager.history.HistoryManager;
import ru.java.java_kanban.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final Path file;

    public FileBackedTaskManager(HistoryManager historyManager, Path file) {
        super(historyManager);
        this.file = file;
        loadFromFile();
    }

    public static class ManagerSaveException extends RuntimeException {
        public ManagerSaveException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public void save() {
        List<String> lines = new ArrayList<>();
        lines.add("id,type,name,status,description,epic");

        for (Task task : getAllTasks()) {
            lines.add(toCsv(task));
        }

        for (Epic epic : getAllEpics()) {
            lines.add(toCsv(epic));
        }

        for (Subtask subtask : getAllSubtasks()) {
            lines.add(toCsv(subtask));
        }

        try {
            Files.write(file,
                    lines,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new ManagerSaveException("Error saving to file" + file, e);
        }
    }

    private String toCsv(Task task) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(task.getId()).append(",");
        stringBuilder.append(task.getType()).append(",");
        stringBuilder.append(task.getName()).append(",");
        stringBuilder.append(task.getStatus()).append(",");
        stringBuilder.append(task.getDescription()).append(",");

        if (task instanceof Subtask) {
            stringBuilder.append(((Subtask) task).getEpicId());
        }

        return stringBuilder.toString();
    }

    private Task fromCsv(String line) {
        String[] parts = line.split(",");

        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String name = parts[2];
        String status = parts[3];
        String description = parts[4];

        switch (type) {
            case "TASK":
                Task task = new Task(name,
                        description,
                        TaskStatus.valueOf(status));
                task.setId(id);
                return task;

            case "EPIC":
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(TaskStatus.valueOf(status));
                return epic;

            case "SUBTASK":
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

    private void loadFromFile() {
        if (!Files.exists(file)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {

            String line = reader.readLine();
            int maxId = 0;

            while ((line = reader.readLine()) != null) {
                Task task = fromCsv(line);

                if (task instanceof Epic) {
                    super.addEpic((Epic) task);
                } else if (task instanceof Subtask subtask) {
                    if (getEpicById(subtask.getEpicId()) != null) {
                        super.addSubtask(subtask);
                    } else {
                        System.out.println("⚠ Subtask " + subtask.getId()
                                + "has missed: there is no epic with id="
                                + subtask.getEpicId());
                    }
                } else {
                    super.addTask(task);
                }

                if (task.getId() > maxId) {
                    maxId = task.getId();
                }
            }

            save();
            System.out.println("File updated after download: " + file);


            setNextId(maxId + 1);
        } catch (IOException e) {
            throw new RuntimeException("⚠ Error loading from file: " + file, e);
        }
    }

    //ABOUT TASK
    @Override
    public Task addTask(Task task) {
        super.addTask(task);
        save();
        return task;
    }

    @Override
    public void deleteTaskById(Integer id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    //ABOUT EPICS
    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public Epic addEpic(Epic epic) {
        super.addEpic(epic);
        save();
        return epic;
    }

    @Override
    public void deleteEpicById(Integer id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void updateEpic(Epic updatedEpic) {
        super.updateEpic(updatedEpic);
        save();
    }

    //ABOUT SUBTASKS
    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
        return subtask;
    }

    @Override
    public void updateSubtask(Subtask updatedSubtask) {
        super.updateSubtask(updatedSubtask);
        save();
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        super.deleteSubtaskById(id);
        save();
    }
}
