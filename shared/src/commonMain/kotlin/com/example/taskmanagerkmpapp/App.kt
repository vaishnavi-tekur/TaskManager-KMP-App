package com.example.taskmanagerkmpapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun App() {
    val storage = remember { sessionStorage() }
    var user by remember { mutableStateOf<User?>(null) }
    var screen by remember { mutableStateOf("login") }
    var items by remember { mutableStateOf(emptyList<Task>()) }
    val scope = rememberCoroutineScope()
    val blue = Color(0xFF1A237E)

    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = Color(0xFFF5F5F5)) {
            when (screen) {
                "tasks" -> {
                    BackHandler { screen = "login" }
                    TaskListScreen(user, items, blue, scope, { screen = it }, { items = it; storage.saveTasks(Json.encodeToString(it)) })
                }
                "addTask" -> {
                    BackHandler { screen = "tasks" }
                    AddTaskScreen(blue, scope, { screen = it }, { items = it; storage.saveTasks(Json.encodeToString(it)) })
                }
                "forgotPassword" -> {
                    BackHandler { screen = "login" }
                    ForgotPasswordScreen(blue, scope, { screen = it })
                }
                "register" -> {
                    BackHandler { screen = "login" }
                    AuthScreen(screen, blue, scope, storage, { screen = it }, { u, tasks ->
                        user = u
                        items = tasks
                        storage.saveTasks(Json.encodeToString(tasks))
                        screen = "tasks"
                    })
                }
                else -> AuthScreen(screen, blue, scope, storage, { screen = it }, { u, tasks ->
                    user = u
                    items = tasks
                    storage.saveTasks(Json.encodeToString(tasks))
                    screen = "tasks"
                })
            }
        }
    }
}
