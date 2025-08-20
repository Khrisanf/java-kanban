package ru.java.java_kanban.manager.task;


import ru.java.java_kanban.model.Epic;
import ru.java.java_kanban.model.Subtask;
import ru.java.java_kanban.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskManager {
    //All about Task
    List<Task> getAllTasks();

    void deleteAllTasks();

    Task getTaskById(Integer id);

    Task addTask(Task task);

    void deleteTaskById(Integer id);

    void updateTask(Task task);

    //All about Epic
    List<Epic> getAllEpics();

    void deleteAllEpics();

    Epic getEpicById(Integer id);

    Epic addEpic(Epic epic);

    void deleteEpicById(Integer id);

    void updateEpic(Epic updatedEpic);

    //All about Subtask
    List<Subtask> getAllSubtasks();

    void deleteAllSubtasks();

    Subtask getSubtaskById(Integer id);

    Subtask addSubtask(Subtask subtask);

    void updateSubtask(Subtask updatedSubtask);

    void deleteSubtaskById(Integer id);

    List<Subtask> getSubtasksOfEpic(Integer epicId);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}
