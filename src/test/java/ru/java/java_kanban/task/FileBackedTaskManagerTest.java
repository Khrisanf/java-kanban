package ru.java.java_kanban.task;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.java.java_kanban.manager.ManagerSaveException;
import ru.java.java_kanban.manager.history.InMemoryHistoryManager;
import ru.java.java_kanban.manager.task.FileBackedTaskManager;
import ru.java.java_kanban.model.Task;
import ru.java.java_kanban.model.TaskStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @TempDir
    Path tempDir;

    private Path backingFile;

    @Override
    protected FileBackedTaskManager createManager() {
        backingFile = tempDir.resolve("tasks.csv");
        return new FileBackedTaskManager(new InMemoryHistoryManager(), backingFile);
    }

    @Nested
    class Persistence {

        @Test
        public void saveAndLoad_roundTrip_preservesData() {
            Task t = manager.addTask(new Task("Persist", "D", TaskStatus.NEW));
            FileBackedTaskManager reloaded =
                    new FileBackedTaskManager(new InMemoryHistoryManager(), backingFile);

            Task restored = reloaded.getTaskById(t.getId());
            assertNotNull(restored);
            assertEquals(t.getName(), restored.getName());
            assertEquals(t.getDescription(), restored.getDescription());
            assertEquals(t.getStatus(), restored.getStatus());
        }

        @Test
        public void write_updatesFile_onAddUpdateDelete() throws IOException {
            Task t = manager.addTask(new Task("A", "D", TaskStatus.NEW));
            String content1 = Files.readString(backingFile);
            assertTrue(content1.contains("A"));

            t.setName("B");
            manager.updateTask(t);
            String content2 = Files.readString(backingFile);
            assertTrue(content2.contains("B"));

            manager.deleteTaskById(t.getId());
            String content3 = Files.readString(backingFile);
            assertFalse(content3.contains("B"));
        }

        @Test
        public void loadFromCorruptedFile_throwsManagerSaveException() throws IOException {
            Path broken = tempDir.resolve("broken.csv");
            Files.writeString(broken, "id,type,name,status,start,duration,epic\nXXX;WRONG");

            assertThrows(NumberFormatException.class, () ->
                    new FileBackedTaskManager(new InMemoryHistoryManager(), broken));
        }
    }
}
