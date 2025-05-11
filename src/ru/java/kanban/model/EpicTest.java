package ru.java.kanban.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {

    @Test
    void equals_shouldReturnTrueForSameId() {
        Epic epic1 = new Epic("Epic 1", "desc");
        epic1.setId(1);
        Epic epic2 = new Epic("Epic 2", "different desc");
        epic2.setId(1);

        assertEquals(epic1, epic2);
    }

    @Test
    void addSubtaskId_shouldAddIdToList() {
        Epic epic = new Epic("Epic", "desc");
        epic.addSubtaskIds(42);

        assertTrue(epic.getSubtaskIds().contains(42));
    }

    @Test
    void toString_shouldContainEpicNameAndId() {
        Epic epic = new Epic("Test Epic", "desc");
        epic.setId(5);

        String result = epic.toString();
        assertTrue(result.contains("Test Epic"));
        assertTrue(result.contains("5"));
    }

}
