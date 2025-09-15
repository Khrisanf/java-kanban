package ru.java.java_kanban.http;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.java.java_kanban.exceptions.BadRequestException;
import ru.java.java_kanban.exceptions.ConflictException;
import ru.java.java_kanban.exceptions.NotFoundException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class BaseHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            switch (exchange.getRequestMethod()) {
                case "GET":
                    doGet(exchange);
                    break;
                case "POST":
                    doPost(exchange);
                    break;
                case "PUT":
                    doPut(exchange);
                    break;
                case "DELETE":
                    doDelete(exchange);
                    break;
                default:
                    methodNotAllowed(exchange);
            }
        } catch (BadRequestException e) {
            sendError(exchange, 400, "BAD_REQUEST", e.getMessage());
        } catch (ConflictException e) {
            sendHasInteractions(exchange);
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        } catch (JsonSyntaxException | DateTimeParseException e) {
            sendError(exchange, 400, "BAD_JSON", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "SERVER_ERROR",
                    "internal server error");
        } finally {
            exchange.close();
        }
    }

    protected void doGet(HttpExchange exchange) throws IOException{
        methodNotAllowed(exchange);
    }

    protected void doPost(HttpExchange exchange) throws IOException{
        methodNotAllowed(exchange);
    }

    protected void doPut(HttpExchange exchange) throws IOException{
        methodNotAllowed(exchange);
    }

    protected void doDelete(HttpExchange exchange) throws IOException{
        methodNotAllowed(exchange);
    }

    protected void sendError(HttpExchange exchange, int status, String code, String message) throws IOException {
        String json = gson().toJson(new ApiError(status, code, message));
        sendJson(exchange, json, status);
    }

    protected void methodNotAllowed(HttpExchange exchange) throws IOException {
        sendError(exchange, 405, "METHOD_NOT_ALLOWED", "method not allowed");
    }

    protected void sendJson(HttpExchange exchange, String json, int code) throws IOException {
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(code, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendError(exchange, 404, "NOT_FOUND", "not found");
    }

    // for overlaps task
    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendError(exchange, 406, "OVERLAP", "not acceptable: overlap tasks");
    }

    protected void sendNoContent(HttpExchange ex) throws IOException {
        ex.sendResponseHeaders(204, -1);
    }

    // read body from stream
    protected String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    // search parameter after ? in URI
    protected String queryParameter(HttpExchange exchange, String name) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null || query.isEmpty()) {
            return null;
        }
        for (String parameter : query.split("&")) {
            int index = parameter.indexOf("=");
            if (index > 0 && parameter.substring(0, index).equals(name)) {
                return parameter.substring(index + 1);
            }
        }
        return null;
    }

    // exception interception
    protected int parseIdOrBadRequest(String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new BadRequestException("bad id");
        }

    }

    public static Gson gson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, t, ctx) ->
                        new JsonPrimitive(src.toString()))
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, t, ctx) ->
                        LocalDateTime.parse(json.getAsString()))
                .registerTypeAdapter(Duration.class, (JsonSerializer<Duration>) (src, t, ctx) ->
                        new JsonPrimitive(src.getSeconds()))
                .registerTypeAdapter(Duration.class, (JsonDeserializer<Duration>) (json, t, ctx) ->
                        Duration.ofSeconds(json.getAsLong()))
                .create();
    }
}
