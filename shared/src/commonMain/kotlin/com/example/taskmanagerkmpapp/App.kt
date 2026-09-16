package com.example.taskmanagerkmpapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
            if (user == null) {
                AuthScreen(screen, blue, scope, storage, { screen = it }, { user = it })
            } else {
                WelcomeScreen(user, blue) { user = null; storage.clear() }
            }
        }
    }
}
