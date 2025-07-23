package ru.java.java_kanban.manager.task;

import ru.java.java_kanban.manager.history.HistoryManager;
import ru.java.java_kanban.model.Task;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final Path file;

    public FileBackedTaskManager(HistoryManager historyManager, Path file) {
        super(historyManager);
        this.file = file;
    }

    public void save() {
        List<String> lines = new ArrayList<>();
        lines.add("id,type,name,status,description,epic");

        for (Task task : getAllTasks()) {
            lines.add(toCsv(task));
        }
    }

    private String toCsv(Task task) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(task.getId()).append(",");
        stringBuilder.append(task.getType()).append(",");
        return "";
    }
}
