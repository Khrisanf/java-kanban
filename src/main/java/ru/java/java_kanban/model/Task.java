package ru.java.java_kanban.model;

import java.util.Objects;

public class Task {
    private Integer id;
    protected final TaskType type;
    private String name;
    private String description;
    private TaskStatus status;

    public Task(String name, String description, TaskStatus status, TaskType type) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TaskType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Task copy() {
        Task copy = new Task(getName(), getDescription(), status, type);
        copy.setId(this.id);
        return copy;
    }

    public String toCsvString() {
        return getId() + "," + getType() + "," + getName() + "," + getStatus() + "," + getDescription() + ",";
    }


    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
