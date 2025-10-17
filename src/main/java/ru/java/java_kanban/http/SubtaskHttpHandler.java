package ru.java.java_kanban.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.java.java_kanban.manager.task.TaskManager;
import ru.java.java_kanban.model.Subtask;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SubtaskHttpHandler extends BaseHttpHandler {
    private final Gson gson = BaseHttpHandler.gson();
    private final TaskManager manager;

    public SubtaskHttpHandler(TaskManager manager) {
        this.manager = Objects.requireNonNull(manager, "SubtaskHttpHandler: manager must not be null");
    }

    @Override
    protected void doGet(HttpExchange exchange) throws IOException {
        String idStr = queryParameter(exchange, "id");
        if (idStr != null) {
            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                sendJson(exchange, "bad id", 400);
                return;
            }
            Subtask subtask = manager.getSubtaskById(id);
            if (subtask == null) {
                sendNotFound(exchange);
            } else {
                sendJson(exchange, gson.toJson(subtask), 200);
            }
            return;
        }

        String epicIdStr = queryParameter(exchange, "epicId");
        if (epicIdStr != null) {
            int epicId;
            try {
                epicId = Integer.parseInt(epicIdStr);
            } catch (NumberFormatException e) {
                sendJson(exchange, "bad id", 400);
                return;
            }
            List<Subtask> list = manager.getSubtasksOfEpic(epicId);
            if (list == null) {
                list = Collections.emptyList();
            }
            sendJson(exchange, gson.toJson(list), 200);
            return;
        }

        sendJson(exchange, gson.toJson(manager.getAllSubtasks()), 200);
    }


    @Override
    protected void doPost(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Subtask subtask;
        try {
            subtask = gson.fromJson(body, Subtask.class);
        } catch (RuntimeException je) {
            sendJson(exchange, "bad json", 400);
            return;
        }
        if (subtask == null) {
            sendJson(exchange, "bad json", 400);
            return;
        }

        try {
            Integer id = subtask.getId();
            if (id == null || id == 0) {
                Subtask created = manager.addSubtask(subtask);
                sendJson(exchange, gson.toJson(created), 201);
            } else {
                Subtask old = manager.getSubtaskById(id);
                if (old == null) {
                    sendNotFound(exchange);
                    return;
                }
                manager.updateSubtask(subtask);
                sendJson(exchange, gson.toJson(subtask), 200);
            }
        } catch (IllegalArgumentException iae) {
            sendHasInteractions(exchange);
        }
    }

    @Override
    protected void doDelete(HttpExchange exchange) throws IOException {
        String idStr = queryParameter(exchange, "id");
        if (idStr == null) {
            manager.deleteAllSubtasks();
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

        Subtask existed = manager.getSubtaskById(id);
        if (existed == null) {
            sendNotFound(exchange);
            return;
        }

        manager.deleteSubtaskById(id);
        sendNoContent(exchange);
    }
}
