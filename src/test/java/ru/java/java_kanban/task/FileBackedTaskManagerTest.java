package ru.java.java_kanban.task;

import org.junit.jupiter.api.*;
import ru.java.java_kanban.manager.ManagerSaveException;
import ru.java.java_kanban.manager.history.InMemoryHistoryManager;
import ru.java.java_kanban.manager.task.FileBackedTaskManager;
import ru.java.java_kanban.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {

    private FileBackedTaskManager manager;
    private Path testFile;

    @BeforeEach
    void setUp() throws IOException {
        testFile = Files.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(new InMemoryHistoryManager(), testFile);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(testFile);
    }

    // === TASK TESTS ===
    @Nested
    class TaskTest {

        @Test
        void addTask_savesTaskToFile() throws IOException {
            Task task = new Task("Test", "Description", TaskStatus.NEW, TaskType.TASK);
            manager.addTask(task);

            List<String> lines = Files.readAllLines(testFile);
            assertTrue(lines.stream().anyMatch(line -> line.contains("Test")));
        }

        @Test
        void updateTask_updatesFileAfterChange() throws IOException {
            Task task = manager.addTask(new Task("Test", "Desc", TaskStatus.NEW, TaskType.TASK));
            task.setStatus(TaskStatus.DONE);
            manager.updateTask(task);

            String content = Files.readString(testFile);
            assertTrue(content.contains("DONE"));
        }

        @Test
        void deleteTaskById_removesFromFile() throws IOException {
            Task task = manager.addTask(new Task("To delete", "Desc", TaskStatus.NEW, TaskType.TASK));
            manager.deleteTaskById(task.getId());

            String content = Files.readString(testFile);
            assertFalse(content.contains("To delete"));
        }
    }

    // === EPIC TESTS ===
    @Nested
    class EpicTest {

        @Test
        void addEpic_savesEpicAndSubtasksToFile() throws IOException {
            Epic epic = manager.addEpic(new Epic("Epic name", "Epic desc"));
            manager.addSubtask(new Subtask("Sub", "Desc", TaskStatus.NEW, epic.getId()));

            String content = Files.readString(testFile);
            assertTrue(content.contains("Epic name"));
            assertTrue(content.contains("Sub"));
        }

        @Test
        void deleteAllEpics_clearsFile() throws IOException {
            Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
            manager.addSubtask(new Subtask("S", "D", TaskStatus.NEW, epic.getId()));

            manager.deleteAllEpics();

            String content = Files.readString(testFile);
            assertFalse(content.contains("Epic"));
        }
    }

    // === SUBTASK TESTS ===
    @Nested
    class SubtaskTest {

        @Test
        void addSubtask_savesToFile() throws IOException {
            Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
            Subtask subtask = new Subtask("Sub", "Desc", TaskStatus.NEW, epic.getId());
            manager.addSubtask(subtask);

            String content = Files.readString(testFile);
            assertTrue(content.contains("Sub"));
        }

        @Test
        void deleteSubtaskById_updatesFile() throws IOException {
            Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
            Subtask subtask = manager.addSubtask(new Subtask("Sub", "Desc", TaskStatus.NEW, epic.getId()));

            manager.deleteSubtaskById(subtask.getId());
            String content = Files.readString(testFile);
            assertFalse(content.contains("Sub"));
        }
    }
}
