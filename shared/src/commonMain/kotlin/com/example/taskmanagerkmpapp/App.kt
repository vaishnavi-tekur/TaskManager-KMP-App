package com.example.taskmanagerkmpapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*

@Composable
fun App() {
    val authRepository = remember { MockAuthRepository }
    var currentScreen by remember { mutableStateOf("login") }
    var loggedInUser by remember { mutableStateOf<UserProfile?>(null) }
    var userTasks by remember {
        mutableStateOf(
            AppDataStorage.loadTasks().ifEmpty {
                mapOf(
                    "admin" to listOf(
                        Task(1, "Complete Project Proposal", "Finalize the draft for the TaskManager project and send it to the team.", "High"),
                        Task(2, "Buy Groceries", "Milk, Eggs, Bread, and Fruits.", "Medium"),
                        Task(3, "Workout", "Morning cardio and strength training.", "Low")
                    )
                )
            }
        )
    }

    fun saveTasksForUser(username: String, tasks: List<Task>) {
        userTasks = userTasks + (username to tasks)
        AppDataStorage.saveTasks(userTasks)
    }

    val currentUserTasks = loggedInUser?.username?.let { userTasks[it] } ?: emptyList()

    MaterialTheme {
        when (currentScreen) {
            "login" -> LoginScreen(
                authRepository = authRepository,
                onLoginSuccess = { user ->
                    loggedInUser = user
                    currentScreen = "profile"
                }
            )
            "profile" -> ProfileScreen(
                user = loggedInUser ?: UserProfile(
                    id = "guest",
                    username = "guest",
                    name = "Guest User",
                    email = "guest@example.com"
                ),
                onContinue = { currentScreen = "taskList" },
                onLogout = {
                    loggedInUser = null
                    currentScreen = "login"
                }
            )
            "taskList" -> TaskListScreen(
                tasks = currentUserTasks,
                userName = loggedInUser?.name ?: "User",
                onAddTaskClick = { currentScreen = "addTask" },
                onToggleTask = { taskId ->
                    val username = loggedInUser?.username ?: return@TaskListScreen
                    val updatedList = (userTasks[username] ?: emptyList()).map { task ->
                        if (task.id == taskId) task.copy(isCompleted = !task.isCompleted) else task
                    }
                    saveTasksForUser(username, updatedList)
                },
                onDeleteTask = { taskId ->
                    val username = loggedInUser?.username ?: return@TaskListScreen
                    val updatedList = (userTasks[username] ?: emptyList()).filter { it.id != taskId }
                    saveTasksForUser(username, updatedList)
                }
            )
            "addTask" -> AddTaskScreen(
                onSave = { title, description, priority ->
                    val username = loggedInUser?.username ?: return@AddTaskScreen
                    val existing = userTasks[username] ?: emptyList()
                    if (title.isNotBlank() && description.isNotBlank()) {
                        val newTask = Task(
                            id = (existing.maxOfOrNull { it.id } ?: 0) + 1,
                            title = title,
                            description = description,
                            priority = priority
                        )
                        saveTasksForUser(username, existing + newTask)
                        currentScreen = "taskList"
                    }
                },
                onBack = { currentScreen = "taskList" }
            )
            else -> TaskListScreen(
                tasks = currentUserTasks,
                userName = loggedInUser?.name ?: "User",
                onAddTaskClick = { currentScreen = "addTask" },
                onToggleTask = { taskId ->
                    val username = loggedInUser?.username ?: return@TaskListScreen
                    val updatedList = (userTasks[username] ?: emptyList()).map { task ->
                        if (task.id == taskId) task.copy(isCompleted = !task.isCompleted) else task
                    }
                    saveTasksForUser(username, updatedList)
                },
                onDeleteTask = { taskId ->
                    val username = loggedInUser?.username ?: return@TaskListScreen
                    val updatedList = (userTasks[username] ?: emptyList()).filter { it.id != taskId }
                    saveTasksForUser(username, updatedList)
                }
            )
        }
    }
}
