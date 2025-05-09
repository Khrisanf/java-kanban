import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.java.kanban.model.*;
import ru.java.kanban.service.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;
    private Task task;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        task = new Task("Test Task", "Test Description", TaskStatus.NEW);
        task.setId(1);
    }

    @Test
    void shouldAddTaskToHistory() {
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.getFirst());
    }

    @Test
    void shouldNotAddNullToHistory() {
        historyManager.add(null);

        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    void shouldBehaveAsFifoWhenLimitExceeded() {
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
    void getHistoryShouldReturnCopy() {
        historyManager.add(task);
        List<Task> copy = historyManager.getHistory();

        copy.clear();

        List<Task> original = historyManager.getHistory();
        assertEquals(1, original.size());
    }
}