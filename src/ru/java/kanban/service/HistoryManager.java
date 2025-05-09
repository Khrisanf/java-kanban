package ru.java.kanban.service;

import ru.java.kanban.model.Task;
import java.util.List;

public interface HistoryManager {
    void add(Task task);
    List<Task> getHistory();
}
