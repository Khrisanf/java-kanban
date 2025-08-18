package ru.java.java_kanban.manager.priority;

import ru.java.java_kanban.model.Task;

import java.util.List;

public interface PriorityIndex {
    void add(Task task);
    void replace(Task oldTask, Task newTask);
    void remove(Task task);
    void clearAll();
    List<Task> asList();
}
