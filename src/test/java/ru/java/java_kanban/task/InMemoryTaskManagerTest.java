package ru.java.java_kanban.task;

import ru.java.java_kanban.manager.history.InMemoryHistoryManager;
import ru.java.java_kanban.manager.task.InMemoryTaskManager;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager(new InMemoryHistoryManager());
    }
}
