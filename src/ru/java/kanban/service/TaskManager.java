package ru.java.kanban.service;

import ru.java.kanban.model.Epic;
import ru.java.kanban.model.Subtask;
import ru.java.kanban.model.Task;

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

    Subtask addSubtaskByEpic(Subtask subtask);

    void updateSubtask(Subtask updatedSubtask);

    void deleteSubtaskById(Integer id);

    List<Subtask> getSubtaskOfEpic(Integer epicId);

    List<Task> getHistory();
}
