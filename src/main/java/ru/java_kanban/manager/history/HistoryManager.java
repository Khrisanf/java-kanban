package main.java.ru.java_kanban.manager.history;

import main.java.ru.java_kanban.model.Task;
import java.util.List;

public interface HistoryManager {
    void add(Task task);
    List<Task> getHistory();
    void remove(int id);
}
