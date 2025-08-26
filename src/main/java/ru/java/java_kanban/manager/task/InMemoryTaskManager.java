package ru.java.java_kanban.manager.task;

import ru.java.java_kanban.manager.history.HistoryManager;
import ru.java.java_kanban.manager.priority.PriorityIndex;
import ru.java.java_kanban.manager.priority.TreeSetPriorityIndex;
import ru.java.java_kanban.model.Epic;
import ru.java.java_kanban.model.Subtask;
import ru.java.java_kanban.model.Task;
import ru.java.java_kanban.model.TaskStatus;
import ru.java.java_kanban.util.TimeUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryTaskManager implements TaskManager {
    protected final HistoryManager historyManager;
    public Map<Integer, Task> tasks = new HashMap<>();
    public Map<Integer, Epic> epics = new HashMap<>();
    public Map<Integer, Subtask> subtasks = new HashMap<>();
    private int nextId = 1;
    private PriorityIndex priorityIndex = new TreeSetPriorityIndex();
    public TimeUtils timeUtils = new TimeUtils();

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    protected void setNextId(int newNextId) {
        this.nextId = newNextId;
    }

    protected void recalcAllEpics() {
        epics.values().forEach(e -> {
            e.recalcFromSubtasks(subtasks);
            updateEpicStatus(e.getId());
        });
    }

    protected void rebuildPriorityIndex() {
        priorityIndex.clearAll();
        priorityIndex.clearAll();
        Stream.concat(tasks.values().stream(), subtasks.values().stream())
                .forEach(priorityIndex::add);
    }

    protected void postLoadReinit() {
        recalcAllEpics();
        rebuildPriorityIndex();
    }

    //All about Task
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.keySet().forEach(historyManager::remove);
        tasks.clear();
        priorityIndex.clearAll();
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
        if (task.getStartTime() != null && task.getEndTime() != null) {
            boolean overlapWithTasks = tasks.values().stream().anyMatch(existing ->
                    timeUtils.isOverlapping(task.getStartTime(), task.getDuration(),
                            existing.getStartTime(), existing.getDuration()));
            if (overlapWithTasks) {
                throw new IllegalArgumentException("tasks are overlapping");
            }

            boolean overlapWithSubtasks = subtasks.values().stream().anyMatch(st ->
                    timeUtils.isOverlapping(task.getStartTime(), task.getDuration(),
                            st.getStartTime(), st.getDuration()));
            if (overlapWithSubtasks) {
                throw new IllegalArgumentException("subtask and task are overlapping");
            }
        }

        task.setId(nextId++);
        tasks.put(task.getId(), task);
        priorityIndex.add(task);
        return task;
    }

    @Override
    public void deleteTaskById(Integer id) {
        Task task = tasks.remove(id);
        historyManager.remove(id);
        if (task != null) {
            priorityIndex.remove(task);
        }
    }

    @Override
    public void updateTask(Task task) {
        Task old = tasks.get(task.getId());
        if (old == null) {
            return;
        }

        if (!(task.getId().equals(old.getId()))
                && task.getStartTime() != null
                && task.getDuration() != null) {
            boolean overlapTasks = tasks.values().stream().anyMatch(existing ->
                    timeUtils.isOverlapping(old.getStartTime(), old.getDuration(),
                            existing.getStartTime(), existing.getDuration()));
            if (overlapTasks) {
                throw new IllegalArgumentException("tasks are overlapping");
            }

            boolean overlapSubtasks = subtasks.values().stream().anyMatch(st ->
                    timeUtils.isOverlapping(task.getStartTime(), task.getDuration(),
                            st.getStartTime(), st.getDuration()));
            if (overlapSubtasks) {
                throw new IllegalArgumentException("subtask and task are overlapping");
            }
        }

        priorityIndex.replace(old, task);
        tasks.put(task.getId(), task);
    }

    //All about Epic
    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        new ArrayList<>(epics.values()).forEach(epic -> {
            new ArrayList<>(epic.getSubtaskIds()).forEach(this::deleteSubtaskById);
            historyManager.remove(epic.getId());
        });
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
            epic.getSubtaskIds().forEach(this::deleteSubtaskById);
            historyManager.remove(id);
        }
    }

    void updateEpicStatus(Integer epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = subtaskIds.stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .allMatch(st -> st.getStatus() == TaskStatus.NEW);

        boolean allDone = subtaskIds.stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .allMatch(st -> st.getStatus() == TaskStatus.DONE);

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
        subtasks.values().forEach(st -> {
            priorityIndex.remove(st);
            historyManager.remove(st.getId());
        });
        subtasks.clear();

        epics.values().forEach(epic -> {
            epic.getSubtaskIds().clear();
            epic.recalcFromSubtasks(subtasks);
            updateEpicStatus(epic.getId());
            historyManager.remove(epic.getId());
        });
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
            System.out.println("error! there is no epic for this task !" +
                    epicId + "not found.");
            return null;
        }

        int id = nextId++;
        subtask.setId(id);
        if (subtask.getStartTime() != null && subtask.getEndTime() != null) {
            boolean overlapSubs = subtasks.values().stream().anyMatch(ex ->
                    timeUtils.isOverlapping(subtask.getStartTime(), subtask.getDuration(),
                            ex.getStartTime(), ex.getDuration()));
            if (overlapSubs) {
                throw new IllegalArgumentException("subtasks are overlapping");
            }

            boolean overlapTasks2 = tasks.values().stream().anyMatch(t ->
                    timeUtils.isOverlapping(subtask.getStartTime(), subtask.getDuration(),
                            t.getStartTime(), t.getDuration()));
            if (overlapTasks2) {
                throw new IllegalArgumentException("subtasks and task are overlapping");
            }
        }

        subtasks.put(subtask.getId(), subtask);
        priorityIndex.add(subtask);

        epic.addSubtaskIds(id);
        epic.recalcFromSubtasks(subtasks);
        updateEpicStatus(epic.getId());

        return subtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Subtask oldSubtask = subtasks.get(subtask.getId());
        if (oldSubtask == null) {
            return;
        }

        if (!(subtask.getId().equals(oldSubtask.getId()))
                && subtask.getStartTime() != null
                && subtask.getDuration() != null) {
            boolean overlapSubs = subtasks.values().stream().anyMatch(ex ->
                    timeUtils.isOverlapping(subtask.getStartTime(), subtask.getDuration(),
                            ex.getStartTime(), ex.getDuration()));
            if (overlapSubs) {
                throw new IllegalArgumentException("subtasks are overlapping");
            }

            boolean overlapTasks3 = tasks.values().stream().anyMatch(t ->
                    timeUtils.isOverlapping(subtask.getStartTime(), subtask.getDuration(),
                            t.getStartTime(), t.getDuration()));
            if (overlapTasks3) {
                throw new IllegalArgumentException("subtasks and task are overlapping");
            }
        }

        priorityIndex.replace(oldSubtask, subtask);
        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.recalcFromSubtasks(subtasks);
            updateEpicStatus(subtask.getEpicId());
        }
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            historyManager.remove(id);
            priorityIndex.remove(subtask);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove(id);
                epic.recalcFromSubtasks(subtasks);
                updateEpicStatus(epic.getId());
            }
        }
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(Integer epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }

        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return priorityIndex.asList();
    }
}
