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

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerHistoryTest {

    private HttpTaskServer server;
    private InMemoryHistoryManager historyManager;
    private TaskManager manager;
    private HttpClient client;

    private String historyPath;

    @BeforeEach
    void setUp() throws Exception {
        historyManager = new InMemoryHistoryManager();
        manager = new InMemoryTaskManager(historyManager);
        server = new HttpTaskServer(manager);
        server.start();
        client = HttpClient.newHttpClient();

        historyPath = "/history";
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void get_whenEmpty_returns200_andEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + historyPath))
                .GET().build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertEquals("[]", resp.body());
    }

    @Test
    void historyReflectsViewOrder_andHasNoDuplicates() throws Exception {
        Task t1 = new Task("T1", "D1", TaskStatus.NEW);
        t1.setStartTime(LocalDateTime.parse("2025-08-20T10:00"));
        t1.setDuration(Duration.ofMinutes(5));
        t1 = manager.addTask(t1);

        Task t2 = new Task("T2", "D2", TaskStatus.NEW);
        t2.setStartTime(LocalDateTime.parse("2025-08-20T11:00"));
        t2.setDuration(Duration.ofMinutes(5));
        t2 = manager.addTask(t2);

        HttpRequest getT1 = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + "/tasks?id=" + t1.getId()))
                .GET().build();
        client.send(getT1, HttpResponse.BodyHandlers.ofString());

        HttpRequest getT2 = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + "/tasks?id=" + t2.getId()))
                .GET().build();
        client.send(getT2, HttpResponse.BodyHandlers.ofString());

        HttpRequest getT1Again = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + "/tasks?id=" + t1.getId()))
                .GET().build();
        client.send(getT1Again, HttpResponse.BodyHandlers.ofString());

        HttpRequest getHistory = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + historyPath))
                .GET().build();

        HttpResponse<String> resp = client.send(getHistory, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());

        Task[] arr = BaseHttpHandler.gson().fromJson(resp.body(), Task[].class);
        assertNotNull(arr);
        assertEquals(2, arr.length);
        assertEquals(t2.getId(), arr[0].getId());
        assertEquals(t1.getId(), arr[1].getId());
    }
}
