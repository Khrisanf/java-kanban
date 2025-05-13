package ru.java.kanban.main.manager;

import ru.java.kanban.main.manager.history.HistoryManager;
import ru.java.kanban.main.manager.history.InMemoryHistoryManager;
import ru.java.kanban.main.manager.task.InMemoryTaskManager;
import ru.java.kanban.main.manager.task.TaskManager;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(new InMemoryHistoryManager());
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();     //History
    }
}
