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

public class BaseHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            switch (exchange.getRequestMethod()) {
                case "GET":
                    toGet(exchange);
                    break;
                case "POST":
                    toPost(exchange);
                    break;
                case "PUT":
                    toPut(exchange);
                    break;
                case "DELETE":
                    toDelete(exchange);
                    break;
                default:
                    sendJson(exchange, "method not allowed", 405);
            }
        } catch (BadRequestException e) {
            sendJson(exchange, "{\"error\":\"" + e.getMessage() + "\"}", 400);
        } catch (NotFoundException e) {
            sendJson(exchange, "{\"error\":\"" + e.getMessage() + "\"}", 404);
        } catch (ConflictException e) {
            sendJson(exchange, "{\"error\":\"" + e.getMessage() + "\"}", 409);
        } catch (com.google.gson.JsonSyntaxException | java.time.format.DateTimeParseException e) {
            sendJson(exchange, "{\"error\":\"bad json\"}", 400);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("HTTP 500 at: " + e.getClass().getName());
            for (StackTraceElement el : e.getStackTrace()) {
                System.err.println("  at " + el);
                break;
            }
            sendJson(exchange, "{\"error\":\"server_error\"}", 500);
        } finally {
            exchange.close();
        }
    }

    protected void toGet(HttpExchange exchange) throws IOException{
        sendJson(exchange, "method not allowed", 405);
    }

    protected void toPost(HttpExchange exchange) throws IOException{
        sendJson(exchange, "method not allowed", 405);
    }

    protected void toPut(HttpExchange exchange) throws IOException{
        sendJson(exchange, "method not allowed", 405);
    }

    protected void toDelete(HttpExchange exchange) throws IOException{
        sendJson(exchange, "method not allowed", 405);
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
        String message = "not found";
        byte[] response = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(404, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    // for overlaps task
    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        String message = "not acceptable: overlap tasks";
        byte[] response = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(406, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
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
