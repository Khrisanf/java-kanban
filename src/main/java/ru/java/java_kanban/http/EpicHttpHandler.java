package ru.java.java_kanban.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.java.java_kanban.exceptions.BadRequestException;
import ru.java.java_kanban.exceptions.NotFoundException;
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
    protected void doGet(HttpExchange exchange) throws IOException {
        String idStr = queryParameter(exchange, "id");
        if (idStr == null) {
            sendJson(exchange, gson.toJson(manager.getAllEpics()), 200);
        }

        int id = parseIdOrBadRequest(idStr);

        Epic epic = manager.getEpicById(id);
        if (epic == null) {
            throw new NotFoundException("epic " + id + " not found");
        } else {
            sendJson(exchange, gson.toJson(epic), 200);
        }
    }

    @Override
    protected void doPost(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Epic epic = gson.fromJson(body, Epic.class);
        if (epic == null) {
            throw new BadRequestException("bad json");
        }

        Integer id = epic.getId();
        if (id == null || id == 0) {
            Epic created = manager.addEpic(epic);
            sendJson(exchange, gson.toJson(created), 201);
        } else {
            Epic old = manager.getEpicById(id);
            if (old == null) {
                throw new NotFoundException("epic " + id + " not found");
            }
            manager.updateEpic(epic);
            sendJson(exchange, gson.toJson(epic), 200);
        }
    }

    @Override
    protected void doDelete(HttpExchange exchange) throws IOException {
        String idStr = queryParameter(exchange, "id");
        if (idStr == null) {
            manager.deleteAllEpics();
            sendNoContent(exchange);
            return;
        }

        int id = parseIdOrBadRequest(idStr);

        Epic existed = manager.getEpicById(id);
        if (existed == null) {
            throw new NotFoundException("epic " + id + " not found");
        }

        manager.deleteEpicById(id);
        sendNoContent(exchange);
    }
}
