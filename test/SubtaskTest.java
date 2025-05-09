import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.java.kanban.model.*;
import ru.java.kanban.service.*;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {

    private TaskManager manager;
    private Epic epic;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
        epic = manager.addEpic(new Epic("Epic Test", "Epic Desc"));
    }

    @Test
    void shouldAffectEpicStatus() {
        Subtask sub1 = manager.addSubtaskByEpic(new Subtask("S1", "D1", TaskStatus.NEW, epic.getId()));
        Subtask sub2 = manager.addSubtaskByEpic(new Subtask("S2", "D2", TaskStatus.DONE, epic.getId()));

        Epic updatedEpic = manager.getEpicById(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getStatus());

        sub1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(sub1);

        updatedEpic = manager.getEpicById(epic.getId());
        assertEquals(TaskStatus.DONE, updatedEpic.getStatus());
    }

}
