package com.example.taskmanagerkmpapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private data class User(val name:String, val username:String, val email:String)
private data class Task(val id:Int, val title:String, val description:String, val priority:String = "Medium", val done:Boolean = false)
private object Repo {
    val users = mutableMapOf("admin" to User("Jane Doe", "admin", "jane@example.com"))
    val pass = mutableMapOf("admin" to "password123")
    val tasks = mutableMapOf("admin" to listOf(Task(1,"Project","Finalize app flow","High"), Task(2,"Groceries","Milk, Eggs","Medium")))
    suspend fun login(u:String,p:String): User? = if (pass[u] == p) users[u] else null
    suspend fun register(n:String,u:String,e:String,p:String): User? {
        if (users.containsKey(u)) return null
        users[u] = User(n, u, e)
        pass[u] = p
        tasks[u] = emptyList()
        return users[u]
    }
    suspend fun reset(e:String, np:String): Boolean {
        val user = users.values.firstOrNull { it.email == e } ?: return false
        pass[user.username] = np
        return true
    }
}

@Composable
fun App() {
    var screen by remember { mutableStateOf("login") }
    var user by remember { mutableStateOf<User?>(null) }
    var mode by remember { mutableStateOf("login") }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("admin") }
    var email by remember { mutableStateOf("jane@example.com") }
    var password by remember { mutableStateOf("password123") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val blue = Color(0xFF1A237E)

    MaterialTheme {
        Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
            Card(Modifier.fillMaxWidth().padding(horizontal = 24.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                when (screen) {
                    "login" -> Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(if (mode == "register") "Create account" else if (mode == "forgot") "Reset password" else "Welcome back", color = blue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        if (mode == "register") OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Full Name") }, singleLine = true)
                        if (mode != "forgot") OutlinedTextField(username, { username = it }, Modifier.fillMaxWidth(), label = { Text("Username") }, singleLine = true)
                        if (mode != "login") OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true)
                        if (mode != "forgot") OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Password") }, singleLine = true, visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { TextButton(onClick = { showPassword = !showPassword }) { Text(if (showPassword) "Hide" else "Show") } })
                        if (mode == "register") OutlinedTextField(confirmPassword, { confirmPassword = it }, Modifier.fillMaxWidth(), label = { Text("Confirm Password") }, singleLine = true, visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation())
                        if (mode == "forgot") OutlinedTextField(newPassword, { newPassword = it }, Modifier.fillMaxWidth(), label = { Text("New Password") }, singleLine = true, visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { TextButton(onClick = { showPassword = !showPassword }) { Text(if (showPassword) "Hide" else "Show") } })
                        if (error.isNotEmpty()) Text(error, color = Color(0xFFB00020), fontSize = 12.sp)
                        Button(onClick = {
                            if (isLoading) return@Button
                            error = when {
                                mode == "register" && name.isBlank() -> "Name is required"
                                mode != "forgot" && username.isBlank() -> "Username is required"
                                mode != "login" && !email.contains("@") -> "Enter a valid email"
                                mode != "forgot" && password.length < 6 -> "Password must be at least 6 characters"
                                mode == "register" && password != confirmPassword -> "Passwords do not match"
                                mode == "forgot" && newPassword.length < 6 -> "Password must be at least 6 characters"
                                else -> ""
                            }
                            if (error.isNotEmpty()) return@Button
                            isLoading = true
                            scope.launch {
                                val result = when (mode) {
                                    "register" -> Repo.register(name, username, email, password)
                                    "forgot" -> if (Repo.reset(email, newPassword)) User("", "", email) else null
                                    else -> Repo.login(username, password)
                                }
                                when {
                                    mode == "register" && result != null -> { user = result; screen = "tasks" }
                                    mode == "forgot" && result != null -> { mode = "login"; password = newPassword; error = "Password updated" }
                                    mode == "login" && result != null -> { user = result; screen = "tasks" }
                                    else -> error = "Invalid input"
                                }
                                isLoading = false
                            }
                        }, Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = blue)) {
                            Text(if (isLoading) "Please wait..." else if (mode == "register") "Register" else if (mode == "forgot") "Update" else "Login", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            TextButton(onClick = { mode = if (mode == "login") "register" else "login"; error = "" }) { Text(if (mode == "login") "New user? Register" else "Back to Login", color = blue) }
                            if (mode == "login") TextButton(onClick = { mode = "forgot"; error = "" }) { Text("Forgot password", color = blue) }
                        }
                    }
                    "tasks" -> {
                        val items = Repo.tasks[user?.username ?: ""] ?: emptyList()
                        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("${user?.name ?: "User"}'s Tasks", color = blue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            items.forEach { task ->
                                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4FF))) {
                                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column(Modifier.weight(1f)) { Text(task.title, fontWeight = FontWeight.Bold); Text(task.description, fontSize = 12.sp, color = Color.Gray) }
                                        Checkbox(task.done, onCheckedChange = { val current = Repo.tasks[user?.username ?: ""] ?: emptyList(); Repo.tasks[user?.username ?: ""] = current.map { if (it.id == task.id) it.copy(done = !it.done) else it } })
                                    }
                                }
                            }
                            Button(onClick = { screen = "addTask" }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = blue)) { Text("Add Task", color = Color.White) }
                            OutlinedButton(onClick = { user = null; screen = "login"; mode = "login" }, Modifier.fillMaxWidth()) { Text("Logout") }
                        }
                    }
                    "addTask" -> Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Add Task", color = blue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Title") }, singleLine = true)
                        OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Description") })
                        Button(onClick = {
                            val current = Repo.tasks[user?.username ?: ""] ?: emptyList()
                            if (title.isNotBlank() && description.isNotBlank()) {
                                Repo.tasks[user?.username ?: ""] = current + Task((current.maxOfOrNull { it.id } ?: 0) + 1, title.trim(), description.trim())
                                title = ""; description = ""; screen = "tasks"
                            }
                        }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = blue)) { Text("Save Task", color = Color.White) }
                        OutlinedButton(onClick = { title = ""; description = ""; screen = "tasks" }, Modifier.fillMaxWidth()) { Text("Cancel") }
                    }
                    else -> { }
                }
            }
        }
    }
}
