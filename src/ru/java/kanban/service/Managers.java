package ru.java.kanban.service;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();        //Tasks
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();     //History
    }
}
