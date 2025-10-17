package ru.java.java_kanban.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

public class HttpTaskManagerPrioritizedTest {

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
    void get_whenEmpty_return200_andEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + "/priority"))
                .GET().build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertEquals("[]", resp.body());
    }

    @Test
    void get_whenTwoScheduledAndOneUnscheduled_returnSorted_andSkipUnscheduled() throws Exception {
        addScheduledTask("A_08_30", LocalDateTime.of(2025, 1, 1, 8, 0), 30);
        addScheduledTask("B_09_30", LocalDateTime.of(2025, 1, 1, 9, 0), 30);
        addUnscheduledTask("C_unscheduled");

        JsonArray arr = requestPriority();
        assertEquals(2, arr.size());
        assertEquals("A_08_30", getName(arr.get(0)));
        assertEquals("B_09_30", getName(arr.get(1)));
    }

    @Test
    void get_whenBackToBackIntervals_returnSorted_andNoConflict() throws Exception {
        addScheduledTask("Short_010", LocalDateTime.of(2025, 2, 1, 9, 0), 10);
        addScheduledTask("Long_120",  LocalDateTime.of(2025, 2, 1, 9, 10), 120);

        JsonArray arr = requestPriority();
        assertEquals(2, arr.size());
        assertEquals("Short_010", getName(arr.get(0)));
        assertEquals("Long_120",  getName(arr.get(1)));
    }

    @Test
    void get_whenSameStartTime_zeroDuration_orderById_andNoConflict() throws Exception {
        LocalDateTime t = LocalDateTime.of(2025, 2, 1, 12, 0);

        Task a = addScheduledTask("Instant_A", t, 0);
        Task b = addScheduledTask("Instant_B", t, 0);

        JsonArray arr = requestPriority();
        assertEquals(2, arr.size());
        assertEquals("Instant_A", getName(arr.get(0)));
        assertEquals("Instant_B", getName(arr.get(1)));
    }

    @Test
    void get_whenTaskUpdatedFromUnscheduledToScheduled_appearInList() throws Exception {
        Task t = new Task("X_to_schedule", "desc", TaskStatus.NEW);
        t = manager.addTask(t);
        assertEquals(0, requestPriority().size());

        t.setStartTime(LocalDateTime.of(2025, 3, 10, 12, 0));
        t.setDuration(Duration.ofMinutes(45));
        manager.updateTask(t);

        JsonArray arr = requestPriority();
        assertEquals(1, arr.size());
        assertEquals("X_to_schedule", getName(arr.get(0)));
    }

    @Test
    void get_whenTaskDeleted_removeFromPriority() throws Exception {
        Task keep = addScheduledTask("Keep",     LocalDateTime.of(2025, 4, 1, 8, 0), 30);
        Task del  = addScheduledTask("DeleteMe", LocalDateTime.of(2025, 4, 1, 9, 0), 30);

        assertEquals(2, requestPriority().size());

        manager.deleteTaskById(del.getId());
        JsonArray arr = requestPriority();
        assertEquals(1, arr.size());
        assertEquals("Keep", getName(arr.get(0)));
    }

    @Test
    void get_whenOnlyStartTimeOrOnlyDuration_excludeFromPriority() throws Exception {
        Task onlyStart = new Task("OnlyStart", "desc", TaskStatus.NEW);
        onlyStart.setStartTime(LocalDateTime.of(2025, 5, 5, 10, 0));
        manager.addTask(onlyStart);

        Task onlyDuration = new Task("OnlyDuration", "desc", TaskStatus.NEW);
        onlyDuration.setDuration(Duration.ofMinutes(15));
        manager.addTask(onlyDuration);

        assertEquals(0, requestPriority().size());
    }

    @Test
    void post_return405() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + "/priority"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, resp.statusCode());
    }

    private Task addScheduledTask(String name, LocalDateTime start, long minutes) {
        Task t = new Task(name, "desc", TaskStatus.NEW);
        t.setStartTime(start);
        t.setDuration(Duration.ofMinutes(minutes));
        return manager.addTask(t);
    }

    private Task addUnscheduledTask(String name) {
        Task t = new Task(name, "desc", TaskStatus.NEW);
        return manager.addTask(t);
    }

    private JsonArray requestPriority() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(server.getBaseUrl() + "/priority"))
                .GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        return JsonParser.parseString(resp.body()).getAsJsonArray();
    }

    private static String getName(com.google.gson.JsonElement e) {
        JsonObject obj = e.getAsJsonObject();
        return obj.has("name") ? obj.get("name").getAsString() : "";
    }
}
