import ru.java.kanban.main.model.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {

    @Test
    void equals_returnTrue_sameId() {
        Epic epic1 = new Epic("Epic 1", "desc");
        epic1.setId(1);
        Epic epic2 = new Epic("Epic 2", "different desc");
        epic2.setId(1);

        assertEquals(epic1, epic2);
    }

    @Test
    void addSubtaskId_notAddIfEqualsEpicId() {
        Epic epic = new Epic("Epic", "desc");
        epic.setId(100);
        epic.addSubtaskIds(100);

        assertFalse(epic.getSubtaskIds().contains(100));
    }


    @Test
    //сделала сам тест и в методе самого эпика проверяю дубли
    void addSubtaskId_addIdToList() {
        Epic epic = new Epic("Epic", "desc");
        epic.addSubtaskIds(42);

        assertTrue(epic.getSubtaskIds().contains(42));
    }

    @Test
    void toString_containEpicNameAndId() {
        Epic epic = new Epic("Test Epic", "desc");
        epic.setId(5);

        String result = epic.toString();
        assertTrue(result.contains("Test Epic"));
        assertTrue(result.contains("5"));
    }

}
