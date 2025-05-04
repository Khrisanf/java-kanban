package service;

import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskManager {
    private int nextId = 1;

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();


    //All about Task
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void deleteAllTasks() {
        tasks.clear();
    }
    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public Task addTask(Task task) {
        Task newTask = new Task(nextId++, task.getName(), task.getDescription(), task.getStatus());
        tasks.put(newTask.getId(), newTask);
        return newTask;
    }


    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }


    //All about Epic
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public void deleteAllEpics() {
        epics.clear();
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public void addEpic(Epic epic) {
        Epic newEpic = new Epic(nextId++, epic.getName(), epic.getDescription(), TaskStatus.NEW);
        epics.put(newEpic.getId(), newEpic);
    }

    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);

        if (epic != null) {
            subtasks.values().removeIf(s -> s.getEpic().getId() == id);
        }
    }


    public void updateEpic(Epic updatedEpic) {
        int id = updatedEpic.getId();
        Epic existingEpic = epics.get(id);

        if (existingEpic != null) {
            existingEpic.setName(updatedEpic.getName());
            existingEpic.setDescription(updatedEpic.getDescription());

            existingEpic.updateStatus();
        }
    }


    //All about Subtask
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.updateStatus();
        }
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public void addSubtaskByEpic(Subtask subtask) {
        int id = nextId++;

        Subtask newSubtask = new Subtask(
                id,
                subtask.getName(),
                subtask.getDescription(),
                subtask.getStatus(),
                subtask.getEpic()
        );

        this.subtasks.put(id, newSubtask);

        Epic epic = subtask.getEpic();

        if (epic != null) {
            epic.addSubtasks(newSubtask);
        } else {
            System.out.println("error! there is no epic for this task !");
        }
    }

    public void updateSubtask (Subtask updatedSubtask) {
        int id = updatedSubtask.getId();
        if (subtasks.containsKey(id)) {
            subtasks.put(id, updatedSubtask);
            Epic epic = updatedSubtask.getEpic();
            if (epic != null) {
                epic.updateStatus();
            }
        }
    }

    public void deleteSubtaskById(int id) {
        Subtask s = subtasks.remove(id);
        if (s != null) {
            Epic epic = s.getEpic();
            if (epic != null) {
                epic.updateStatus();
            }
        }
    }

    public List<Subtask> getSubtaskOfEpic(int epicId) {
        List<Subtask> result = new ArrayList<>();
        for (Subtask s : subtasks.values()) {
            if (s.getEpic() != null && s.getEpic().getId() == epicId) {
                result.add(s);
            }
        }
        return result;
    }

}
