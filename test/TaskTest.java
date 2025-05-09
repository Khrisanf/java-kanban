import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import ru.java.kanban.model.*;

public class TaskTest {

    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task("Name", "Description", TaskStatus.NEW);
    }

    @Test
    void shouldCreateTaskWithCorrectFields() {
        assertEquals("Name", task.getName());
        assertEquals("Description", task.getDescription());
        assertEquals(TaskStatus.NEW, task.getStatus());
    }

    @Test
    void shouldSetAndGetFieldsCorrectly() {
        task.setId(42);
        task.setName("Updated name");
        task.setDescription("Updated description");
        task.setStatus(TaskStatus.DONE);

        assertEquals(42, task.getId());
        assertEquals("Updated name", task.getName());
        assertEquals("Updated description", task.getDescription());
        assertEquals(TaskStatus.DONE, task.getStatus());
    }

    @Test
    void shouldBeEqualIfIdsAreSame() {
        Task task2 = new Task("Name1", "Description1", TaskStatus.NEW);
        task.setId(1);
        task2.setId(1);

        assertEquals(task, task2);
    }

    @Test
    void shouldHaveSameHashCodeIfIdsAreSame() {
        Task task2 = new Task("Name1", "Description1", TaskStatus.NEW);
        task.setId(42);
        task2.setId(42);

        assertEquals(task.hashCode(), task2.hashCode());
    }

    @Test
    void toStringShouldContainAllFields() {
        task.setId(10);
        String result = task.toString();

        assertTrue(result.contains("id=10"));
        assertTrue(result.contains("name='Name'"));
        assertTrue(result.contains("description='Description'"));
        assertTrue(result.contains("status=NEW"));
    }
}
