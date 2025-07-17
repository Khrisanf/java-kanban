package ru.java.java_kanban;

import ru.java.java_kanban.manager.history.*;
import ru.java.java_kanban.manager.task.*;
import ru.java.java_kanban.model.*;
import ru.java.java_kanban.manager.Managers;

public class Main {

    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // Добавление задач
        System.out.println("=== ДОБАВЛЕНИЕ ЗАДАЧ ===");
        Task task1 = new Task("Moving out", "Pack boxes", TaskStatus.NEW);
        Task task2 = new Task("Studying", "Pass the module about Java", TaskStatus.NEW);
        manager.addTask(task1);
        manager.addTask(task2);

        Epic epic1 = new Epic("Moving out to another town", "Move to Tomsk");
        manager.addEpic(epic1);

        Subtask subtask1 = new Subtask("Pack clothes", "Pack a bag", TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Apartment rental", "Rent for some times", TaskStatus.NEW, epic1.getId());
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        Epic epic2 = new Epic("Session", "Pass deadlines");
        manager.addEpic(epic2);

        Subtask subtask3 = new Subtask("Pass java", "Pass projects", TaskStatus.NEW, epic2.getId());
        manager.addSubtask(subtask3);

        printAll(manager);

        // Обновление статусов
        System.out.println("\n=== ОБНОВЛЕНИЕ СТАТУСОВ ===");
        task1.setStatus(TaskStatus.DONE);
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask1);
        subtask2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask2);
        subtask3.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask3);

        printAll(manager);

        // Тест истории
        System.out.println("\n=== ИСТОРИЯ ПРОСМОТРОВ ===");
        manager.getTaskById(task1.getId());
        printHistory(manager, "После просмотра Task 1");

        manager.getTaskById(task2.getId());
        printHistory(manager, "После просмотра Task 2");

        manager.getTaskById(task1.getId()); // повторный просмотр
        printHistory(manager, "После повторного просмотра Task 1");

        manager.getTaskById(epic2.getId()); // просмотр эпика
        printHistory(manager, "После просмотра Epic 2");


        // Удаление
        System.out.println("\n=== УДАЛЕНИЕ ЗАДАЧ ===");
        manager.deleteTaskById(task2.getId());
        manager.deleteEpicById(epic1.getId());
        printAll(manager);

    }


    private static void printAll(TaskManager manager) {
        System.out.println("\n--- Общие задачи ---");
        manager.getAllTasks().forEach(System.out::println);

        System.out.println("\n--- Эпики ---");
        manager.getAllEpics().forEach(System.out::println);

        System.out.println("\n--- Подзадачи ---");
        manager.getAllSubtasks().forEach(System.out::println);
    }

    private static void printHistory(TaskManager manager, String title) {
        System.out.println("\n--- " + title + " ---");
        System.out.println(manager.getHistory());
    }
}
