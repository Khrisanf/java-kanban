package ru.java.kanban.service.history;

import org.junit.jupiter.api.Test;
import ru.java.kanban.model.*;
import ru.java.kanban.service.*;
import ru.java.kanban.service.task.TaskManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    @Test
    void addTask_shouldAppearInHistory_ifNotNull() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW);
        task.setId(1);

        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }

    @Test
    void addTask_shouldIgnore_ifNull() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        historyManager.add(null);

        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    void addMoreThanTenTasks_shouldRemoveOldest_firstInFirstOut() {
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
    void getHistory_shouldReturnIndependentCopy() {
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
    void addTask_shouldStoreImmutableSnapshot_notReference() {
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
