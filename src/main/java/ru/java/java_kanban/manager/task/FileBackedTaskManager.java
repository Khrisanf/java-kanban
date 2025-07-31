package ru.java.java_kanban.manager.task;

import ru.java.java_kanban.manager.ManagerSaveException;
import ru.java.java_kanban.manager.history.HistoryManager;
import ru.java.java_kanban.model.*;
import ru.java.java_kanban.util.CsvConverter;

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

    private void save() {
        List<String> lines = new ArrayList<>();
        lines.add("id,type,name,status,description,epic");

        for (Task task : getAllTasks()) {
            lines.add(CsvConverter.toString(task));
        }

        for (Epic epic : getAllEpics()) {
            lines.add(CsvConverter.toString(epic));
        }

        for (Subtask subtask : getAllSubtasks()) {
            lines.add(CsvConverter.toString(subtask));
        }

        try {
            Files.write(file,
                    lines,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new ManagerSaveException("Error saving to file" + file);
        }
    }



    private void loadFromFile() {
        if (!Files.exists(file)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {

            String line = reader.readLine();
            int maxId = 0;

            List<String> taskLines = new ArrayList<>();

            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                taskLines.add(line);
            }

            for (String l : taskLines) {
                Task task = CsvConverter.fromString(l);
                maxId = Math.max(maxId, task.getId());

                if (task instanceof Epic epic) {
                    restoreEpic(epic);
                } else if (!(task instanceof Subtask)) {
                    restoreTask(task);
                }
            }

            for (String l :  taskLines) {
                Task task = CsvConverter.fromString(l);
                if (task instanceof Subtask subtask) {
                    if (getEpicById(Subtask.getEpicId()) != null) {
                        restoreSubtask(subtask);
                    } else {
                        throw new BrokenTaskLinkException("⚠ Subtask " + subtask.getId()
                                + " has missed: there is no epic with id="
                                + subtask.getEpicId());
                    }
                }
            }

            if ((line = reader.readLine()) != null && !line.isBlank()) {
                String[] fields = line.split(",");
                for (String field : fields) {
                    int id = Integer.parseInt(field.trim());
                    Task task = getTaskById(id);
                    if (task != null) {
                        historyManager.add(task);
                    }
                }
            }

            setNextId(maxId + 1);
            System.out.printf("File loaded successfully! " + file.getFileName());
        } catch (IOException e) {
            throw new ManagerSaveException("⚠ Error loading from file: " + file);
        }
    }

    //ABOUT TASK
    @Override
    public Task addTask(Task task) {
        Task added = super.addTask(task);
        save();
        return added;
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
