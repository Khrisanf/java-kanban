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

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private int nextId = 1;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    public Map<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public Map<Integer, Epic> getEpics() {
        return epics;
    }

    public Map<Integer, Task> getTasks() {
        return tasks;
    }

    protected void setNextId(int newNextId) {
        this.nextId = newNextId;
    }

    // сбор существующих сабтасок у эпика
    private List<Subtask> epicSubtasks(Epic epic) {
        var ids = epic.getSubtaskIds();
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .toList();
    }

    // сумма длительностей в минутах
    private long totalMinutes(List<Subtask> sts) {
        return sts.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .mapToLong(Duration::toMinutes)
                .sum();
    }

    // минимальный старт и максимальный конец среди сабтасков
    private LocalDateTime minStart(List<Subtask> sts) {
        return sts.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    private LocalDateTime maxEnd(List<Subtask> sts) {
        return sts.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    // пересчет значений к эпику
    private void applyEpicTime(Epic epic, long minutes, LocalDateTime start, LocalDateTime end) {
        epic.setDuration(minutes > 0 ? Duration.ofMinutes(minutes) : null);
        epic.setStartTime(start);
        epic.setEndTime(end);
    }

    // оркестратор
    private void recalcEpicTime(Epic epic) {
        List<Subtask> sts = epicSubtasks(epic);
        if (sts.isEmpty()) {
            applyEpicTime(epic, 0, null, null);
            return;
        }
        long minutes = totalMinutes(sts);
        LocalDateTime start = minStart(sts);
        LocalDateTime end = maxEnd(sts);
        applyEpicTime(epic, minutes, start, end);
    }


    private static boolean hasOverlaps(Task x, Task y) {
        // пересекаются, если x.start < y.end И y.start < x.end
        // по методу отрезков
        if (!x.hasSchedule() || !y.hasSchedule()) {
            return false;
        }
        return x.getStartTime().isBefore(y.getEndTime())
                && y.getStartTime().isBefore(x.getEndTime());
    }

    // сравнивает кандидата со всеми существующими задачами/сабтасками
    private boolean hasOverlaps(Task candidate) {
        if (!candidate.hasSchedule()) {
            return false;
        }

        return Stream.concat(tasks.values().stream(), subtasks.values().stream())
                .filter(Task::hasSchedule)                             // сравнение только с интервалами
                // не сравнивает с самим собой при обновлении
                .filter(t -> !Objects.equals(t.getId(), candidate.getId()))
                .anyMatch(other -> hasOverlaps(candidate, other)); // использует парный предикат
    }

    // вместо кучи методов с пересчетом индексов теперь
    // просто сбор в список и сортировка внутри
    @Override
    public List<Task> getPrioritizedTasks() {
        return Stream.concat(tasks.values().stream(), subtasks.values().stream())
                .filter(Task::hasSchedule)
                .sorted(
                        Comparator.comparing(Task::getStartTime)
                                .thenComparing(Task::getEndTime)
                                .thenComparingInt(Task::getId)
                )
                .toList();
    }

    //All about Task
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
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
        if (hasOverlaps(task)) {
            throw new IllegalArgumentException("Schedule conflict");
        }
        task.setId(nextId++);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public void deleteTaskById(Integer id) {
        Task task = tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void updateTask(Task task) {
        Task old = tasks.get(task.getId());
        if (old == null) {
            return;
        }
        if (hasOverlaps(task)) {
            throw new IllegalArgumentException("Schedule conflict");
        }
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
            throw new NoSuchElementException("Epic " + epicId + " not found");
        }

        if (hasOverlaps(subtask)) {
            throw new IllegalArgumentException("Schedule conflict");
        }

        int id = nextId++;
        subtask.setId(id);

        subtasks.put(subtask.getId(), subtask);

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

        if (hasOverlaps(subtask)) {
            throw new IllegalArgumentException("Schedule conflict");
        }

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
}
