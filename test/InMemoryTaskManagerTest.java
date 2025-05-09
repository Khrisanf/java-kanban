import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.java.kanban.model.*;
import ru.java.kanban.service.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {
    private TaskManager manager;
    private Task task;
    private Epic epic;
    private Subtask subtask;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();

        task = new Task("Test", "Description", TaskStatus.NEW);
        manager.addTask(task);

        epic = new Epic("Epic name", "Epic desc");
        manager.addEpic(epic);

        subtask = new Subtask("Sub", "desc", TaskStatus.NEW, epic.getId());
        manager.addSubtaskByEpic(subtask);
    }

    // TASK TESTS
    @Nested
    class TaskTest {
        @Test
        void addTask_shouldReturnAddedTask() {
            assertNotNull(task);
            assertEquals("Test", task.getName());
        }

        @Test
        void getTaskById_shouldReturnCorrectTask() {
            Task retrieved = manager.getTaskById(task.getId());
            assertEquals(task, retrieved);
        }

        @Test
        void getTaskById_shouldReturnNullForNonexistentId() {
            assertNull(manager.getTaskById(999));
        }

        @Test
        void updateTask_shouldChangeFields() {
            task.setName("Updated");
            task.setDescription("Updated desc");
            task.setStatus(TaskStatus.IN_PROGRESS);
            manager.updateTask(task);

            Task updated = manager.getTaskById(task.getId());
            assertEquals("Updated", updated.getName());
            assertEquals(TaskStatus.IN_PROGRESS, updated.getStatus());
        }

        @Test
        void updateTask_shouldNotAddNewIfIdNotExists() {
            Task unknown = new Task("Ghost", "not exist", TaskStatus.NEW);
            unknown.setId(999);
            manager.updateTask(unknown);

            assertNull(manager.getTaskById(999));
        }

        @Test
        void deleteTaskById_shouldRemoveTask() {
            manager.deleteTaskById(task.getId());
            assertNull(manager.getTaskById(task.getId()));
        }
        @Test
        void deleteTaskById_shouldNotThrowForNonexistentId() {
            assertDoesNotThrow(() -> manager.deleteTaskById(12345));
        }
    }

    // EPIC TESTS
    @Nested
    class EpicTest {
        @Test
        void shouldAddAndGetEpic() {
            Epic result = manager.getEpicById(epic.getId());
            assertEquals(epic, result);
        }

        @Test
        void getEpicById_shouldReturnNullForNonexistentId() {
            assertNull(manager.getEpicById(-1));
        }

        @Test
        void shouldUpdateEpicFields() {
            Epic updated = new Epic("New name", "New desc");
            updated.setId(epic.getId());
            manager.updateEpic(updated);

            Epic result = manager.getEpicById(epic.getId());
            assertEquals("New name", result.getName());
            assertEquals("New desc", result.getDescription());
        }

        @Test
        void epicStatus_shouldBeNewIfNoSubtasks() {
            Epic solo = manager.addEpic(new Epic("Alone", "No subs"));
            assertEquals(TaskStatus.NEW, manager.getEpicById(solo.getId()).getStatus());
        }


        @Test
        void shouldDeleteEpicAndItsSubtasks() {
            manager.deleteEpicById(epic.getId());

            assertNull(manager.getEpicById(epic.getId()));
            assertNull(manager.getSubtaskById(subtask.getId()));
        }

        @Test
        void shouldDeleteAllEpicsAndSubtasks() {
            manager.deleteAllEpics();

            assertTrue(manager.getAllEpics().isEmpty());
            assertTrue(manager.getAllSubtasks().isEmpty());
        }

        @Test
        void deleteEpicById_shouldNotThrowIfEpicMissing() {
            assertDoesNotThrow(() -> manager.deleteEpicById(1000));
        }
    }

    // SUBTASK TESTS
    @Nested
    class SubtaskTest {
        @Test
        void shouldAddAndGetSubtask() {
            Subtask result = manager.getSubtaskById(subtask.getId());
            assertEquals(subtask, result);
        }

        @Test
        void shouldReturnSubtasksOfEpic() {
            List<Subtask> subtasks = manager.getSubtaskOfEpic(epic.getId());

            assertEquals(1, subtasks.size());
            assertTrue(subtasks.contains(subtask));
        }

        @Test
        void shouldReturnEmptyListIfEpicHasNoSubtasks() {
            Epic emptyEpic = manager.addEpic(new Epic("Empty Epic", "No subtasks"));
            List<Subtask> subtasks = manager.getSubtaskOfEpic(emptyEpic.getId());
            assertTrue(subtasks.isEmpty());
        }

        @Test
        void getSubtaskById_shouldReturnNullForNonexistentId() {
            assertNull(manager.getSubtaskById(9999));
        }

        @Test
        void shouldUpdateSubtaskAndAffectEpicStatus() {
            subtask.setStatus(TaskStatus.DONE);
            manager.updateSubtask(subtask);

            Epic updatedEpic = manager.getEpicById(epic.getId());
            assertEquals(TaskStatus.DONE, updatedEpic.getStatus());
        }

        @Test
        void deleteSubtaskById_shouldUpdateEpic() {
            manager.deleteSubtaskById(subtask.getId());

            Epic updatedEpic = manager.getEpicById(epic.getId());
            assertTrue(updatedEpic.getSubtaskIds().isEmpty());
            assertEquals(TaskStatus.NEW, updatedEpic.getStatus());
        }

        @Test
        void deleteSubtaskById_shouldNotThrowIfMissing() {
            assertDoesNotThrow(() -> manager.deleteSubtaskById(-42));
        }

        @Test
        void deleteAllSubtasks_shouldClearFromEpics() {
            manager.deleteAllSubtasks();

            assertTrue(manager.getAllSubtasks().isEmpty());
            Epic updated = manager.getEpicById(epic.getId());
            assertTrue(updated.getSubtaskIds().isEmpty());
        }

        @Test
        void addSubtask_shouldReturnNullIfEpicNotExist() {
            Subtask sub = new Subtask("Orphan", "No parent", TaskStatus.NEW, 9999);
            Subtask result = manager.addSubtaskByEpic(sub);
            assertNull(result);
        }
    }

    // HISTORY TESTS
    @Nested
    class HistoryTest {
        @Test
        void getTaskById_shouldAddToHistory() {
            manager.getTaskById(task.getId());

            List<Task> history = manager.getHistory();
            assertEquals(1, history.size());
            assertEquals(task, history.getFirst());
        }
    }
}
