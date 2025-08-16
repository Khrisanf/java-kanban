package ru.java.java_kanban.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public void recalcFromSubtasks(List<Subtask> subtasks) {
        long totalMinutes = 0;
        LocalDateTime minStart = null;
        LocalDateTime maxEnd = null;

        if (subtasks != null) {
            for (Subtask subtask : subtasks) {
                if (subtask.getDuration() != null) {
                    totalMinutes += subtask.getDuration().toMinutes();
                }

                if (subtask.getStartTime() != null) {
                    if (minStart == null || minStart.isAfter(subtask.getStartTime())) {
                        minStart = subtask.getStartTime();
                    }
                }

                if (subtask.getEndTime() != null) {
                    if (maxEnd == null || maxEnd.isBefore(subtask.getEndTime())) {
                        maxEnd = subtask.getEndTime();
                    }
                }
            }
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
        return copy;
    }
}
