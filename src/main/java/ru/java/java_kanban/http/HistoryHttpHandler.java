package ru.java.java_kanban.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.java.java_kanban.manager.task.TaskManager;

import java.io.IOException;
import java.util.Objects;

public class HistoryHttpHandler extends BaseHttpHandler {
    private final Gson gson = BaseHttpHandler.gson();
    private final TaskManager manager;

    public HistoryHttpHandler(TaskManager manager) {
        this.manager = Objects.requireNonNull(manager, "HistoryHttpHandler: manager must not be null");
    }

    @Override
    protected void doGet(HttpExchange exchange) throws IOException {
        sendJson(exchange, gson.toJson(manager.getHistory()), 200);
    }
}
