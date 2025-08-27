package ru.java.java_kanban.model;

import org.junit.jupiter.api.Test;
import ru.java.java_kanban.manager.history.InMemoryHistoryManager;
import ru.java.java_kanban.manager.task.InMemoryTaskManager;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {

    @Test
    public void equals_returnTrue_sameId() {
        Epic epic1 = new Epic("Epic 1", "desc");
        epic1.setId(1);
        Epic epic2 = new Epic("Epic 2", "different desc");
        epic2.setId(1);

        assertEquals(epic1, epic2);
    }

    @Test
    public void addSubtaskId_notAddIfEqualsEpicId() {
        Epic epic = new Epic("Epic", "desc");
        epic.setId(100);
        epic.addSubtaskIds(100);

        assertFalse(epic.getSubtaskIds().contains(100));
    }


    @Test
    public void addSubtaskId_addIdToList() {
        Epic epic = new Epic("Epic", "desc");
        epic.setId(1);
        epic.addSubtaskIds(42);

        assertTrue(epic.getSubtaskIds().contains(42));
    }

    @Test
    public void toString_containEpicNameAndId() {
        Epic epic = new Epic("Test Epic", "desc");
        epic.setId(5);

        String result = epic.toString();
        assertTrue(result.contains("Test Epic"));
        assertTrue(result.contains("5"));
    }

    // BOUNDARY VALUES
    @Test
    public void status_returnsNew_allSubtasksNew() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic = new Epic("E", "D");
        epic = manager.addEpic(epic);

        manager.addSubtask(new Subtask("S1", "D", TaskStatus.NEW, epic.getId()));
        manager.addSubtask(new Subtask("S2", "D", TaskStatus.NEW, epic.getId()));

        Epic stored = manager.getEpicById(epic.getId());
        assertEquals(TaskStatus.NEW, stored.getStatus());
    }

    @Test
    public void status_returnsDone_allSubtasksDone() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic = manager.addEpic(new Epic("E", "D"));

        manager.addSubtask(new Subtask("S1", "D", TaskStatus.DONE, epic.getId()));
        manager.addSubtask(new Subtask("S2", "D", TaskStatus.DONE, epic.getId()));

        Epic stored = manager.getEpicById(epic.getId());
        assertEquals(TaskStatus.DONE, stored.getStatus());
    }

    @Test
    public void status_returnsInProgress_mixOfNewAndDone() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic = manager.addEpic(new Epic("E", "D"));

        manager.addSubtask(new Subtask("S1", "D", TaskStatus.NEW, epic.getId()));
        manager.addSubtask(new Subtask("S2", "D", TaskStatus.DONE, epic.getId()));

        Epic stored = manager.getEpicById(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, stored.getStatus());
    }

    @Test
    public void status_returnsInProgress_anySubtaskInProgress() {
        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        Epic epic = manager.addEpic(new Epic("E", "D"));

        manager.addSubtask(new Subtask("S1", "D", TaskStatus.IN_PROGRESS, epic.getId()));
        manager.addSubtask(new Subtask("S2", "D", TaskStatus.NEW, epic.getId()));

        Epic stored = manager.getEpicById(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, stored.getStatus());
    }
}
