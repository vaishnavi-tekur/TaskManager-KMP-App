package com.example.taskmanagerkmpapp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color

@Composable
fun App() {
    val storage = remember { sessionStorage() }
    var user by remember { mutableStateOf<User?>(null) }
    var screen by remember { mutableStateOf("login") }
    val scope = rememberCoroutineScope()
    val blue = Color(0xFF1A237E)

    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = Color(0xFFF5F5F5)) {
            if (user != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Welcome, ${user?.name}!", style = MaterialTheme.typography.headlineMedium)
                        Button({ user = null; screen = "login" }) { Text("Logout") }
                    }
                }
            } else {
                AuthScreen(screen, blue, scope, storage, { screen = it }, { u, _ -> user = u })
            }
        }
    }
}
