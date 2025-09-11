package ru.java.java_kanban.http;

import com.sun.net.httpserver.HttpServer;
import ru.java.java_kanban.manager.history.InMemoryHistoryManager;
import ru.java.java_kanban.manager.task.FileBackedTaskManager;
import ru.java.java_kanban.manager.task.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Objects;

public class HttpTaskServer {
    private final TaskManager manager;
    private final int requestedPort;
    private HttpServer server;
    private int boundPort;

    public HttpTaskServer(TaskManager manager) {
        this(manager, 0);
    }
    public HttpTaskServer(TaskManager manager, int port) {
        this.manager = Objects.requireNonNull(manager, "HttpTaskServer: manager must not be null");
        this.requestedPort = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(requestedPort), 0);
        server.createContext("/tasks", new TaskHttpHandler(manager));
        server.createContext("/epics", new EpicHttpHandler(manager));
        server.createContext("/subtasks", new SubtaskHttpHandler(manager));

        server.start();
        boundPort = server.getAddress().getPort();
        System.out.println("HTTP server started on " + getBaseUrl());
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    public String getBaseUrl() {
        return "http://localhost:" + boundPort;
    }

    public static void main(String[] args) throws IOException {
        int PORT = 8080;
        Path file = Path.of("tasks.csv");
        TaskManager mgr = new FileBackedTaskManager(new InMemoryHistoryManager(), file);
        new HttpTaskServer(mgr, PORT).start();
    }
}
