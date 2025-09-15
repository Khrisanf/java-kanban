package ru.java.java_kanban.http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.java.java_kanban.manager.history.InMemoryHistoryManager;
import ru.java.java_kanban.manager.task.InMemoryTaskManager;
import ru.java.java_kanban.manager.task.TaskManager;
import ru.java.java_kanban.model.Epic;
import ru.java.java_kanban.model.Subtask;
import ru.java.java_kanban.model.TaskStatus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerSubtasksTest {

    private HttpTaskServer server;
    private InMemoryHistoryManager historyManager;
    private TaskManager manager;
    private HttpClient client;

    @BeforeEach
    void setUp() throws Exception {
        historyManager = new InMemoryHistoryManager();
        manager = new InMemoryTaskManager(historyManager);
        server = new HttpTaskServer(manager);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void createSubtask_returns201_and_persistsInManager() throws Exception {
        Epic epic = manager.addEpic(new Epic("Parent", "Epic D"));

        Subtask s = new Subtask("S1", "D", TaskStatus.NEW, epic.getId());
        s.setStartTime(LocalDateTime.parse("2025-08-31T11:00"));
        s.setDuration(Duration.ofMinutes(10));

        String json = BaseHttpHandler.gson().toJson(s);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + "/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, resp.statusCode());

        var subtasks = manager.getAllSubtasks();
        assertEquals(1, subtasks.size());
        assertEquals("S1", subtasks.get(0).getName());
        assertEquals(epic.getId(), subtasks.get(0).getEpicId());
    }

    @Test
    void getAll_whenEmpty_returns200_andEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + "/subtasks"))
                .GET().build();

        var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertEquals("[]", resp.body());
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + "/subtasks?id=999"))
                .GET().build();

        var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, resp.statusCode());
    }

    @Test
    void getByEpicId_returns200_andOnlyEpicSubtasks() throws Exception {
        Epic e1 = manager.addEpic(new Epic("E1", "D1"));
        Epic e2 = manager.addEpic(new Epic("E2", "D2"));

        Subtask s1 = new Subtask("S1", "D", TaskStatus.NEW, e1.getId());
        s1.setStartTime(LocalDateTime.parse("2025-08-20T10:00"));
        s1.setDuration(Duration.ofMinutes(5));
        manager.addSubtask(s1);

        Subtask s2 = new Subtask("S2", "D", TaskStatus.NEW, e2.getId());
        s2.setStartTime(LocalDateTime.parse("2025-08-20T11:00"));
        s2.setDuration(Duration.ofMinutes(5));
        manager.addSubtask(s2);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + "/subtasks?epicId=" + e1.getId()))
                .GET().build();

        var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());

        Subtask[] arr = BaseHttpHandler.gson().fromJson(resp.body(), Subtask[].class);
        assertEquals(1, arr.length);
        assertEquals(e1.getId(), arr[0].getEpicId());
        assertEquals("S1", arr[0].getName());
    }

    @Test
    void updateSubtask_returns200() throws Exception {
        Epic epic = manager.addEpic(new Epic("Parent", "D"));
        Subtask s = new Subtask("S", "D", TaskStatus.NEW, epic.getId());
        s.setStartTime(LocalDateTime.parse("2025-08-20T12:00"));
        s.setDuration(Duration.ofMinutes(15));
        s = manager.addSubtask(s);

        s.setName("S+");
        String json = BaseHttpHandler.gson().toJson(s);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + "/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertEquals("S+", manager.getSubtaskById(s.getId()).getName());
    }

    @Test
    void deleteById_returns204() throws Exception {
        Epic epic = manager.addEpic(new Epic("Parent", "D"));
        Subtask s = new Subtask("Del", "D", TaskStatus.NEW, epic.getId());
        s.setStartTime(LocalDateTime.parse("2025-08-20T13:00"));
        s.setDuration(Duration.ofMinutes(1));
        s = manager.addSubtask(s);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + "/subtasks?id=" + s.getId()))
                .DELETE().build();

        var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, resp.statusCode());
        assertNull(manager.getSubtaskById(s.getId()));
    }

    @Test
    void badId_returns400() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + "/subtasks?id=abc"))
                .GET().build();

        var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, resp.statusCode());
    }
}
