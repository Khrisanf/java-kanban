package app.model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String name,
                String description,
                TaskStatus status) {
        super(name, description, status);
    }

    public void addSubtaskIds(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void setSubtaskIds(List<Integer> ids) {
        this.subtaskIds = ids;
    }
}
