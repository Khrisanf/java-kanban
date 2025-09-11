package ru.java.java_kanban.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.java.java_kanban.manager.task.TaskManager;
import ru.java.java_kanban.model.Task;

import java.io.IOException;

public class TaskHttpHandler extends BaseHttpHandler {
        private TaskManager manager;
        private final Gson gson = BaseHttpHandler.gson();

        public TaskHttpHandler(TaskManager manager) {
            this.manager = manager;
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
            Task task = gson.fromJson(body, Task.class);
            if (task == null) {
                sendJson(exchange, "bad json", 400);
                return;
            }

            if (task.getId() == 0) {
                try {
                    Task created = manager.addTask(task);
                    sendJson(exchange, gson.toJson(created), 201);
                } catch (IllegalArgumentException iae) {
                    sendHasInteractions(exchange);
                }
            } else {
                Task old = manager.getTaskById(task.getId());
                if (old == null) {
                    sendNotFound(exchange);
                    return;
                }
                try {
                    manager.updateTask(task);
                    sendJson(exchange, gson.toJson(task), 200);
                } catch (IllegalArgumentException iae) {
                    sendHasInteractions(exchange);
                }
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
