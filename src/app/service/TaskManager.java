package app.service;

import app.model.Epic;
import app.model.Subtask;
import app.model.Task;
import app.model.TaskStatus;

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
        task.setId(nextId++);
        tasks.put(task.getId(), task);
        return task;
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
        List<Integer> epicIds = new ArrayList<>(epics.keySet());

        for (Integer id : epicIds) {
            subtasks.values().removeIf(s -> s.getEpicId() == id);
        }
        epics.clear();
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public Epic addEpic(Epic epic) {
       epic.setId(nextId++);
       epics.put(epic.getId(), epic);
        return epic;
    }

    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);

        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allDone = true;
        boolean anyInProgress = false;

        for (int subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) continue;

            TaskStatus status = subtask.getStatus();
            if (status != TaskStatus.DONE) {
                allDone = false;
            }
            if (status == TaskStatus.IN_PROGRESS) {
                allDone = false;
            }
        }
        if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else if (anyInProgress) {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        } else {
            epic.setStatus(TaskStatus.NEW);
        }
    }

    public void updateEpic(Epic updatedEpic) {
        int id = updatedEpic.getId();
        Epic existingEpic = epics.get(id);

        if (existingEpic != null) {
            existingEpic.setName(updatedEpic.getName());
            existingEpic.setDescription(updatedEpic.getDescription());

            updateEpicStatus(existingEpic.getId());
        }
    }


    //All about Subtask
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
        }
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public Subtask addSubtaskByEpic(Subtask subtask) {

        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            System.out.println("error! there is no epic for this task !"
                    + epicId + "not found.");
            return null;
        }

        int id = nextId++;
        subtask.setId(id);
        subtasks.put(subtask.getId(), subtask);

        epic.addSubtaskIds(id);
        updateEpicStatus(epic.getId());

        return subtask;
    }

    public void updateSubtask (Subtask updatedSubtask) {
        int id = updatedSubtask.getId();
        if (subtasks.containsKey(id)) {
            subtasks.put(id, updatedSubtask);
            updateEpicStatus(updatedSubtask.getEpicId());
        }
    }

    public void deleteSubtaskById(int id) {
        Subtask s = subtasks.remove(id);
        if (s != null) {
            Epic epic = epics.get(s.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove(Integer.valueOf(id));
                updateEpicStatus(epic.getId());
            }
        }
    }

    public List<Subtask> getSubtaskOfEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return new ArrayList<>();

        List<Subtask> result = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask s = subtasks.get(subtaskId);
            if (s != null) {
                result.add(s);
            }
        }
        return result;
    }

}
