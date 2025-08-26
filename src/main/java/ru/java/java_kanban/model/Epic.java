package ru.java.java_kanban.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class Epic extends Task {
    private List<Integer> subtaskIds = new ArrayList<>();
    private Duration duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;


    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
    }

    public void addSubtaskIds(int subtaskId) {
        if (subtaskId == this.getId()) {
            return;
        }
        if (!subtaskIds.contains(subtaskId)) {
            subtaskIds.add(subtaskId);
        }
    }

    public void recalcFromSubtasks(Map<Integer, Subtask> subtasks) {
        long totalMinutes = 0;
        LocalDateTime minStart = null;
        LocalDateTime maxEnd = null;

        if (subtasks != null && !subtaskIds.isEmpty()) {
            totalMinutes = subtaskIds.stream()
                    .map(subtasks::get)
                    .filter(Objects::nonNull)
                    .map(Subtask::getDuration)
                    .filter(Objects::nonNull)
                    .mapToLong(Duration::toMinutes)
                    .sum();

            minStart = subtaskIds.stream()
                    .map(subtasks::get)
                    .filter(Objects::nonNull)
                    .map(Subtask::getStartTime)
                    .filter(Objects::nonNull)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);

            maxEnd = subtaskIds.stream()
                    .map(subtasks::get)
                    .filter(Objects::nonNull)
                    .map(Subtask::getEndTime)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
        }

        this.duration = (totalMinutes > 0)
                ? Duration.ofMinutes(totalMinutes)
                : null;
        this.startTime = minStart;
        this.endTime = maxEnd;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void setSubtaskIds(List<Integer> ids) {
        this.subtaskIds = ids;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public Long getDurationMinutes() {
        return duration == null ? null : duration.toMinutes();
    }

    @Override
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds +
                '}';
    }

    @Override
    public Task copy() {
        Epic copy = new Epic(getName(), getDescription());
        copy.setId(this.getId());
        copy.setStatus(getStatus());
        copy.setSubtaskIds(new ArrayList<>(getSubtaskIds()));
        copy.setStartTime(getStartTime());
        copy.setDuration(getDuration());
        return copy;
    }
}
