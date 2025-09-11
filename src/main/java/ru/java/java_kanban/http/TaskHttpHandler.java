package ru.java.java_kanban.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.java.java_kanban.manager.task.TaskManager;
import ru.java.java_kanban.model.Task;

import java.io.IOException;
import java.util.Objects;

public class TaskHttpHandler extends BaseHttpHandler {
    private final Gson gson = BaseHttpHandler.gson();
    private final TaskManager manager;

    public TaskHttpHandler(TaskManager manager) {
        this.manager = Objects.requireNonNull(manager, "TaskHttpHandler: manager must not be null");
    }

    @Override
    protected void toGet(HttpExchange exchange) throws IOException {
        String idStr = queryParameter(exchange, "id");
        if (idStr == null) {
            sendJson(exchange, gson.toJson(manager.getAllTasks()), 200);
            return;
        }
        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            sendJson(exchange, "bad id", 400);
            return;
        }
        Task taskById = manager.getTaskById(id);
        if (taskById == null) {
            sendNotFound(exchange);
        } else {
            sendJson(exchange, gson.toJson(taskById), 200);
        }
    }

    @Override
    protected void toPost(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Task task;
        try {
            task = gson.fromJson(body, Task.class);
        } catch (RuntimeException je) {
            sendJson(exchange, "bad json", 400);
            return;
        }
        if (task == null) {
            sendJson(exchange, "bad json", 400);
            return;
        }

        try {
            Integer id = task.getId();

            if (id == null || id == 0) {
                Task created = manager.addTask(task);
                sendJson(exchange, gson.toJson(created), 201);
                return;
            } else {
                Task old = manager.getTaskById(id);
                if (old == null) {
                    sendNotFound(exchange);
                    return;
                }
                manager.updateTask(task);
                sendJson(exchange, gson.toJson(task), 200);
                return;
            }

        } catch (IllegalArgumentException iae) {
            sendHasInteractions(exchange);
        }
    }


    @Override
    protected void toDelete(HttpExchange exchange) throws IOException {
        String idStr = queryParameter(exchange, "id");
        if (idStr == null) {
            manager.deleteAllTasks();
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
        Task existed = manager.getTaskById(id);
        if (existed == null) {
            sendNotFound(exchange);
            return;
        }
        manager.deleteTaskById(id);
        sendNoContent(exchange);
    }
}
