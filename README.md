# 🗂 Java Task Tracker (Kanban-style)

## 📌 Description

A Java-based Kanban-style task tracker designed for educational purposes.  
Supports structured management of tasks, epics, and subtasks with history tracking and modular architecture.

## 🧩 Key Features

- 👤 CRUD operations for:
  - ✅ **Tasks**
  - 🧩 **Epics** — container tasks that include subtasks
  - 🔹 **Subtasks** — associated with epics
- 🧠 **Automatic Epic status updates** based on subtasks
- 🆔 **Unique ID** generation for all task types
- 🧾 **View history** of the last 10 accessed tasks
- 🧪 **Unit-tested logic** with JUnit 5
- 💾 **In-memory storage** using HashMaps
- 🧱 **Modular architecture** with interfaces:
  - `TaskManager`, `HistoryManager` for flexibility and testability

## 🛠 Technologies Used

- Java 23
- Maven (Project Management + Build)
- JUnit 5 (Testing)

## 🧪 Test Coverage

Tested classes include:

- `Task`, `Epic`, `Subtask` models
- `InMemoryTaskManager` — task management logic
- `InMemoryHistoryManager` — fixed-size task access history
- Edge cases: nulls, invalid IDs, reference independence, etc.


Made with ☕ and Java :)

Run tests using:
```bash
mvn test


