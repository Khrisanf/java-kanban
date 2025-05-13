package ru.java.kanban.main.manager.history;

import ru.java.kanban.main.model.Task;
import java.util.List;

public interface HistoryManager {
    void add(Task task);
    List<Task> getHistory();
    void remove(int id);
}
