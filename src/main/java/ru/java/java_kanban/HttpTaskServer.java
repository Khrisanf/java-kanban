package ru.java.java_kanban;

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

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TaskHttpHandler(manager));

        server.start();
        System.out.println("HTTP server started on http://localhost:" + PORT);
    }
}
