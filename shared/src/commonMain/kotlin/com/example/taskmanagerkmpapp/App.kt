package com.example.taskmanagerkmpapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*

@Composable
fun App() {
    val authRepository = remember { MockAuthRepository() }
    var currentScreen by remember { mutableStateOf("login") }
    var loggedInUser by remember { mutableStateOf<UserProfile?>(null) }
    var tasks by remember {
        mutableStateOf(
            listOf(
                Task(1, "Complete Project Proposal", "Finalize the draft for the TaskManager project and send it to the team.", "High"),
                Task(2, "Buy Groceries", "Milk, Eggs, Bread, and Fruits.", "Medium"),
                Task(3, "Workout", "Morning cardio and strength training.", "Low"),
                Task(4, "Call Mom", "Catch up with family in the evening.", "Medium")
            )
        )
    }

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
                tasks = tasks,
                onAddTaskClick = { currentScreen = "addTask" },
                onToggleTask = { taskId ->
                    tasks = tasks.map { task ->
                        if (task.id == taskId) task.copy(isCompleted = !task.isCompleted) else task
                    }
                },
                onDeleteTask = { taskId ->
                    tasks = tasks.filter { it.id != taskId }
                }
            )
            "addTask" -> AddTaskScreen(
                onSave = { title, description, priority ->
                    if (title.isNotBlank() && description.isNotBlank()) {
                        tasks = tasks + Task(
                            id = (tasks.maxOfOrNull { it.id } ?: 0) + 1,
                            title = title,
                            description = description,
                            priority = priority
                        )
                        currentScreen = "taskList"
                    }
                },
                onBack = { currentScreen = "taskList" }
            )
            else -> TaskListScreen(
                tasks = tasks,
                onAddTaskClick = { currentScreen = "addTask" },
                onToggleTask = { taskId ->
                    tasks = tasks.map { task ->
                        if (task.id == taskId) task.copy(isCompleted = !task.isCompleted) else task
                    }
                },
                onDeleteTask = { taskId ->
                    tasks = tasks.filter { it.id != taskId }
                }
            )
        }
    }
}
