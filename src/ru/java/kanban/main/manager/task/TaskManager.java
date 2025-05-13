package ru.java.kanban.main.manager.task;

import ru.java.kanban.main.model.Epic;
import ru.java.kanban.main.model.Subtask;
import ru.java.kanban.main.model.Task;

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
}
