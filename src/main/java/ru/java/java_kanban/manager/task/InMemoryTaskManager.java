package ru.java.java_kanban.manager.task;

import ru.java.java_kanban.manager.history.HistoryManager;
import ru.java.java_kanban.model.Epic;
import ru.java.java_kanban.model.Subtask;
import ru.java.java_kanban.model.Task;
import ru.java.java_kanban.model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryTaskManager implements TaskManager {
    protected final HistoryManager historyManager;
    public Map<Integer, Task> tasks = new HashMap<>();
    public Map<Integer, Epic> epics = new HashMap<>();
    public Map<Integer, Subtask> subtasks = new HashMap<>();
    private int nextId = 1;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    protected void setNextId(int newNextId) {
        this.nextId = newNextId;
    }

    private void recalcEpicTime(Epic epic) {
        var ids = epic.getSubtaskIds();
        if (ids == null || ids.isEmpty()) {
            epic.setDuration(null);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }

        long totalMinutes = ids.stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .mapToLong(d -> d.toMinutes())
                .sum();

        var minStart = ids.stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        var maxEnd = ids.stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        epic.setDuration(totalMinutes > 0 ? Duration.ofMinutes(totalMinutes) : null);
        epic.setStartTime(minStart);
        epic.setEndTime(maxEnd);
    }


    protected void recalcAllEpics() {
        epics.values().stream().forEach(e -> {
            recalcEpicTime(e);
            updateEpicStatus(e.getId());
        });
    }

    private static int compareIds(Integer a, Integer b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        return Integer.compare(a, b);
    }

    private final NavigableSet<Task> priority = new TreeSet<>((a, b) -> {
        boolean aHas = hasSchedule(a);
        boolean bHas = hasSchedule(b);

        if (aHas && bHas) {
            int byStart = a.getStartTime().compareTo(b.getStartTime());
            if (byStart != 0) {
                return byStart;
            }

            int byEnd = endOf(a).compareTo(endOf(b));
            if (byEnd != 0) {
                return byEnd;
            }

            return compareIds(a.getId(), b.getId());
        }

        if (aHas != bHas) {
            return aHas ? -1 : 1;
        }
        return compareIds(a.getId(), b.getId());
    });

    private static boolean hasSchedule(Task t) {
        return t != null && t.getStartTime() != null && t.getDuration() != null;
    }

    private static java.time.LocalDateTime endOf(Task t) {
        return t.getStartTime().plus(t.getDuration());
    }

    private static boolean overlaps(Task x, Task y) {
        return x.getStartTime().isBefore(endOf(y)) && y.getStartTime().isBefore(endOf(x));
    }

    private void indexAdd(Task t) {
        if (hasSchedule(t)) {
            priority.add(t);
        }
    }

    private void indexRemove(Task t) {
        if (hasSchedule(t)) {
            priority.remove(t);
        }
    }

    private void indexReplace(Task oldT, Task newT) {
        boolean removed = false;
        if (oldT != null && hasSchedule(oldT)) {
            removed = priority.remove(oldT);
        }
        try {
            ensureNoOverlapFast(newT);
            indexAdd(newT);
        } catch (RuntimeException ex) {
            if (removed) {
                priority.add(oldT);
            }
            throw ex;
        }
    }

    private void ensureNoOverlapFast(Task candidate) {
        if (!hasSchedule(candidate)) {
            return;
        }

        Task left  = priority.lower(candidate);
        Task right = priority.ceiling(candidate);

        if (left != null && !Objects.equals(left.getId(), candidate.getId()) && overlaps(candidate, left)) {
            throw new IllegalArgumentException("Конфликт расписания слева: " + left);
        }

        if (right != null && !Objects.equals(right.getId(), candidate.getId()) && overlaps(candidate, right)) {
            throw new IllegalArgumentException("Конфликт расписания справа: " + right);
        }
    }


    protected void rebuildPriorityIndex() {
        priority.clear();
        Stream.concat(tasks.values().stream(), subtasks.values().stream())
                .forEach(this::indexAdd);
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
        tasks.values().stream().forEach(this::indexRemove);
        tasks.keySet().stream().forEach(historyManager::remove);
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
        ensureNoOverlapFast(task);
        task.setId(nextId++);
        tasks.put(task.getId(), task);
        indexAdd(task);
        return task;
    }

    @Override
    public void deleteTaskById(Integer id) {
        Task task = tasks.remove(id);
        historyManager.remove(id);
        if (task != null) {
            indexRemove(task);
        }
    }

    @Override
    public void updateTask(Task task) {
        Task old = tasks.get(task.getId());
        if (old == null) {
            return;
        }
        indexReplace(old, task);
        tasks.put(task.getId(), task);
    }

    //All about Epic
    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        epics.values().stream()
                .flatMap(e -> e.getSubtaskIds().stream())
                .toList()
                .forEach(this::deleteSubtaskById);

        epics.keySet().stream().forEach(historyManager::remove);
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
            epic.getSubtaskIds().stream()
                    .toList()
                    .forEach(this::deleteSubtaskById);
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
        subtasks.values().stream().forEach(st -> {
            indexRemove(st);
            historyManager.remove(st.getId());
        });
        subtasks.clear();

        epics.values().stream().forEach(epic -> {
            epic.getSubtaskIds().clear();
            recalcEpicTime(epic);
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

        ensureNoOverlapFast(subtask);
        int id = nextId++;
        subtask.setId(id);

        subtasks.put(subtask.getId(), subtask);
        indexAdd(subtask);

        epic.addSubtaskIds(id);
        recalcEpicTime(epic);
        updateEpicStatus(epic.getId());

        return subtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Subtask oldSubtask = subtasks.get(subtask.getId());
        if (oldSubtask == null) {
            return;
        }
        indexReplace(oldSubtask, subtask);
        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            recalcEpicTime(epic);
            updateEpicStatus(subtask.getEpicId());
        }
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            historyManager.remove(id);
            indexRemove(subtask);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove(id);
                recalcEpicTime(epic);
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
        return new ArrayList<>(priority);
    }
}
