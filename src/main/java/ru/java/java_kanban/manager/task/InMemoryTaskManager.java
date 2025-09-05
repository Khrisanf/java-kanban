package ru.java.java_kanban.manager.task;

import ru.java.java_kanban.manager.history.HistoryManager;
import ru.java.java_kanban.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryTaskManager implements TaskManager {
    protected final HistoryManager historyManager;

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    private int nextId = 1;
    private final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime)
                    .thenComparing(Task::getEndTime)
                    .thenComparingInt(Task::getId)
    );

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    protected void setNextId(int newNextId) {
        this.nextId = newNextId;
    }

    // сумма длительностей
    private Duration getTotalDuration(List<Subtask> sts) {
        boolean anyDuration = sts.stream()
                .anyMatch(st -> st != null
                        && st.getDuration() != null);
        if (!anyDuration) {
            return null;
        }
        long minutes = sts.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .mapToLong(Duration::toMinutes)
                .sum();
        return Duration.ofMinutes(minutes);
    }


    // минимальный старт и максимальный конец среди сабтасков
    private LocalDateTime getMinStart(List<Subtask> sts) {
        return sts.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    private LocalDateTime getMaxEnd(List<Subtask> sts) {
        return sts.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    private TaskStatus calculateEpicStatus(List<Subtask> sts) {
        if (sts == null || sts.isEmpty()) {
            return TaskStatus.NEW;               // пустой эпик = NEW
        }

        boolean allNew  = sts.stream().allMatch(s -> s.getStatus() == TaskStatus.NEW);
        if (allNew) {
            return TaskStatus.NEW;               // все NEW
        }

        boolean allDone = sts.stream().allMatch(s -> s.getStatus() == TaskStatus.DONE);
        if (allDone) {
            return TaskStatus.DONE;              // все DONE
        }

        // хотя бы один IN_PROGRESS
        return TaskStatus.IN_PROGRESS;
    }


    // оркестратор
    protected void recalcEpic(Epic epic) {
        List<Subtask> sts = getSubtasksOfEpic(epic.getId());
        epic.setDuration(getTotalDuration(sts));
        epic.setStartTime(getMinStart(sts));
        epic.setEndTime(getMaxEnd(sts));
        epic.setStatus(calculateEpicStatus(sts));
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

    //All about Task
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        prioritizedTasks.removeIf(t -> t.getType() == TaskType.TASK);
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
        if (task.hasSchedule()) {
            prioritizedTasks.add(task);
        }
        return task;
    }

    @Override
    public void deleteTaskById(Integer id) {
        Task task = tasks.remove(id);
        if (task.hasSchedule()) {
            prioritizedTasks.remove(task);
        }
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
        if (old.hasSchedule()) {
            prioritizedTasks.remove(old);
        }
        tasks.put(task.getId(), task);
        if (task.hasSchedule()) {
            prioritizedTasks.add(task);
        }
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

    @Override
    public void updateEpic(Epic updatedEpic) {
        int id = updatedEpic.getId();
        Epic existingEpic = epics.get(id);

        if (existingEpic != null) {
            existingEpic.setName(updatedEpic.getName());
            existingEpic.setDescription(updatedEpic.getDescription());

            recalcEpic(existingEpic);
        }
    }


    //All about Subtask
    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        prioritizedTasks.removeIf(t -> t.getType() == TaskType.SUBTASK);
        subtasks.values().stream().forEach(st -> {
            historyManager.remove(st.getId());
        });
        subtasks.clear();

        epics.values().stream().forEach(epic -> {
            epic.getSubtaskIds().clear();
            recalcEpic(epic);
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
        if (subtask.hasSchedule()) {
            prioritizedTasks.add(subtask);
        }

        epic.addSubtaskIds(id);
        recalcEpic(epic);

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

        if (oldSubtask.hasSchedule()) {
            prioritizedTasks.remove(oldSubtask);
        }
        subtasks.put(subtask.getId(), subtask);
        if (subtask.hasSchedule()) {
            prioritizedTasks.add(subtask);
        }

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            recalcEpic(epic);
        }
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        Subtask removed = subtasks.remove(id);
        if (removed != null) {
            if (removed.hasSchedule()) {
                prioritizedTasks.remove(removed);
            }
            historyManager.remove(id);
            Epic epic = epics.get(removed.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove(id);
                recalcEpic(epic);
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
    public Set<Task> prioritizedTasks() {
        return prioritizedTasks;
    }
}
