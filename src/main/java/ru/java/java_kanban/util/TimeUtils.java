package ru.java.java_kanban.util;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeUtils {

    public TimeUtils() {}

    public boolean isOverlapping(LocalDateTime start, Duration duration,
                                LocalDateTime start2, Duration duration2) {
        if (start == null || duration == null || start2 == null || duration2 == null) {
            return false;
        }
        LocalDateTime end1 = start.plus(duration);
        LocalDateTime end2 = start2.plus(duration2);
        return start.isBefore(end2) && start2.isBefore(end1);
    }
}
