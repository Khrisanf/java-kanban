package ru.java.java_kanban.http;

import com.sun.net.httpserver.HttpServer;
import ru.java.java_kanban.manager.history.InMemoryHistoryManager;
import ru.java.java_kanban.manager.task.TaskManager;
import ru.java.java_kanban.manager.task.FileBackedTaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;

public class HttpTaskServer {
    static int PORT = 8080;
    static Path file = Path.of("tasks.csv");
    static TaskManager manager = new FileBackedTaskManager(new InMemoryHistoryManager(), file);

    private final int requestedPort;
    private HttpServer server;
    private int boundPort;

    public HttpTaskServer(TaskManager manager) {
        this(manager, 0);
    }

    public HttpTaskServer(TaskManager manager, int port) {
        this.manager = manager;
        this.requestedPort = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(requestedPort), 0);
        server.createContext("/tasks", new TaskHttpHandler(manager));
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
        HttpTaskServer server = new HttpTaskServer(manager, PORT);
        server.start();
    }
}
