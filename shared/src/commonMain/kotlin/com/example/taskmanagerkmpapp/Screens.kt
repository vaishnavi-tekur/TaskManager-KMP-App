package com.example.taskmanagerkmpapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import kotlinx.coroutines.*

@Composable
internal fun AuthScreen(screen: String, blue: Color, scope: CoroutineScope, storage: SessionStorage, onNavigate: (String) -> Unit, onLoginSuccess: (User) -> Unit) {
    var u by remember { mutableStateOf("") }; var p by remember { mutableStateOf("") }; var n by remember { mutableStateOf("") }
    var err by remember { mutableStateOf("") }; var vis by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), Alignment.Center) {
        Card(Modifier.fillMaxWidth().padding(24.dp), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color.White)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if(screen == "login") "Welcome Back" else "Join Us", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = blue)
                if (screen != "login") OutlinedTextField(n, { n = it }, Modifier.fillMaxWidth(), label = { Text("Name") })
                OutlinedTextField(u, { u = it }, Modifier.fillMaxWidth(), label = { Text("Email/Username") })
                OutlinedTextField(p, { p = it }, Modifier.fillMaxWidth(), label = { Text("Password") }, visualTransformation = if (vis) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton({ vis = !vis }) { Icon(if (vis) Icons.Default.Visibility else Icons.Default.VisibilityOff, null) } })
                if (err.isNotEmpty()) Text(err, color = Color.Red, fontSize = 12.sp)
                Button({
                    val res = if (screen == "login") Repo.login(u, p) else Repo.register(n, u, "mock@email.com", p)
                    if (res != null) {
                        storage.save(res.username, res.name, res.email, "token")
                        onLoginSuccess(res)
                    } else err = "Invalid credentials (Try admin/password)"
                }, Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(blue)) { Text(if (screen == "login") "Login" else "Register") }
                TextButton({ onNavigate(if (screen == "login") "register" else "login") }) { Text(if (screen == "login") "Need an account? Register" else "Have an account? Login", color = Color.Gray) }
            }
        }
    }
}

@Composable
internal fun WelcomeScreen(user: User?, blue: Color, onLogout: () -> Unit) {
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("Hello, ${user?.name}!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = blue)
        Spacer(Modifier.height(24.dp))
        Button(onLogout, colors = ButtonDefaults.buttonColors(blue)) { Text("Logout") }
    }
}
