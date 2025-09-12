package ru.java.java_kanban.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.java.java_kanban.manager.task.TaskManager;
import ru.java.java_kanban.model.Epic;

import java.io.IOException;
import java.util.Objects;

public class EpicHttpHandler extends BaseHttpHandler {
    private final Gson gson = BaseHttpHandler.gson();
    private final TaskManager manager;

    public EpicHttpHandler(TaskManager manager) {
        this.manager = Objects.requireNonNull(manager, "EpicHttpHandler: manager must not be null");
    }

    @Override
    protected void toGet(HttpExchange exchange) throws IOException {
        String idStr = queryParameter(exchange, "id");
        if (idStr == null) {
            sendJson(exchange, gson.toJson(manager.getAllEpics()), 200);
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            sendJson(exchange, "bad id", 400);
            return;
        }

        Epic epic = manager.getEpicById(id);
        if (epic == null) {
            sendNotFound(exchange);
        } else {
            sendJson(exchange, gson.toJson(epic), 200);
        }
    }

    @Override
    protected void toPost(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Epic epic;
        try {
            epic = gson.fromJson(body, Epic.class);
        } catch (RuntimeException je) {
            sendJson(exchange, "bad json", 400);
            return;
        }
        if (epic == null) {
            sendJson(exchange, "bad json", 400);
            return;
        }

        try {
            Integer id = epic.getId();
            if (id == null || id == 0) {
                Epic created = manager.addEpic(epic);
                sendJson(exchange, gson.toJson(created), 201);
            } else {
                Epic old = manager.getEpicById(id);
                if (old == null) {
                    sendNotFound(exchange);
                    return;
                }
                manager.updateEpic(epic);
                sendJson(exchange, gson.toJson(epic), 200);
            }
        } catch (IllegalArgumentException iae) {
            sendHasInteractions(exchange);
        }
    }

    @Override
    protected void toDelete(HttpExchange exchange) throws IOException {
        String idStr = queryParameter(exchange, "id");
        if (idStr == null) {
            manager.deleteAllEpics();
            sendNoContent(exchange);
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            sendJson(exchange, "bad id", 400);
            return;
        }

        Epic existed = manager.getEpicById(id);
        if (existed == null) {
            sendNotFound(exchange);
            return;
        }

        manager.deleteEpicById(id);
        sendNoContent(exchange);
    }
}
