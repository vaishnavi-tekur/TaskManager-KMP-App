package com.example.taskmanagerkmpapp
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
internal fun AuthScreen(screen: String, blue: Color, scope: CoroutineScope, storage: SessionStorage, onNavigate: (String) -> Unit, onLoginSuccess: (User, List<Task>) -> Unit) {
    var u by remember { mutableStateOf("") }; var p by remember { mutableStateOf("") }; var n by remember { mutableStateOf("") }
    var e by remember { mutableStateOf("") }; var cp by remember { mutableStateOf("") }; var vis by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf("") }; var load by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Card(Modifier.fillMaxWidth().padding(24.dp), RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(24.dp), Arrangement.spacedBy(12.dp)) {
                Text(if (screen == "login") "Welcome back" else "Create account", color = blue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                if (screen != "login") OutlinedTextField(n, { n = it }, Modifier.fillMaxWidth(), label = { Text("Full Name") })
                OutlinedTextField(u, { u = it }, Modifier.fillMaxWidth(), label = { Text("Username") })
                if (screen != "login") OutlinedTextField(e, { e = it }, Modifier.fillMaxWidth(), label = { Text("Email") })
                OutlinedTextField(p, { p = it }, Modifier.fillMaxWidth(), label = { Text("Password") }, visualTransformation = if (vis) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton({ vis = !vis }) { Icon(if (vis) Icons.Default.Visibility else Icons.Default.VisibilityOff, null) } })
                if (screen != "login") OutlinedTextField(cp, { cp = it }, Modifier.fillMaxWidth(), label = { Text("Confirm Password") }, visualTransformation = if (vis) VisualTransformation.None else PasswordVisualTransformation())
                if (err.isNotEmpty()) Text(err, color = Color.Red, fontSize = 12.sp)
                Button({
                    if (screen != "login" && p != cp) { err = "Passwords mismatch"; return@Button }
                    load = true; scope.launch {
                        val res = if (screen == "login") Repo.login(u, p) else Repo.register(n, u, e, p)
                        if (res is AuthResponse.Success) {
                            storage.save(res.auth.user.username, res.auth.user.name, res.auth.user.email, res.auth.token)
                            Repo.token = res.auth.token; onLoginSuccess(User(res.auth.user.name, res.auth.user.username, res.auth.user.email), emptyList())
                        } else err = (res as AuthResponse.Error).message
                        load = false
                    }
                }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(blue)) { Text(if (load) "Loading..." else if (screen == "login") "Login" else "Register") }
                TextButton({ onNavigate(if (screen == "login") "register" else "login"); err = "" }, Modifier.align(Alignment.CenterHorizontally)) { Text(if (screen == "login") "New user? Register" else "Back to Login") }
            }
        }
    }
}
