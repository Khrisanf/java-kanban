package ru.java.java_kanban.manager;

import ru.java.java_kanban.manager.history.*;
import ru.java.java_kanban.manager.task.*;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(new InMemoryHistoryManager());
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();     //History
    }
}
