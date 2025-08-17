package ru.java.java_kanban.manager.task;

import ru.java.java_kanban.manager.ManagerSaveException;
import ru.java.java_kanban.manager.history.HistoryManager;
import ru.java.java_kanban.model.Epic;
import ru.java.java_kanban.model.Subtask;
import ru.java.java_kanban.model.Task;
import ru.java.java_kanban.model.TaskType;
import ru.java.java_kanban.util.CsvConverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final Path file;

    public FileBackedTaskManager(HistoryManager historyManager, Path file) {
        super(historyManager);
        this.file = file;
        loadFromFile();
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        lines.add("id,type,name,status,description,startTime,endTime,durationMinutes,epic");

        for (Task task : getAllTasks()) {
            lines.add(CsvConverter.toCsvString(task));
        }

        for (Epic epic : getAllEpics()) {
            lines.add(CsvConverter.toCsvString(epic));
        }

        for (Subtask subtask : getAllSubtasks()) {
            lines.add(CsvConverter.toCsvString(subtask));
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

    private List<String> readTasksLines(BufferedReader reader) throws IOException {
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            lines.add(line);
        }
        return lines;
    }

    private int parseTaskIds(List<String> lines) {
        int maxId = 0;
        Map<Integer, Task> tasksToRestore = new HashMap<>();
        List<Subtask> subtasksToRestore = new ArrayList<>();

        for (String line : lines) {
            Task task = CsvConverter.fromCvsString(line);
            maxId = Math.max(maxId, task.getId());

            if (Objects.requireNonNull(task.getType()) == TaskType.SUBTASK) {
                subtasksToRestore.add((Subtask) task);
            } else {
                tasksToRestore.put(task.getId(), task);
            }
        }

        for (Task task : tasksToRestore.values()) {
            if (task.getType() == TaskType.TASK) {
                this.tasks.put(task.getId(), task);
            } else if (task.getType() == TaskType.EPIC) {
                Epic epic = (Epic) task;
                this.epics.put(epic.getId(), epic);
            } else {
                throw new ManagerSaveException("Invalid task type");
            }
        }


        for (Subtask subtask : subtasksToRestore) {
            this.subtasks.put(subtask.getId(), subtask);

            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.addSubtaskIds(subtask.getId());
                updateEpicStatus(epic.getId());
            } else {
                throw new BrokenTaskLinkException("⚠ Subtask " + subtask.getId()
                        + " has missed: there is no epic with id=" + subtask.getEpicId());
            }
        }
        return maxId;
    }

    private void parseHistory(BufferedReader reader) throws IOException {
        String line = reader.readLine();

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
    }

    private void loadFromFile() {
        if (!Files.exists(file)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            reader.readLine();
            List<String> taskLines = readTasksLines(reader);
            int maxId = parseTaskIds(taskLines);
            parseHistory(reader);
            setNextId(maxId + 1);
            System.out.println("File loaded successfully! " + file.getFileName());

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
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        super.deleteSubtaskById(id);
        save();
    }
}
