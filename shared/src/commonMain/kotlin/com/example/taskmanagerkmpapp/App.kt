package com.example.taskmanagerkmpapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal data class User(val name:String, val username:String, val email:String)
@Serializable
internal data class Task(val id:Int, val title:String, val description:String, val priority:String = "Medium", val done:Boolean = false)
internal data class LoginResult(val user: User, val token: String)
private object Repo {
    private val api = BackendApi()
    var token = ""
    suspend fun login(u:String,p:String): AuthResponse = api.login(u,p)
    suspend fun register(n:String,u:String,e:String,p:String): AuthResponse = api.register(RegisterBody(n,u,e,p))
    suspend fun reset(e:String,np:String) = api.reset(ResetBody(e,np))
    suspend fun tasks(): List<Task> = api.tasks(token).map { Task(it.id.toInt(),it.title,it.description,it.priority,it.completed) }
    suspend fun add(task:Task) = api.add(token,TaskBody(task.title,task.description,task.priority))
    suspend fun complete(id:Int,done:Boolean) = api.complete(token,id.toLong(),done)
    suspend fun delete(id:Int) = api.delete(token,id.toLong())
}

@Composable
fun App() {
    val storage = remember { sessionStorage() }
    Repo.token = storage.read("token")
    var user by remember { mutableStateOf<User?>(null) }
    var screen by remember { mutableStateOf("login") }
    var mode by remember { mutableStateOf("login") }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    var items by remember { 
        mutableStateOf(
            try {
                val cached = storage.read("tasks_cache")
                if (cached.isNotBlank()) Json.decodeFromString<List<Task>>(cached) else emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        )
    }
    
    val scope = rememberCoroutineScope()
    val blue = Color(0xFF1A237E)

    LaunchedEffect(user) { 
        if (user != null && Repo.token.isNotBlank()) {
            val remoteTasks = Repo.tasks()
            if (remoteTasks.isNotEmpty() || items.isEmpty()) {
                items = remoteTasks
                storage.saveTasks(Json.encodeToString(remoteTasks))
            }
        } 
    }

    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = Color(0xFFF5F5F5)) {
            when (screen) {
                "tasks" -> {
                    Scaffold(
                        floatingActionButton = {
                            FloatingActionButton(
                                onClick = { screen = "addTask" },
                                containerColor = Color(0xFFFF4081),
                                contentColor = Color.White,
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Task")
                            }
                        }
                    ) { padding ->
                        Column(Modifier.fillMaxSize().padding(padding)) {
                            // Header
                            Column(
                                Modifier.fillMaxWidth().background(blue).padding(24.dp)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("${user?.name ?: "User"}'s Tasks", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                        Text("03.09.2026", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                                    }
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.size(width = 70.dp, height = 75.dp)
                                    ) {
                                        Column(
                                            Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text("${items.size}", color = blue, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                                            Text("Tasks", color = Color.Gray, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            // Task List
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                items(items) { task ->
                                    Card(
                                        Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth().padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = task.done,
                                                onCheckedChange = {
                                                    scope.launch {
                                                        Repo.complete(task.id, !task.done)
                                                        items = Repo.tasks()
                                                        storage.saveTasks(Json.encodeToString(items))
                                                    }
                                                }
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text(task.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                Text(task.description, fontSize = 13.sp, color = Color.Gray)
                                                val priorityColor = when (task.priority.lowercase()) {
                                                    "high" -> Color(0xFFD32F2F)
                                                    "medium" -> Color(0xFFF57C00)
                                                    else -> Color(0xFF388E3C)
                                                }
                                                Text(task.priority, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = priorityColor, modifier = Modifier.padding(top = 4.dp))
                                            }
                                            IconButton(onClick = {
                                                scope.launch {
                                                    Repo.delete(task.id)
                                                    items = Repo.tasks()
                                                    storage.saveTasks(Json.encodeToString(items))
                                                }
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF9A9A))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Card(
                            Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            when (screen) {
                                "login" -> Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Welcome back", color = blue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                    OutlinedTextField(username, { username = it }, Modifier.fillMaxWidth(), label = { Text("Username") }, singleLine = true)
                                    OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Password") }, singleLine = true, visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { TextButton(onClick = { showPassword = !showPassword }) { Text(if (showPassword) "Hide" else "Show") } })
                                    if (error.isNotEmpty()) Text(error, color = Color(0xFFB00020), fontSize = 12.sp)
                                    Button(onClick = {
                                        if (isLoading || username.isBlank() || password.isBlank()) {
                                            if (username.isBlank()) error = "Username is required"
                                            else if (password.isBlank()) error = "Password is required"
                                            return@Button
                                        }
                                        isLoading = true
                                        scope.launch {
                                            when (val result = Repo.login(username, password)) {
                                                is AuthResponse.Success -> {
                                                    val data = result.auth
                                                    user = User(data.user.name, data.user.username, data.user.email)
                                                    storage.save(data.user.username, data.user.name, data.user.email, data.token)
                                                    Repo.token = data.token
                                                    items = Repo.tasks()
                                                    storage.saveTasks(Json.encodeToString(items))
                                                    screen = "tasks"
                                                }
                                                is AuthResponse.Error -> error = result.message
                                            }
                                            isLoading = false
                                        }
                                    }, Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = blue)) {
                                        Text(if (isLoading) "Please wait..." else "Login", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                        TextButton(onClick = { screen = "register"; error = "" }) { Text("New user? Register", color = blue) }
                                        TextButton(onClick = { mode = "forgot"; error = "" }) { Text("Forgot password", color = blue) }
                                    }
                                }
                                "register" -> Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Create account", color = blue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                    OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Full Name") }, singleLine = true)
                                    OutlinedTextField(username, { username = it }, Modifier.fillMaxWidth(), label = { Text("Username") }, singleLine = true)
                                    OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true)
                                    OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Password") }, singleLine = true, visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { TextButton(onClick = { showPassword = !showPassword }) { Text(if (showPassword) "Hide" else "Show") } })
                                    OutlinedTextField(confirmPassword, { confirmPassword = it }, Modifier.fillMaxWidth(), label = { Text("Confirm Password") }, singleLine = true, visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation())
                                    if (error.isNotEmpty()) Text(error, color = Color(0xFFB00020), fontSize = 12.sp)
                                    Button(onClick = {
                                        if (isLoading) return@Button
                                        error = when {
                                            name.isBlank() -> "Name is required"
                                            username.isBlank() -> "Username is required"
                                            !email.contains("@") -> "Enter a valid email"
                                            password.length < 6 -> "Password must be at least 6 characters"
                                            password != confirmPassword -> "Passwords do not match"
                                            else -> ""
                                        }
                                        if (error.isNotEmpty()) return@Button
                                        isLoading = true
                                        scope.launch {
                                            when (val result = Repo.register(name, username, email, password)) {
                                                is AuthResponse.Success -> {
                                                    val data = result.auth
                                                    user = User(data.user.name, data.user.username, data.user.email)
                                                    storage.save(data.user.username, data.user.name, data.user.email, data.token)
                                                    Repo.token = data.token
                                                    items = Repo.tasks()
                                                    storage.saveTasks(Json.encodeToString(items))
                                                    screen = "tasks"
                                                }
                                                is AuthResponse.Error -> error = result.message
                                            }
                                            isLoading = false
                                        }
                                    }, Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = blue)) {
                                        Text(if (isLoading) "Please wait..." else "Register", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    TextButton(onClick = { screen = "login"; error = "" }, Modifier.align(Alignment.CenterHorizontally)) { Text("Back to Login", color = blue) }
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
                                "addTask" -> Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Add Task", color = blue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                    OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Title") }, singleLine = true)
                                    OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Description") })
                                    
                                    // Priority Selection
                                    Text("Priority", fontWeight = FontWeight.Bold, color = blue, fontSize = 16.sp)
                                    var selectedPriority by remember { mutableStateOf("Medium") }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("Low", "Medium", "High").forEach { p ->
                                            FilterChip(
                                                selected = selectedPriority == p,
                                                onClick = { selectedPriority = p },
                                                label = { Text(p) }
                                            )
                                        }
                                    }
                                    
                                    Button(onClick = {
                                        if (title.isNotBlank() && description.isNotBlank()) {
                                            scope.launch {
                                                Repo.add(Task(0, title.trim(), description.trim(), selectedPriority))
                                                items = Repo.tasks()
                                                storage.saveTasks(Json.encodeToString(items))
                                                title = ""; description = ""; screen = "tasks"
                                            }
                                        }
                                    }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = blue)) { Text("Save Task", color = Color.White) }
                                    OutlinedButton(onClick = { title = ""; description = ""; screen = "tasks" }, Modifier.fillMaxWidth()) { Text("Cancel") }
                                }
                                else -> { }
                            }
                        }
                        
                        // Logout button at bottom of profile or login is not really needed as it's inside card.
                        // But let's add a logout button accessible from tasks too if needed.
                        // For now, logout is in "profile" and "login" screens.
                    }
                }
            }
        }
    }
}
