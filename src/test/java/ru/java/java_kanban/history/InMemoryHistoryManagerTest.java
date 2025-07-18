package ru.java.java_kanban.history;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import ru.java.java_kanban.manager.history.HistoryManager;
import ru.java.java_kanban.manager.history.InMemoryHistoryManager;
import ru.java.java_kanban.model.Task;
import ru.java.java_kanban.model.TaskStatus;

import java.util.List;

public class InMemoryHistoryManagerTest {

    @Test
    public void add_moveTaskToEnd_ifAlreadyInHistory() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task1 = new Task("Task 1", "Desc", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Desc", TaskStatus.NEW);
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task1, history.get(1));
    }

    @Test
    public void remove_deleteTaskFromHistory_ifExists() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task1 = new Task("Task 1", "Desc", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Desc", TaskStatus.NEW);
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task2, history.get(0));
    }

    @Test
    public void remove_doNothing_ifHistoryEmpty() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        assertDoesNotThrow(() -> historyManager.remove(999));
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    public void getHistory_returnEmptyList_ifNoTasksViewed() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    public void add_setHeadAndTail_ifHistoryEmpty() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task("Task 1", "Desc", TaskStatus.NEW);
        task.setId(1);

        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }

    @Test
    public void remove_updateHead_ifFirstTaskRemoved() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task1 = new Task("Task 1", "Desc", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Desc", TaskStatus.NEW);
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task2, history.get(0));
    }

    @Test
    public void remove_updateTail_ifLastTaskRemoved() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task1 = new Task("Task 1", "Desc", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Desc", TaskStatus.NEW);
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));
    }

    @Test
    public void add_appearInHistory_ifNotNull() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW);
        task.setId(1);

        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }

    @Test
    public void remove_clearHistory_ifLastTaskRemoved() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task("Task 1", "Desc", TaskStatus.NEW);
        task.setId(1);

        historyManager.add(task);
        historyManager.remove(1);

        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    public void add_ignore_ifNull() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        historyManager.add(null);

        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }
}

