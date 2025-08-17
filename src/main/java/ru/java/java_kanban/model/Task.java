package ru.java.java_kanban.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class Task {
    private Integer id;
    private String name;
    private String description;
    private TaskStatus status;
    private Duration duration;
    private LocalDateTime startTime;

    public Task(String name, String description, TaskStatus status) {
        this.name = name;
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
        return TaskType.TASK;
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

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Duration getDuration() {
        return duration;
    }

    public Long getDurationMinutes() {
        return duration == null ? null : duration.toMinutes();
    }


    public Optional<Duration> getDurationOptional() {
        return Optional.ofNullable(duration);
    }

    public void setDuration(Duration duration) {
        if (duration != null && duration.isNegative()) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
        this.duration = duration;
    }

    public void setDurationMinutes(Long minutes) {
        if (minutes == null) {
            this.duration = null;
        } else if(minutes < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        } else {
            this.duration = Duration.ofMinutes(minutes);
        }
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    public Task copy() {
        Task copy = new Task(getName(), getDescription(), status);
        copy.setId(this.id);
        copy.setDuration(this.duration);
        copy.setStartTime(this.startTime);
        return copy;
    }


    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", type=" + TaskType.TASK +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", getEndTime=" + getEndTime() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
