package ru.java.java_kanban;

import ru.java.java_kanban.manager.history.InMemoryHistoryManager;
import ru.java.java_kanban.manager.task.FileBackedTaskManager;
import ru.java.java_kanban.manager.task.TaskManager;
import ru.java.java_kanban.model.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

public class Main {

    // ---------- простенький «логгер» ----------
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static void log(String fmt, Object... args) {
        String ts = LocalDateTime.now().format(TS);
        System.out.println("[" + ts + "] " + String.format(fmt, args));
    }
    private static void banner(String title) {
        String line = "─".repeat(Math.max(8, title.length() + 4));
        System.out.println("\n" + line);
        System.out.println("  " + title);
        System.out.println(line);
    }
    private static String fmtTask(Task t) {
        String start = t.getStartTime() == null ? "—" : t.getStartTime().toString();
        String end   = t.getEndTime()   == null ? "—" : t.getEndTime().toString();
        Long durMin  = t.getDurationMinutes();
        String dur   = durMin == null ? "—" : (durMin + "m");
        return String.format("#%d %-8s %-16s [%s] start=%s end=%s dur=%s",
                t.getId(), t.getType(), t.getName(), t.getStatus(), start, end, dur);
    }
    private static void printAll(TaskManager m) {
        banner("Текущие данные");
        System.out.println("\n— Общие задачи —");
        m.getAllTasks().forEach(t -> System.out.println("  " + fmtTask(t)));

        System.out.println("\n— Эпики —");
        m.getAllEpics().forEach(e -> {
            System.out.println("  " + fmtTask(e));
            List<Subtask> subs = m.getSubtasksOfEpic(e.getId());
            if (!subs.isEmpty()) {
                for (Subtask s : subs) {
                    System.out.println("     ↳ " + fmtTask(s));
                }
            }
        });

        System.out.println("\n— Подзадачи (все) —");
        m.getAllSubtasks().forEach(s -> System.out.println("  " + fmtTask(s)));

        System.out.println("\n— Приоритизированный список —");
        Set<Task> pr = m.prioritizedTasks();
        if (pr.isEmpty()) {
            System.out.println("  (пусто: нет задач со startTime)");
        } else {
            pr.forEach(t -> System.out.println("  " + fmtTask(t)));
        }
    }
    private static void printHistory(TaskManager m, String title) {
        banner("История: " + title);
        m.getHistory().forEach(t -> System.out.println("  " + fmtTask(t)));
    }

    // ---------- demo-сценарий ----------
    public static void main(String[] args) {
        Path file = Path.of("tasks.csv");
        TaskManager manager = new FileBackedTaskManager(new InMemoryHistoryManager(), file);

        if (Files.notExists(file)) {
            log("Файл %s не найден. Запускаю сценарий заполнения…", file);
            createScenario(manager);
        } else {
            log("Файл %s найден. Перезагружаю задачи из файла…", file);
            printAll(manager);
            printHistory(manager, "после перезагрузки");
        }
        log("Готово.");
    }

    private static void createScenario(TaskManager manager) {
        banner("ДОБАВЛЕНИЕ ЗАДАЧ");

        Task task1 = new Task("Moving out", "Pack boxes", TaskStatus.NEW);
        task1.setStartTime(LocalDateTime.of(2025, 8, 20, 10, 0));
        task1.setDurationMinutes(90L);
        manager.addTask(task1);
        log("Добавлена задача: %s", fmtTask(task1));

        Task task2 = new Task("Studying", "Pass the module about Java", TaskStatus.NEW);
        task2.setStartTime(LocalDateTime.of(2025, 8, 21, 10, 0));
        task2.setDurationMinutes(90L);
        manager.addTask(task2);
        log("Добавлена задача: %s", fmtTask(task2));

        Epic epic1 = new Epic("Moving out to another town", "Move to Tomsk");
        manager.addEpic(epic1);
        log("Добавлен эпик: %s", fmtTask(epic1));

        Subtask subtask1 = new Subtask("Pack clothes", "Pack a bag", TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Apartment rental", "Rent for some times", TaskStatus.NEW, epic1.getId());
        manager.addSubtask(subtask1);
        log("Добавлена подзадача: %s (эпик #%d)", fmtTask(subtask1), epic1.getId());
        manager.addSubtask(subtask2);
        log("Добавлена подзадача: %s (эпик #%d)", fmtTask(subtask2), epic1.getId());

        Epic epic2 = new Epic("Session", "Pass deadlines");
        manager.addEpic(epic2);
        log("Добавлен эпик: %s", fmtTask(epic2));

        Subtask subtask3 = new Subtask("Pass java", "Pass projects", TaskStatus.NEW, epic2.getId());
        subtask3.setStartTime(LocalDateTime.of(2025, 8, 22, 10, 0));
        subtask3.setDurationMinutes(70L);
        manager.addSubtask(subtask3);
        log("Добавлена подзадача: %s (эпик #%d)", fmtTask(subtask3), epic2.getId());

        printAll(manager);

        banner("ОБНОВЛЕНИЕ СТАТУСОВ");
        task1.setStatus(TaskStatus.DONE);
        log("Изменён статус: #%d → %s", task1.getId(), task1.getStatus());

        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask1);
        log("Обновлена подзадача: %s", fmtTask(subtask1));

        subtask2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask2);
        log("Обновлена подзадача: %s", fmtTask(subtask2));

        subtask3.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask3);
        log("Обновлена подзадача: %s", fmtTask(subtask3));

        printAll(manager);

        banner("ИСТОРИЯ ПРОСМОТРОВ");
        manager.getTaskById(task1.getId());
        printHistory(manager, "после просмотра Task 1");

        manager.getTaskById(task2.getId());
        printHistory(manager, "после просмотра Task 2");

        manager.getTaskById(task1.getId()); // повторный просмотр
        printHistory(manager, "после повторного просмотра Task 1");

        manager.getTaskById(epic2.getId()); // просмотр эпика
        printHistory(manager, "после просмотра Epic 2");

        banner("УДАЛЕНИЕ ЗАДАЧ");
        manager.deleteTaskById(task2.getId());
        log("Удалена задача #%d", task2.getId());

        manager.deleteEpicById(epic1.getId());
        log("Удалён эпик #%d и все его подзадачи", epic1.getId());

        printAll(manager);
    }
}
