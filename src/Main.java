import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import service.TaskManager;

import javax.sound.midi.Soundbank;

public class Main {

    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        //common taks
        Task task1 = new Task(0, "Moving out", "Pack boxes", TaskStatus.NEW);
        Task task2 = new Task(0, "Studying", "Pass the module about Java", TaskStatus.NEW);
        manager.addTask(task1);
        manager.addTask(task2);

        //create epic with 2 subtasks
        Epic epic1 = new Epic(0, "Moving out to another town", "Move to Tomsk", TaskStatus.NEW);
        manager.addEpic(epic1);

        Subtask subtask1 = new Subtask(0, "Pack clothes", "Pack a bag", TaskStatus.NEW, epic1);
        Subtask subtask2 = new Subtask(0, "Apartment rental", "Rent for some times", TaskStatus.NEW, epic1);
        manager.addSubtaskByEpic(subtask1);
        manager.addSubtaskByEpic(subtask2);

        //create epic with 1 subtask
        Epic epic2 = new Epic(0, "Session", "Pass deadlines", TaskStatus.NEW);
        manager.addEpic(epic2);
        Subtask subtask3 = new Subtask(0, "Pass java", "Pass projects", TaskStatus.NEW, epic2);
        manager.addSubtaskByEpic(subtask3);

        //print all tasks
        System.out.println("Common tasks: ");
        manager.getAllTasks().forEach(System.out::println);

        System.out.println("Epics: ");
        manager.getAllEpics().forEach(System.out::println);

        System.out.println("Subtasks: ");
        manager.getAllSubtasks().forEach(System.out::println);

        //update statuses
        task1.setStatus(TaskStatus.DONE);
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        subtask2.setStatus(TaskStatus.DONE);
        subtask3.setStatus(TaskStatus.DONE);

        System.out.println("\n After updating of statuses: ");
        System.out.println("Task 1: " + task1);
        System.out.println("Subtask 1: " + subtask1);
        System.out.println("Subtask 2: " + subtask2);
        System.out.println("Subtask 3: " + subtask3);
        System.out.println("Epic 1: " + epic1);
        System.out.println("Epic 2: " + epic2);

        //delete task and epic
        manager.deleteTaskById(task2.getId());
        manager.deleteEpicById(epic1.getId());

        System.out.println("\n After deleting task and epic: ");
        System.out.println("Tasks:");
        manager.getAllTasks().forEach(System.out::println);
        System.out.println("Epics: ");
        manager.getAllEpics().forEach(System.out::println);
        System.out.println("Subtasks: ");
        manager.getAllSubtasks().forEach(System.out::println);
    }
}
