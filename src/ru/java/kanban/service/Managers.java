package ru.java.kanban.service;
//этот вообще имеет одно отношение к таскам и истории.
//предлагаю его оставить в общем пакете

import ru.java.kanban.service.history.HistoryManager;
import ru.java.kanban.service.history.InMemoryHistoryManager;
import ru.java.kanban.service.task.InMemoryTaskManager;
import ru.java.kanban.service.task.TaskManager;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(new InMemoryHistoryManager());
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();     //History
    }
}
