package main.java.ru.java_kanban.manager.history;

import main.java.ru.java_kanban.model.Task;

import java.util.List;
import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (task == null) return;
        history.add(task.copy());
        if (history.size() > 10) {
            history.remove(0);
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }

    @Override
    public void remove(int id) {
        history.removeIf(task -> task.getId().equals(id));
    }
}
