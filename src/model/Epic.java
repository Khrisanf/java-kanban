package model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Subtask> subtasks = new ArrayList<>();

    public Epic(int id,
                String name,
                String description,
                TaskStatus status) {
        super(id, name, description, status);
    }

    public void addSubtasks(Subtask subtask) {
        subtasks.add(subtask);
        updateStatus();
    }
    
    public void updateStatus() {
        if (subtasks.isEmpty()) {
            setStatus(TaskStatus.NEW);
            return;
        }

        boolean allDone = true;
        boolean anyInProgress = false;

        for (Subtask subtask : subtasks) {
            TaskStatus subtaskStatus = subtask.getStatus();


            if (!subtaskStatus.equals(TaskStatus.DONE)) {
               allDone = false;
            }

            if (subtaskStatus.equals(TaskStatus.IN_PROGRESS)) {
                anyInProgress = true;
            }
        }

        if(allDone) {
            setStatus(TaskStatus.DONE);
        } else if (anyInProgress) {
            setStatus(TaskStatus.IN_PROGRESS);
        } else {
            setStatus(TaskStatus.NEW);
        }
    }
}
