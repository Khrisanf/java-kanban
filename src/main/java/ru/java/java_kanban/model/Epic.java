package ru.java.java_kanban.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds = new ArrayList<>();
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
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
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
        copy.setEndTime(getEndTime());
        copy.setDuration(getDuration());
        return copy;
    }
}
