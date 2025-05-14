package ru.java_kanban.history;

import main.java.ru.java_kanban.manager.history.HistoryManager;
import main.java.ru.java_kanban.manager.history.InMemoryHistoryManager;
import main.java.ru.java_kanban.manager.task.TaskManager;
import main.java.ru.java_kanban.model.Task;
import main.java.ru.java_kanban.model.TaskStatus;
import org.junit.jupiter.api.Test;
import main.java.ru.java_kanban.manager.Managers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    @Test
    void add_appearInHistory_ifNotNull() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW);
        task.setId(1);

        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }

    @Test
    void add_ignore_ifNull() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        historyManager.add(null);

        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    void addMoreThanTenTasks_fIfO() {
        HistoryManager historyManager = new InMemoryHistoryManager();

        for (int i = 1; i <= 12; i++) {
            Task t = new Task("Task " + i, "Description", TaskStatus.NEW);
            t.setId(i);
            historyManager.add(t);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size());
        assertEquals("Task 3", history.get(0).getName());
        assertEquals("Task 12", history.get(9).getName());
    }

    @Test
    void getHistory_returnIndependentCopy() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW);
        task.setId(1);
        historyManager.add(task);

        List<Task> copy = historyManager.getHistory();
        copy.clear();

        List<Task> original = historyManager.getHistory();
        assertEquals(1, original.size());
    }

    @Test
    void add_storeImmutableSnapshot_notReference() {
        TaskManager manager = Managers.getDefault();
        Task task = new Task("Original", "Desc", TaskStatus.NEW);
        manager.addTask(task);
        Task originalCopy = task.copy();

        manager.getTaskById(task.getId());
        task.setStatus(TaskStatus.DONE);

        Task fromHistory = manager.getHistory().get(0);

        assertEquals(originalCopy.getStatus(), fromHistory.getStatus(),
                "History should contain a snapshot, not a live reference");
    }
}
