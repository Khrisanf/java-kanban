package main.java.ru.java_kanban.model;

public class Subtask extends Task {
    private Integer epicId;

    public Subtask(String name,
                   String description,
                   TaskStatus status,
                   int epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public Integer getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
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
