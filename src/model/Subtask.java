package model;

public class Subtask extends Task {
    private Epic epic;

    public Epic getEpic() {
        return epic;
    }

    public Subtask(int id,
                   String name,
                   String description,
                   TaskStatus status,
                   Epic epic
    ) {
        super(id, name, description, status);
        this.epic = epic;
    }

    @Override
    public void setStatus(TaskStatus status) {
        super.setStatus(status);
        epic.updateStatus();
    }
}
