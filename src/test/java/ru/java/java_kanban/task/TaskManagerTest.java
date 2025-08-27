package ru.java.java_kanban.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.java.java_kanban.manager.task.TaskManager;
import ru.java.java_kanban.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    /* ==== helpers ==== */
    protected Task timedTask(int day, int hour, int min, int durMin) {
        Task t = new Task("T-" + day + "-" + hour, "D", TaskStatus.NEW);
        t.setStartTime(LocalDateTime.of(2025, 1, day, hour, min));
        t.setDuration(Duration.ofMinutes(durMin));
        return t;
    }

    protected Subtask timedSub(int epicId, int day, int hour, int min, int durMin) {
        Subtask s = new Subtask("S-" + day + "-" + hour, "D", TaskStatus.NEW, epicId);
        s.setStartTime(LocalDateTime.of(2025, 1, day, hour, min));
        s.setDuration(Duration.ofMinutes(durMin));
        return s;
    }

    /* ===================== TASKS ===================== */
    @Nested
    class TasksCRUD {

        @Test
        public void getAllTasks_returnsEmpty_ifNoTasks() {
            assertTrue(manager.getAllTasks().isEmpty());
        }

        @Test
        public void addTask_assignsId_andStoresTask() {
            Task t = manager.addTask(new Task("A", "B", TaskStatus.NEW));
            assertNotNull(t.getId());
            assertEquals(t, manager.getTaskById(t.getId()));
        }

        @Test
        public void getTaskById_returnsTask_andAddsToHistory() {
            Task t = manager.addTask(new Task("A", "B", TaskStatus.NEW));
            assertEquals(t, manager.getTaskById(t.getId()));
            List<Task> history = manager.getHistory();
            assertEquals(1, history.size());
            assertEquals(t, history.getFirst());
        }

        @Test
        public void updateTask_updatesFields_ifExists() {
            Task t = manager.addTask(new Task("Old", "Desc", TaskStatus.NEW));
            t.setName("New");
            t.setDescription("Updated");
            manager.updateTask(t);

            Task got = manager.getTaskById(t.getId());
            assertEquals("New", got.getName());
            assertEquals("Updated", got.getDescription());
        }

        @Test
        public void deleteTaskById_removesTask_andHistory() {
            Task t = manager.addTask(new Task("A", "B", TaskStatus.NEW));
            manager.getTaskById(t.getId());
            manager.deleteTaskById(t.getId());

            assertNull(manager.getTaskById(t.getId()));
            assertTrue(manager.getHistory().isEmpty());
        }

        @Test
        public void deleteAllTasks_clearsTasks_andPriorityIndex() {
            manager.addTask(timedTask(1, 9, 0, 30));
            manager.addTask(timedTask(1, 10, 0, 30));
            manager.getAllTasks().forEach(task -> manager.getTaskById(task.getId()));

            manager.deleteAllTasks();

            assertTrue(manager.getAllTasks().isEmpty());
            assertTrue(manager.getPrioritizedTasks().isEmpty());
            assertTrue(manager.getHistory().isEmpty());
        }
    }

    /* ===================== EPICS ===================== */
    @Nested
    class EpicsCRUD {

        @Test
        public void getAllEpics_returnsEmpty_ifNoEpics() {
            assertTrue(manager.getAllEpics().isEmpty());
        }

        @Test
        public void addEpic_assignsId_andStoresEpic() {
            Epic e = manager.addEpic(new Epic("E", "D"));
            assertNotNull(e.getId());
            assertEquals(e, manager.getEpicById(e.getId()));
        }

        @Test
        public void getEpicById_returnsEpic_andAddsToHistory() {
            Epic e = manager.addEpic(new Epic("E", "D"));
            Epic got = manager.getEpicById(e.getId());
            assertEquals(e, got);
            assertEquals(1, manager.getHistory().size());
            assertEquals(e, manager.getHistory().getFirst());
        }

        @Test
        public void updateEpic_updatesNameAndDescription() {
            Epic e = manager.addEpic(new Epic("Old", "Desc"));
            e.setName("New");
            e.setDescription("Updated");
            manager.updateEpic(e);

            Epic got = manager.getEpicById(e.getId());
            assertEquals("New", got.getName());
            assertEquals("Updated", got.getDescription());
        }

        @Test
        public void deleteEpicById_removesEpic_andItsSubtasks() {
            Epic e = manager.addEpic(new Epic("E", "D"));
            Subtask s1 = manager.addSubtask(new Subtask("S1", "D", TaskStatus.NEW, e.getId()));
            Subtask s2 = manager.addSubtask(new Subtask("S2", "D", TaskStatus.NEW, e.getId()));

            manager.getEpicById(e.getId());
            manager.getSubtaskById(s1.getId());
            manager.deleteEpicById(e.getId());

            assertNull(manager.getEpicById(e.getId()));
            assertNull(manager.getSubtaskById(s1.getId()));
            assertNull(manager.getSubtaskById(s2.getId()));
            assertTrue(manager.getHistory().isEmpty());
        }

        @Test
        public void deleteAllEpics_clearsEpics_andSubtasks() {
            Epic e1 = manager.addEpic(new Epic("E1", "D"));
            Epic e2 = manager.addEpic(new Epic("E2", "D"));
            manager.addSubtask(new Subtask("S", "D", TaskStatus.NEW, e1.getId()));

            manager.deleteAllEpics();

            assertTrue(manager.getAllEpics().isEmpty());
            assertTrue(manager.getAllSubtasks().isEmpty());
        }
    }

    /* ===================== SUBTASKS ===================== */
    @Nested
    class SubtasksCRUD {

        @Test
        public void getAllSubtasks_returnsEmpty_ifNoSubtasks() {
            assertTrue(manager.getAllSubtasks().isEmpty());
        }

        @Test
        public void addSubtask_linksToEpic_andAppearsInEpicList() {
            Epic e = manager.addEpic(new Epic("E", "D"));
            Subtask s = manager.addSubtask(new Subtask("S1", "D", TaskStatus.NEW, e.getId()));

            Subtask got = manager.getSubtaskById(s.getId());
            assertEquals(e.getId(), got.getEpicId());

            List<Subtask> forEpic = manager.getSubtasksOfEpic(e.getId());
            assertEquals(1, forEpic.size());
            assertEquals(s, forEpic.getFirst());
        }

        @Test
        public void getSubtaskById_returnsSubtask_andAddsToHistory() {
            Epic e = manager.addEpic(new Epic("E", "D"));
            Subtask s = manager.addSubtask(new Subtask("S1", "D", TaskStatus.NEW, e.getId()));

            assertEquals(s, manager.getSubtaskById(s.getId()));
            assertEquals(1, manager.getHistory().size());
            assertEquals(s, manager.getHistory().getFirst());
        }

        @Test
        public void updateSubtask_updatesFields_andEpicStatusRecalculated() {
            Epic e = manager.addEpic(new Epic("E", "D"));
            Subtask s1 = manager.addSubtask(new Subtask("S1", "D", TaskStatus.NEW, e.getId()));
            manager.addSubtask(new Subtask("S2", "D", TaskStatus.NEW, e.getId()));

            s1.setStatus(TaskStatus.DONE);
            manager.updateSubtask(s1);

            Epic stored = manager.getEpicById(e.getId());
            assertEquals(TaskStatus.IN_PROGRESS, stored.getStatus());
            assertEquals(TaskStatus.DONE, manager.getSubtaskById(s1.getId()).getStatus());
        }

        @Test
        public void deleteSubtaskById_removesSubtask_andUpdatesEpic() {
            Epic e = manager.addEpic(new Epic("E", "D"));
            Subtask s1 = manager.addSubtask(new Subtask("S1", "D", TaskStatus.DONE, e.getId()));

            manager.deleteSubtaskById(s1.getId());

            assertNull(manager.getSubtaskById(s1.getId()));
            assertTrue(manager.getSubtasksOfEpic(e.getId()).isEmpty());

            Epic stored = manager.getEpicById(e.getId());
            assertNotEquals(TaskStatus.DONE, stored.getStatus());
        }

        @Test
        public void deleteAllSubtasks_clearsSubtasks_andEpicsSubtaskIds() {
            Epic e = manager.addEpic(new Epic("E", "D"));
            manager.addSubtask(new Subtask("S1", "D", TaskStatus.NEW, e.getId()));
            manager.addSubtask(new Subtask("S2", "D", TaskStatus.NEW, e.getId()));

            manager.deleteAllSubtasks();

            assertTrue(manager.getAllSubtasks().isEmpty());
            assertTrue(manager.getSubtasksOfEpic(e.getId()).isEmpty());
        }

        @Test
        public void getSubtasksOfEpic_returnsSubtasks_forExistingEpic() {
            Epic e = manager.addEpic(new Epic("E", "D"));
            Subtask s1 = manager.addSubtask(new Subtask("S1", "D", TaskStatus.NEW, e.getId()));
            Subtask s2 = manager.addSubtask(new Subtask("S2", "D", TaskStatus.NEW, e.getId()));

            List<Subtask> list = manager.getSubtasksOfEpic(e.getId());
            assertEquals(2, list.size());
            assertTrue(list.containsAll(List.of(s1, s2)));
        }
    }

    /* ===================== HISTORY ===================== */
    @Nested
    class HistoryGroup {

        @Test
        public void getHistory_returnsTasks_inAccessOrder() {
            Task t1 = manager.addTask(new Task("A", "D", TaskStatus.NEW));
            Task t2 = manager.addTask(new Task("B", "D", TaskStatus.NEW));

            manager.getTaskById(t1.getId());
            manager.getTaskById(t2.getId());
            manager.getTaskById(t1.getId());

            List<Task> history = manager.getHistory();
            assertEquals(2, history.size());
            assertEquals(t2, history.get(0));
            assertEquals(t1, history.get(1));
        }
    }

    /* ===================== PRIORITY & INTERVALS ===================== */
    @Nested
    class PriorityAndIntervals {

        @Test
        public void getPrioritizedTasks_returnsSortedByStartTime() {
            Task t2 = manager.addTask(timedTask(1, 9, 0, 30));
            Task t1 = manager.addTask(timedTask(1, 8, 0, 30));
            Task t3 = manager.addTask(timedTask(1, 12, 0, 30));

            List<Task> ordered = manager.getPrioritizedTasks();
            assertEquals(List.of(t1, t2, t3), ordered);
        }

        @Test
        public void addTask_throws_ifOverlapsExistingTask() {
            manager.addTask(timedTask(1, 10, 0, 60));
            Task overlapping = timedTask(1, 10, 30, 30);

            assertThrows(IllegalArgumentException.class, () -> manager.addTask(overlapping));
        }

        @Test
        public void nonOverlappingBackToBack_ok() {
            Task a = timedTask(1, 9, 0, 60);
            Task b = timedTask(1, 10, 0, 60);

            assertDoesNotThrow(() -> {
                manager.addTask(a);
                manager.addTask(b);
            });

            assertEquals(List.of(a, b), manager.getPrioritizedTasks());
        }

        @Test
        public void addSubtask_throws_ifOverlapsExistingTask() {
            Epic e = manager.addEpic(new Epic("E", "D"));
            Task t = manager.addTask(timedTask(1, 10, 0, 60));

            Subtask sOverlap = timedSub(e.getId(), 1, 10, 30, 15);

            assertThrows(IllegalArgumentException.class, () -> manager.addSubtask(sOverlap));
        }
    }
}
