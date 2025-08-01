package ru.java.java_kanban.model;

public class Subtask extends Task {
    private static Integer epicId;

    public Subtask(String name,
                   String description,
                   TaskStatus status,
                   int epicId) {
        super(name, description, status, TaskType.SUBTASK);
        Subtask.epicId = epicId;
    }

    public static Integer getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        Subtask.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", epicId=" + epicId +
                '}';
    }

    @Override
    public Task copy() {
        Subtask copy = new Subtask(getName(), getDescription(), getStatus(), getEpicId());
        copy.setId(this.getId());
        return copy;
    }
}
