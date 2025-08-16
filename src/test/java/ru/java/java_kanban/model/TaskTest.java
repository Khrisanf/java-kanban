package ru.java.java_kanban.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {

    @Test
    void equals_returnTrueIfIdsAreEqual() {
        Task t1 = new Task("A", "Desc", TaskStatus.NEW);
        Task t2 = new Task("B", "Diff", TaskStatus.DONE);
        t1.setId(1);
        t2.setId(1);

        assertEquals(t1, t2);
    }

    @Test
    void hashCode_beEqualForSameId() {
        Task t1 = new Task("X", "Y", TaskStatus.IN_PROGRESS);
        Task t2 = new Task("Z", "W", TaskStatus.NEW);
        t1.setId(99);
        t2.setId(99);

        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    void toString_containKeyFields() {
        Task task = new Task("X", "Y", TaskStatus.NEW);
        task.setId(10);
        String result = task.toString();

        assertTrue(result.contains("id=10"));
        assertTrue(result.contains("name='X'"));
        assertTrue(result.contains("description='Y'"));
        assertTrue(result.contains("status=NEW"));
        assertTrue(result.contains("type=TASK"));
    }
}
