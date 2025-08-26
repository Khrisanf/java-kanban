package ru.java.java_kanban.manager.history;

import ru.java.java_kanban.model.Task;

import java.util.*;
import java.util.stream.Stream;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node> historyMap = new HashMap<>();
    private Node head;
    private Node tail;

    private void linkLast(Task task) {
        Node oldTail = tail;
        Node newNode = new Node(oldTail, task, null);
        tail = newNode;
        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }
        historyMap.put(task.getId(), newNode);
    }

    private void removeNode(Node node) {
        if (node == null) {
            return;
        }

        Node prev = node.prev;
        Node next = node.next;

        if (prev != null) {
            prev.next = next;
        } else {
            head = next;
        }

        if (next != null) {
            next.prev = prev;
        } else {
            tail = prev;
        }
        historyMap.remove(node.task.getId());
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        if (historyMap.containsKey(task.getId())) {
            removeNode(historyMap.get(task.getId()));
        }
        linkLast(task.copy());
    }

    @Override
    public List<Task> getHistory() {
        return Stream.iterate(head, Objects::nonNull, node -> node.next)
                .map(node -> node.task)
                .toList();
    }

    @Override
    public void remove(int id) {
        Node node = historyMap.remove(id);
        removeNode(node);
    }

    private static class Node {
        private final Task task;
        private Node prev;
        private Node next;

        public Node(Node prev, Task task, Node next) {
            this.prev = prev;
            this.task = task;
            this.next = next;
        }
    }
}

