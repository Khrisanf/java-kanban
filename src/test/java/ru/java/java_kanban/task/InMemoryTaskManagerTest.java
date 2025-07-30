package ru.java.java_kanban.task;


import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ru.java.java_kanban.manager.history.InMemoryHistoryManager;
import ru.java.java_kanban.manager.task.InMemoryTaskManager;
import ru.java.java_kanban.manager.task.TaskManager;
import ru.java.java_kanban.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {

    private final TaskManager manager;

    public InMemoryTaskManagerTest() {
        this.manager = new InMemoryTaskManager(new InMemoryHistoryManager());
    }

    // TASK TESTS
    @Nested
    class TaskTest {
        @Test
        void addTask_returnsAddedTask() {
            Task task = new Task("Test", "Description", TaskStatus.NEW, TaskType.TASK);
            Task added = manager.addTask(task);
            assertNotNull(added);
            assertEquals("Test", added.getName());
        }

        @Test
        void getTaskById_returnsCorrectTask() {
            Task task = manager.addTask(new Task("Test", "Desc", TaskStatus.NEW, TaskType.TASK));
            Task retrieved = manager.getTaskById(task.getId());
            assertEquals(task, retrieved);
        }

        @Test
        void getTaskById_returnsNull_ifIdNotFound() {
            assertNull(manager.getTaskById(999));
        }

        @Test
        void updateTask_changesFields() {
            Task task = manager.addTask(new Task("Test", "Desc", TaskStatus.NEW, TaskType.TASK));
            task.setName("Updated");
            task.setDescription("Updated desc");
            task.setStatus(TaskStatus.IN_PROGRESS);
            manager.updateTask(task);
            Task updated = manager.getTaskById(task.getId());
            assertEquals("Updated", updated.getName());
            assertEquals(TaskStatus.IN_PROGRESS, updated.getStatus());
        }

        @Test
        void updateTask_doesNotAdd_ifIdNotExists() {
            Task unknown = new Task("Ghost", "not exist", TaskStatus.NEW, TaskType.TASK);
            unknown.setId(999);
            manager.updateTask(unknown);
            assertNull(manager.getTaskById(999));
        }

        @Test
        void deleteTaskById_removesTask() {
            Task task = manager.addTask(new Task("Test", "Desc", TaskStatus.NEW, TaskType.TASK));
            manager.deleteTaskById(task.getId());
            assertNull(manager.getTaskById(task.getId()));
        }

        @Test
        void deleteTaskById_doesNotThrow_ifIdMissing() {
            assertDoesNotThrow(() -> manager.deleteTaskById(12345));
        }
    }

    // EPIC TESTS
    @Nested
    class EpicTest {
        @Test
        void addEpic_returnsAddedEpic() {
            Epic epic = new Epic("Epic name", "Epic desc");
            Epic added = manager.addEpic(epic);
            assertEquals(epic, manager.getEpicById(added.getId()));
        }

        @Test
        void getEpicById_returnsNull_ifIdNotFound() {
            assertNull(manager.getEpicById(-1));
        }

        @Test
        void updateEpic_changesFields() {
            Epic epic = manager.addEpic(new Epic("Old name", "Old desc"));
            Epic updated = new Epic("New name", "New desc");
            updated.setId(epic.getId());
            manager.updateEpic(updated);
            Epic result = manager.getEpicById(epic.getId());
            assertEquals("New name", result.getName());
            assertEquals("New desc", result.getDescription());
        }

        @Test
        void getEpicStatus_returnsNew_ifNoSubtasks() {
            Epic solo = manager.addEpic(new Epic("Alone", "No subs"));
            assertEquals(TaskStatus.NEW, manager.getEpicById(solo.getId()).getStatus());
        }

        @Test
        void deleteEpicById_removesEpicAndItsSubtasks() {
            Epic epic = manager.addEpic(new Epic("Epic", "With subtask"));
            Subtask subtask = manager.addSubtask(new Subtask("S", "D", TaskStatus.NEW, epic.getId()));
            manager.deleteEpicById(epic.getId());
            assertNull(manager.getEpicById(epic.getId()));
            assertNull(manager.getSubtaskById(subtask.getId()));
        }

        @Test
        void deleteAllEpics_removesAllEpicsAndSubtasks() {
            Epic epic = manager.addEpic(new Epic("Epic", "Multiple"));
            manager.addSubtask(new Subtask("S1", "D1", TaskStatus.NEW, epic.getId()));
            manager.addSubtask(new Subtask("S2", "D2", TaskStatus.NEW, epic.getId()));
            manager.deleteAllEpics();
            assertTrue(manager.getAllEpics().isEmpty());
            assertTrue(manager.getAllSubtasks().isEmpty());
        }

        @Test
        void deleteEpicById_doesNotThrow_ifIdMissing() {
            assertDoesNotThrow(() -> manager.deleteEpicById(1000));
        }

        @Test
        void setEpicId_manuallyBreaksEpicConsistency_ifChangedDirectly() {
            Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
            Subtask subtask = manager.addSubtask(new Subtask("Sub", "D", TaskStatus.NEW, epic.getId()));

            // Меняем epicId напрямую
            subtask.setEpicId(999);
            manager.updateSubtask(subtask);

            Epic updated = manager.getEpicById(epic.getId());
            assertTrue(updated.getSubtaskIds().contains(subtask.getId()),
                    "Manager does not check if epicId is correct when updating a subtask");
        }

    }

    //SUBTASK TESTS
    @Nested
    class SubtaskTest {
        @Test
        void addSubtask_returnsAddedSubtask() {
            Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
            Subtask subtask = new Subtask("Sub", "D", TaskStatus.NEW, epic.getId());
            Subtask added = manager.addSubtask(subtask);
            assertNotNull(added);
            assertEquals(subtask.getName(), added.getName());
        }

        @Test
        void getSubtaskById_returnsCorrectSubtask() {
            Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
            Subtask subtask = manager.addSubtask(new Subtask("Sub", "D", TaskStatus.NEW, epic.getId()));
            Subtask result = manager.getSubtaskById(subtask.getId());
            assertEquals(subtask, result);
        }

        @Test
        void getSubtaskById_returnsNull_ifIdMissing() {
            assertNull(manager.getSubtaskById(999));
        }

        @Test
        void getSubtaskOfEpic_returnsSubtasks_ifExist() {
            Epic epic = manager.addEpic(new Epic("Epic", "With subtasks"));
            Subtask subtask = manager.addSubtask(new Subtask("S", "D", TaskStatus.NEW, epic.getId()));
            List<Subtask> subtasks = manager.getSubtasksOfEpic(epic.getId());
            assertEquals(1, subtasks.size());
            assertTrue(subtasks.contains(subtask));
        }

        @Test
        void getSubtaskOfEpic_returnsEmpty_ifNoneExist() {
            Epic epic = manager.addEpic(new Epic("Empty", "No subtasks"));
            List<Subtask> subtasks = manager.getSubtasksOfEpic(epic.getId());
            assertTrue(subtasks.isEmpty());
        }

        @Test
        void updateSubtask_changesEpicStatus_ifStatusChanged() {
            Epic epic = manager.addEpic(new Epic("Epic", "With subtask"));
            Subtask subtask = manager.addSubtask(new Subtask("S", "D", TaskStatus.NEW, epic.getId()));
            subtask.setStatus(TaskStatus.DONE);
            manager.updateSubtask(subtask);
            Epic updated = manager.getEpicById(epic.getId());
            assertEquals(TaskStatus.DONE, updated.getStatus());
        }

        @Test
        void deleteSubtaskById_updatesEpicStatusAndList() {
            Epic epic = manager.addEpic(new Epic("Epic", "With subtask"));
            Subtask subtask = manager.addSubtask(new Subtask("S", "D", TaskStatus.NEW, epic.getId()));
            manager.deleteSubtaskById(subtask.getId());
            Epic updated = manager.getEpicById(epic.getId());
            assertTrue(updated.getSubtaskIds().isEmpty());
            assertEquals(TaskStatus.NEW, updated.getStatus());
        }

        @Test
        void deleteAllSubtasks_removesAllAndCleansEpics() {
            Epic epic = manager.addEpic(new Epic("Epic", "With subtasks"));
            manager.addSubtask(new Subtask("S1", "D1", TaskStatus.NEW, epic.getId()));
            manager.addSubtask(new Subtask("S2", "D2", TaskStatus.NEW, epic.getId()));
            manager.deleteAllSubtasks();
            assertTrue(manager.getAllSubtasks().isEmpty());
            Epic updated = manager.getEpicById(epic.getId());
            assertTrue(updated.getSubtaskIds().isEmpty());
        }

        @Test
        void addSubtask_returnsNull_ifEpicNotFound() {
            Subtask orphan = new Subtask("Orphan", "Lost", TaskStatus.NEW, 999);
            Subtask result = manager.addSubtask(orphan);
            assertNull(result);
        }
    }

    //HISTORY TESTS
    @Nested
    class HistoryTest {
        @Test
        void getTaskById_addsToHistory() {
            Task task = manager.addTask(new Task("T", "H", TaskStatus.NEW, TaskType.TASK));
            manager.getTaskById(task.getId());
            List<Task> history = manager.getHistory();
            assertEquals(1, history.size());
        }
    }
}

