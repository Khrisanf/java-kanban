package ru.java.java_kanban.manager.history;

import ru.java.java_kanban.model.Task;
import java.util.List;
import java.util.Set;

public interface HistoryManager {
    void add(Task task);
    List<Task> getHistoryMap();
    void remove(int id);
}
