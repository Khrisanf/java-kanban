package ru.java.java_kanban.http;

public record ApiError(int status, String code, String message) {

}
