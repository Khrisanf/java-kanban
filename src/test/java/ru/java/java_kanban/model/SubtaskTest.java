package ru.java.java_kanban.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {

    @Test
    void getEpicId_returnCorrectId() {
        Subtask sub = new Subtask("Name", "Desc", TaskStatus.NEW, 42);
        assertEquals(42, sub.getEpicId());
    }

    @Test
    void equals_returnTrueForSameId() {
        Subtask s1 = new Subtask("S1", "D1", TaskStatus.NEW, 1);
        s1.setId(10);
        Subtask s2 = new Subtask("S2", "D2", TaskStatus.DONE, 2);
        s2.setId(10);

        assertEquals(s1, s2);
    }
}
