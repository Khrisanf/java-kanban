import org.junit.jupiter.api.Test;
import ru.java.kanban.model.*;
import ru.java.kanban.service.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerSTRESSTest {

    @Test
    void shouldAddAndDeleteHalfMillionTasks() {
        TaskManager manager = Managers.getDefault();
        int count = 20_000;

        for (int i = 0; i < count; i++) {
            Task task = new Task("Task #" + i, "Stress test", TaskStatus.NEW);
            manager.addTask(task);
        }

        assertEquals(count, manager.getAllTasks().size(), "ALL TASKS SHOULD BE ADD");

        manager.deleteAllTasks();
        assertEquals(0, manager.getAllTasks().size(), "ALL TASKS SHOULD BE REMOVED");
    }

    @Test
    void shouldCorrectlyCalculateEpicStatusWithManySubtasks() {
        TaskManager manager = Managers.getDefault();

        Epic epic = manager.addEpic(new Epic("Mega Epic", "Stress test with subtasks"));

        int count = 20_000;

        // 50% DONE and 50% NEW → expect IN_PROGRESS
        for (int i = 0; i < count; i++) {
            TaskStatus status = (i % 2 == 0) ? TaskStatus.DONE : TaskStatus.NEW;
            Subtask subtask = new Subtask("Subtask " + i, "desc", status, epic.getId());
            manager.addSubtaskByEpic(subtask);
        }

        Epic updatedEpic = manager.getEpicById(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getStatus(), "EPIC STATUS SHOULD BE IN_PROGRESS");
    }

    @Test
    void shouldContainOnlyLast10ViewedTasks() {
        HistoryManager historyManager = new InMemoryHistoryManager();

        for (int i = 1; i <= 100; i++) {
            Task task = new Task("Task " + i, "desc", TaskStatus.NEW);
            task.setId(i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();

        assertEquals(10, history.size(), "HISTORIA SHOULD HAS ONLY 10");

        for (int i = 0; i < 10; i++) {
            int expectedId = 91 + i;
            assertEquals(expectedId, history.get(i).getId(), "EXPECT ID " + expectedId);
        }
    }
}
