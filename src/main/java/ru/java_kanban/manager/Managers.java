package main.java.ru.java_kanban.manager;

import main.java.ru.java_kanban.manager.history.HistoryManager;
import main.java.ru.java_kanban.manager.history.InMemoryHistoryManager;
import main.java.ru.java_kanban.manager.task.InMemoryTaskManager;
import main.java.ru.java_kanban.manager.task.TaskManager;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(new InMemoryHistoryManager());
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();     //History
    }
}
