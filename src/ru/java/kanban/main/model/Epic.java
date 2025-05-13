package ru.java.kanban.main.model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
    }

    public void addSubtaskIds(int subtaskId) {
        if (subtaskId != this.getId()) {
            subtaskIds.add(subtaskId);
        }
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void setSubtaskIds(List<Integer> ids) {
        this.subtaskIds = ids;
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
