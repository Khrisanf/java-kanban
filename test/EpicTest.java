import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.java.kanban.model.*;
import ru.java.kanban.service.*;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {

    private TaskManager manager;
    private Epic epic;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
        epic = manager.addEpic(new Epic("Epic for test", "Testing status changes"));
    }

    @Test
    void shouldReturnNewIfAllSubtasksAreNew() {
        manager.addSubtaskByEpic(new Subtask("Subtask1", "something", TaskStatus.NEW, epic.getId()));
        manager.addSubtaskByEpic(new Subtask("Subtask2", "something", TaskStatus.NEW, epic.getId()));

        assertEquals(TaskStatus.NEW, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void shouldReturnDoneIfAllSubtasksAreDone() {
        manager.addSubtaskByEpic(new Subtask("Sub 1", "", TaskStatus.DONE, epic.getId()));
        manager.addSubtaskByEpic(new Subtask("Sub 2", "", TaskStatus.DONE, epic.getId()));

        assertEquals(TaskStatus.DONE, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void shouldReturnInProgressIfSubtasksAreNewAndDone() {
        manager.addSubtaskByEpic(new Subtask("Sub 1", "", TaskStatus.NEW, epic.getId()));
        manager.addSubtaskByEpic(new Subtask("Sub 2", "", TaskStatus.DONE, epic.getId()));

        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void shouldReturnInProgressIfAtLeastOneInProgress() {
        manager.addSubtaskByEpic(new Subtask("Sub 1", "", TaskStatus.NEW, epic.getId()));
        manager.addSubtaskByEpic(new Subtask("Sub 2", "", TaskStatus.IN_PROGRESS, epic.getId()));

        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
    }
}
