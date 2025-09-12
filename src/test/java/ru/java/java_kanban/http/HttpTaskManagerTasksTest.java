package ru.java.java_kanban.http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.java.java_kanban.manager.history.InMemoryHistoryManager;
import ru.java.java_kanban.manager.task.InMemoryTaskManager;
import ru.java.java_kanban.manager.task.TaskManager;
import ru.java.java_kanban.model.Task;
import ru.java.java_kanban.model.TaskStatus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HttpTaskManagerTasksTest {

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
    void createTask_returns201_and_persistsInManager() throws Exception {
        Task task = new Task("Test 2", "Desc", TaskStatus.NEW);
        task.setStartTime(LocalDateTime.parse("2025-08-31T10:00"));
        task.setDuration(Duration.ofMinutes(5));

        String json = BaseHttpHandler.gson().toJson(task);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, resp.statusCode());

        var tasks = manager.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("Test 2", tasks.get(0).getName());
    }


    @Test
    void getAll_whenEmpty_returns200_andEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + "/tasks"))
                .GET().build();

        var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertEquals("[]", resp.body());
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + "/tasks?id=999"))
                .GET().build();

        var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, resp.statusCode());
    }

    @Test
    void updateTask_returns200() throws Exception {
        Task t = new Task("A", "B", TaskStatus.NEW);
        t.setStartTime(LocalDateTime.parse("2025-08-20T10:00"));
        t.setDuration(Duration.ofMinutes(30));
        t = manager.addTask(t);

        t.setName("A+");
        String json = BaseHttpHandler.gson().toJson(t);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertEquals("A+", manager.getTaskById(t.getId()).getName());
    }

    @Test
    void deleteById_returns204() throws Exception {
        Task t = new Task("X", "", TaskStatus.NEW);
        t.setStartTime(LocalDateTime.parse("2025-08-20T10:00"));
        t.setDuration(Duration.ofMinutes(1));
        t = manager.addTask(t);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + "/tasks?id=" + t.getId()))
                .DELETE().build();

        var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, resp.statusCode());
        assertNull(manager.getTaskById(t.getId()));
    }

    @Test
    void badId_returns400() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + "/tasks?id=abc"))
                .GET().build();

        var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, resp.statusCode());
    }
}
