package ru.java.java_kanban.history;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import ru.java.java_kanban.manager.history.HistoryManager;
import ru.java.java_kanban.manager.history.InMemoryHistoryManager;
import ru.java.java_kanban.model.Task;
import ru.java.java_kanban.model.TaskStatus;

import java.util.List;

public class InMemoryHistoryManagerTest {

    /** helper */
    private Task task(int id, String name) {
        Task t = new Task(name, "Desc", TaskStatus.NEW);
        t.setId(id);
        return t;
    }

    @Nested
    class EmptyAndNullCases {

        @Test
        public void getHistory_returnsEmpty_ifNoTasks() {
            HistoryManager historyManager = new InMemoryHistoryManager();
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
            Task t = task(1, "Task 1");

            historyManager.add(t);

            List<Task> history = historyManager.getHistory();
            assertEquals(1, history.size());
            assertEquals(t, history.getFirst());
        }

        @Test
        public void add_ignore_ifNull() {
            HistoryManager historyManager = new InMemoryHistoryManager();
            assertDoesNotThrow(() -> historyManager.add(null));
            assertTrue(historyManager.getHistory().isEmpty());
        }

        @Test
        public void remove_doNothing_ifHistoryEmpty() {
            HistoryManager historyManager = new InMemoryHistoryManager();
            assertDoesNotThrow(() -> historyManager.remove(999));
            assertTrue(historyManager.getHistory().isEmpty());
        }

        @Test
        public void remove_ignore_ifIdNotExists() {
            HistoryManager hm = new InMemoryHistoryManager();
            hm.add(task(1, "A"));

            assertDoesNotThrow(() -> hm.remove(999));
            assertEquals(1, hm.getHistory().size());
        }
    }

    @Nested
    class DuplicatesBehavior {

        @Test
        public void add_moveTaskToEnd_ifAlreadyInHistory() {
            HistoryManager historyManager = new InMemoryHistoryManager();
            Task task1 = task(1, "Task 1");
            Task task2 = task(2, "Task 2");

            historyManager.add(task1);
            historyManager.add(task2);
            historyManager.add(task1);

            List<Task> history = historyManager.getHistory();
            assertEquals(2, history.size());
            assertEquals(task2, history.get(0));
            assertEquals(task1, history.get(1));
        }

        @Test
        public void add_sameTaskManyTimes_keepsSingleInstance() {
            HistoryManager hm = new InMemoryHistoryManager();
            Task a = task(1, "A");

            hm.add(a);
            hm.add(a);
            hm.add(a);

            List<Task> history = hm.getHistory();
            assertEquals(1, history.size());
            assertEquals(a, history.getFirst());
        }

        @Test
        public void getHistory_returnsTasks_inAccessOrder() {
            HistoryManager hm = new InMemoryHistoryManager();
            Task a = task(1, "A");
            Task b = task(2, "B");

            hm.add(a);
            hm.add(b);
            hm.add(a);

            List<Task> history = hm.getHistory();
            assertEquals(2, history.size());
            assertEquals(b, history.get(0));
            assertEquals(a, history.get(1));
        }
    }

    @Nested
    class RemovalPositions {

        @Test
        public void remove_deleteTaskFromHistory_ifExists() {
            HistoryManager historyManager = new InMemoryHistoryManager();
            Task task1 = task(1, "Task 1");
            Task task2 = task(2, "Task 2");

            historyManager.add(task1);
            historyManager.add(task2);
            historyManager.remove(1);

            List<Task> history = historyManager.getHistory();
            assertEquals(1, history.size());
            assertEquals(task2, history.getFirst());
        }

        @Test
        public void remove_begin_removesHead() {
            HistoryManager hm = new InMemoryHistoryManager();
            Task a = task(1, "A");
            Task b = task(2, "B");
            Task c = task(3, "C");
            hm.add(a); hm.add(b); hm.add(c);

            hm.remove(1);

            assertEquals(List.of(b, c), hm.getHistory());
        }

        @Test
        public void remove_middle_removesMiddleNode() {
            HistoryManager hm = new InMemoryHistoryManager();
            Task a = task(1, "A");
            Task b = task(2, "B");
            Task c = task(3, "C");
            hm.add(a); hm.add(b); hm.add(c);

            hm.remove(2);

            assertEquals(List.of(a, c), hm.getHistory());
        }

        @Test
        public void remove_end_removesTail() {
            HistoryManager hm = new InMemoryHistoryManager();
            Task a = task(1, "A");
            Task b = task(2, "B");
            Task c = task(3, "C");
            hm.add(a); hm.add(b); hm.add(c);

            hm.remove(3);

            assertEquals(List.of(a, b), hm.getHistory());
        }

        @Test
        public void remove_updateHead_ifFirstTaskRemoved() {
            HistoryManager historyManager = new InMemoryHistoryManager();
            Task task1 = task(1, "Task 1");
            Task task2 = task(2, "Task 2");

            historyManager.add(task1);
            historyManager.add(task2);
            historyManager.remove(1);

            List<Task> history = historyManager.getHistory();
            assertEquals(1, history.size());
            assertEquals(task2, history.getFirst());
        }

        @Test
        public void remove_updateTail_ifLastTaskRemoved() {
            HistoryManager historyManager = new InMemoryHistoryManager();
            Task task1 = task(1, "Task 1");
            Task task2 = task(2, "Task 2");

            historyManager.add(task1);
            historyManager.add(task2);
            historyManager.remove(2);

            List<Task> history = historyManager.getHistory();
            assertEquals(1, history.size());
            assertEquals(task1, history.getFirst());
        }

        @Test
        public void remove_onlyItem_makesHistoryEmpty() {
            HistoryManager hm = new InMemoryHistoryManager();
            Task a = task(1, "A");
            hm.add(a);

            assertDoesNotThrow(() -> hm.remove(1));
            assertTrue(hm.getHistory().isEmpty());
        }
    }
}
