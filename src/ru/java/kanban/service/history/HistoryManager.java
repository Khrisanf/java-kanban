package ru.java.kanban.service.history;

import ru.java.kanban.model.Task;
import java.util.List;

public interface HistoryManager {
    void add(Task task);
    List<Task> getHistory();
    void remove(int id);
}
