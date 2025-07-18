package ru.java.java_kanban.manager.task;

import ru.java.java_kanban.model.Epic;
import ru.java.java_kanban.model.Subtask;
import ru.java.java_kanban.model.Task;
import ru.java.java_kanban.model.TaskStatus;
import ru.java.java_kanban.manager.history.HistoryManager;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int nextId = 1;

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }



    //All about Task
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
        }
        tasks.clear();
    }

    @Override
    public Task getTaskById(Integer id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Task addTask(Task task) {
        task.setId(nextId++);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public void deleteTaskById(Integer id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    //All about Epic
    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                historyManager.remove(subtaskId);
                subtasks.remove(subtaskId);
            }
            historyManager.remove(epic.getId());
        }
        epics.clear();
    }

    @Override
    public Epic getEpicById(Integer id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Epic addEpic(Epic epic) {
       epic.setId(nextId++);
       epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public void deleteEpicById(Integer id) {
        Epic epic = epics.remove(id);

        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                deleteSubtaskById(subtaskId);
            }
            historyManager.remove(id);
        }
    }

    private void updateEpicStatus(Integer epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Integer subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) continue;

            TaskStatus status = subtask.getStatus();
            if (status != TaskStatus.NEW) {
                allNew = false;
            }
            if (status != TaskStatus.DONE) {
                allDone = false;
            }
        }

        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }


    @Override
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
    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        for (Integer id : subtasks.keySet()) {
            historyManager.remove(id);
        }

        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
            historyManager.remove(epic.getId());
        }
    }

    @Override
    public Subtask getSubtaskById(Integer id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {

        Integer epicId = subtask.getEpicId();
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

    @Override
    public void updateSubtask(Subtask updatedSubtask) {
        if (subtasks.containsKey(updatedSubtask.getId())) {
            subtasks.put(updatedSubtask.getId(), updatedSubtask);
            updateEpicStatus(updatedSubtask.getEpicId());
        }
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        Subtask s = subtasks.remove(id);
        if (s != null) {
            historyManager.remove(id);
            Epic epic = epics.get(s.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove(id);
                updateEpicStatus(epic.getId());
            }
        }
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(Integer epicId) {
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

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }


}
