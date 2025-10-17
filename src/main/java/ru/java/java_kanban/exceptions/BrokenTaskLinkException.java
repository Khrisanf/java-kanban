package ru.java.java_kanban.exceptions;

public class BrokenTaskLinkException extends RuntimeException {
    public BrokenTaskLinkException(String message) {
        super(message);
    }
}
