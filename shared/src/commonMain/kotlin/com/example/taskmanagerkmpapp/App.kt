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
private data class LoginResult(val user: User, val token: String)
private object Repo {
    private val api = BackendApi()
    var token = ""
    suspend fun login(u:String,p:String): LoginResult? = try { api.login(u,p)?.let { token = it.token; LoginResult(User(it.user.name,it.user.username,it.user.email),it.token) } } catch (e: Exception) { null }
    suspend fun register(n:String,u:String,e:String,p:String): LoginResult? = try { api.register(RegisterBody(n,u,e,p))?.let { login(u,p) } } catch (e: Exception) { null }
    suspend fun reset(e:String,np:String) = try { api.reset(ResetBody(e,np)) } catch (e: Exception) { false }
    suspend fun tasks(): List<Task> = try { api.tasks(token).map { Task(it.id.toInt(),it.title,it.description,it.priority,it.completed) } } catch (e: Exception) { emptyList() }
    suspend fun add(task:Task) = try { api.add(token,TaskBody(task.title,task.description,task.priority)) } catch (e: Exception) { null }
    suspend fun complete(id:Int,done:Boolean) = try { api.complete(token,id.toLong(),done) } catch (e: Exception) { false }
    suspend fun delete(id:Int) = try { api.delete(token,id.toLong()) } catch (e: Exception) { false }
}

@Composable
fun App() {
    val storage = remember { sessionStorage() }
    Repo.token = storage.read("token")
    var user by remember {
        mutableStateOf(storage.read("token").takeIf { it.isNotBlank() }?.let {
            User(storage.read("name"), storage.read("user"), storage.read("email"))
        })
    }
    var screen by remember { mutableStateOf(if (user != null) "tasks" else "login") }
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
    var items by remember { mutableStateOf(emptyList<Task>()) }
    val scope = rememberCoroutineScope()
    val blue = Color(0xFF1A237E)

    LaunchedEffect(user) { if (user != null && Repo.token.isNotBlank()) items = Repo.tasks() }

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
                                    "forgot" -> if (Repo.reset(email, newPassword)) LoginResult(User("", "", email), "") else null
                                    else -> Repo.login(username, password)
                                }
                                if (result != null) {
                                    if (mode == "forgot") {
                                        mode = "login"
                                        password = newPassword
                                        error = "Password updated"
                                    } else {
                                        user = result.user
                                        storage.save(result.user.username, result.user.name, result.user.email, result.token)
                                        Repo.token = result.token
                                        items = Repo.tasks()
                                        screen = "profile"
                                    }
                                } else {
                                    error = "Network error or invalid credentials"
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
                    "profile" -> Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Welcome, ${user?.name}", color = blue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text("Your account details", color = Color.Gray)
                        Text("Name: ${user?.name}")
                        Text("Username: ${user?.username}")
                        Text("Email: ${user?.email}")
                        Button(onClick = { screen = "tasks" }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = blue)) { Text("Continue to Tasks", color = Color.White) }
                        OutlinedButton(onClick = { user = null; storage.clear(); screen = "login" }, Modifier.fillMaxWidth()) { Text("Logout") }
                    }
                    "tasks" -> {
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                Modifier.fillMaxWidth().background(blue).padding(24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${user?.name ?: "User"}'s Tasks", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                    Text("Logged in as ${user?.username ?: "User"}", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                                    Text("03.09.2026", color = Color.White.copy(alpha = 0.75f), fontSize = 14.sp)
                                }
                                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(2.dp)) {
                                    Column(Modifier.padding(horizontal = 18.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${items.size}", color = blue, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                        Text("Tasks", color = Color.Gray, fontSize = 12.sp)
                                    }
                                }
                            }
                            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items.forEach { task ->
                                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4FF))) {
                                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column(Modifier.weight(1f)) { Text(task.title, fontWeight = FontWeight.Bold); Text(task.description, fontSize = 12.sp, color = Color.Gray) }
                                        Checkbox(task.done, onCheckedChange = { scope.launch { Repo.complete(task.id, !task.done); items = Repo.tasks() } })
                                    }
                                }
                            }
                                Button(onClick = { screen = "addTask" }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = blue)) { Text("Add Task", color = Color.White) }
                                OutlinedButton(onClick = { user = null; storage.clear(); screen = "login"; mode = "login" }, Modifier.fillMaxWidth()) { Text("Back to Login") }
                            }
                        }
                    }
                    "addTask" -> Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Add Task", color = blue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Title") }, singleLine = true)
                        OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Description") })
                        Button(onClick = {
                            if (title.isNotBlank() && description.isNotBlank()) {
                                scope.launch {
                                    Repo.add(Task(0, title.trim(), description.trim()))
                                    items = Repo.tasks(); title = ""; description = ""; screen = "tasks"
                                }
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
