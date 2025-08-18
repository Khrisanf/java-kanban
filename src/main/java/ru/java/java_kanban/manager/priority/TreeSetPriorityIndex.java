package ru.java.java_kanban.manager.priority;

import ru.java.java_kanban.model.Task;
import ru.java.java_kanban.model.TaskType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

public class TreeSetPriorityIndex implements  PriorityIndex {

    //COMPARATOR
    private final NavigableSet<Task> taskSet = new TreeSet<Task>((aTask, bTask) -> {
        LocalDateTime time1 = aTask.getStartTime();
        LocalDateTime time2 = bTask.getStartTime();

        if (time1 != null && time2 != null) {
            int cmpTime = time1.compareTo(time2);
            if (cmpTime != 0) {
                return cmpTime;
            }
        } else if (time1 != null) {
            return -1;
        } else if (time2 != null) {
            return 1;
        }
        return Integer.compare(aTask.getId(), bTask.getId());
    });

    private boolean isEligible (Task task) {
        return task != null &&
                task.getType() != TaskType.EPIC &&
                task.getStartTime() != null;
    }

    @Override
    public void remove(Task task) {
        if (task != null) {
            taskSet.remove(task);
        }
    }

    @Override
    public void clearAll() {
        taskSet.clear();
    }

    @Override
    public void replace(Task oldTask, Task newTask) {
        if (isEligible(oldTask)  && oldTask != null) {
            taskSet.remove(oldTask);
        }
        if (isEligible(newTask)) {
            taskSet.add(newTask);
        }
    }

    @Override
    public void add(Task task) {
        if (isEligible(task)) {
            taskSet.add(task);
        }
    }

    @Override
    public List<Task> asList() {
        return new ArrayList<>(taskSet);
    }
}
