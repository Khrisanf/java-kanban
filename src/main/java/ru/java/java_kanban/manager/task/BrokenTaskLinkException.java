package ru.java.java_kanban.manager.task;

public class BrokenTaskLinkException extends RuntimeException {
    public BrokenTaskLinkException(String message) {
        super(message);
    }
}
