package com.example.taskmanagerkmpapp
import androidx.compose.foundation.layout.fillMaxSize; import androidx.compose.material3.*; import androidx.compose.runtime.*; import androidx.compose.ui.Modifier; import androidx.compose.ui.graphics.Color; import kotlinx.coroutines.launch
@Composable fun App() {
    val storage = remember { sessionStorage() }; var user by remember { mutableStateOf<User?>(null) }; var screen by remember { mutableStateOf("login") }
    LaunchedEffect(Unit) {
        val t = storage.read("token"); val e = storage.read("email"); val n = storage.read("name"); val u = storage.read("user")
        if (t.isNotEmpty() && e.endsWith("@bhrish.com")) { Repo.token = t; user = User(n, u, e); screen = "taskList" }
    }
    val scope = rememberCoroutineScope(); val blue = Color(0xFF1A237E)
    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = Color(0xFFF5F5F5)) {
            when (screen) {
                "taskList" -> TaskListScreen(user, blue, scope, { storage.save("", "", "", ""); Repo.token = ""; user = null; screen = "login" }, { screen = "addTask" }, { scope.launch { if (Repo.deleteAccount()) { storage.save("", "", "", ""); Repo.token = ""; user = null; screen = "login" } } })
                "addTask" -> AddTaskScreen(blue, scope, { screen = "taskList" })
                "forgotPassword" -> ForgotPasswordScreen(blue, scope, { screen = it })
                else -> AuthScreen(screen, blue, scope, storage, { screen = it }, { u -> user = u; screen = "taskList" })
            }
        }
    }
}
