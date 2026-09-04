package com.example.taskmanagerkmpapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun TaskListScreen(
    user: User?,
    items: List<Task>,
    blue: Color,
    scope: CoroutineScope,
    onNavigate: (String) -> Unit,
    onTasksUpdated: (List<Task>) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate("addTask") },
                containerColor = Color(0xFFFF4081),
                contentColor = Color.White,
                shape = RoundedCornerShape(50)
            ) { Icon(Icons.Default.Add, "Add Task") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            HeaderSection(user, items.size, blue) { onNavigate("login") }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(items) { task ->
                    TaskCard(task, scope, onTasksUpdated)
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(user: User?, count: Int, blue: Color, onLogout: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(blue).padding(24.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLogout, modifier = Modifier.padding(end = 8.dp)) {
                    Icon(Icons.Default.ArrowBack, "Back to Login", tint = Color.White)
                }
                Column {
                    Text("${user?.name ?: "User"}'s Tasks", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("03.09.2026", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                }
            }
            Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(4.dp), modifier = Modifier.size(70.dp, 75.dp)) {
                Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                    Text("$count", color = blue, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text("Tasks", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun TaskCard(task: Task, scope: CoroutineScope, onTasksUpdated: (List<Task>) -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(task.done, { 
                scope.launch { Repo.complete(task.id, !task.done); onTasksUpdated(Repo.tasks()) } 
            })
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(task.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(task.description, fontSize = 13.sp, color = Color.Gray)
                val pColor = when (task.priority.lowercase()) { "high" -> Color(0xFFD32F2F); "medium" -> Color(0xFFF57C00); else -> Color(0xFF388E3C) }
                Text(task.priority, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = pColor, modifier = Modifier.padding(top = 4.dp))
            }
            IconButton(onClick = { scope.launch { Repo.delete(task.id); onTasksUpdated(Repo.tasks()) } }) {
                Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFEF9A9A))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddTaskScreen(blue: Color, scope: CoroutineScope, onNavigate: (String) -> Unit, onTasksUpdated: (List<Task>) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Box(Modifier.fillMaxWidth().height(56.dp).background(Color(0xFF0D1B4D)))
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(Color.White)) {
            Text(
                text = "Add New Task",
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp, bottom = 16.dp),
                color = blue,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Task Title") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Gray,
                        unfocusedIndicatorColor = Color.LightGray
                    ),
                    singleLine = true
                )

                TextField(
                    value = desc,
                    onValueChange = { desc = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Task Description") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Gray,
                        unfocusedIndicatorColor = Color.LightGray
                    )
                )

                Column {
                    Text("Select Priority", color = Color.Gray, fontSize = 12.sp)
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        TextField(
                            value = priority,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Gray,
                                unfocusedIndicatorColor = Color.LightGray
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf("Low", "Medium", "High").forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p) },
                                    onClick = {
                                        priority = p
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank() && desc.isNotBlank()) {
                            scope.launch {
                                Repo.add(Task(0, title, desc, priority))
                                onTasksUpdated(Repo.tasks())
                                onNavigate("tasks")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(blue)
                ) {
                    Text("SAVE TASK", color = Color.White, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onNavigate("tasks") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                ) {
                    Text("CANCEL", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
internal fun ForgotPasswordScreen(
    blue: Color,
    scope: CoroutineScope,
    onNavigate: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Card(Modifier.fillMaxWidth().padding(24.dp), RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(24.dp), Arrangement.spacedBy(12.dp)) {
                Text("Reset Password", color = blue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Enter your email and a new password to reset your account access.", fontSize = 14.sp, color = Color.Gray)
                
                OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email Address") })
                OutlinedTextField(newPassword, { newPassword = it }, Modifier.fillMaxWidth(), label = { Text("New Password") }, visualTransformation = PasswordVisualTransformation())
                OutlinedTextField(confirmPassword, { confirmPassword = it }, Modifier.fillMaxWidth(), label = { Text("Confirm New Password") }, visualTransformation = PasswordVisualTransformation())
                
                if (error.isNotEmpty()) Text(error, color = Color.Red, fontSize = 12.sp)
                if (message.isNotEmpty()) Text(message, color = Color(0xFF388E3C), fontSize = 12.sp)
                
                Button(onClick = {
                    if (newPassword != confirmPassword) { error = "Passwords mismatch"; return@Button }
                    if (newPassword.length < 6) { error = "Password too short"; return@Button }
                    isLoading = true
                    error = ""
                    message = ""
                    scope.launch {
                        if (Repo.reset(email, newPassword)) {
                            message = "Password updated successfully!"
                            kotlinx.coroutines.delay(1500)
                            onNavigate("login")
                        } else {
                            error = "No account found with this email."
                        }
                        isLoading = false
                    }
                }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(blue)) { 
                    Text(if (isLoading) "Processing..." else "Reset Password") 
                }
                
                TextButton(onClick = { onNavigate("login") }, Modifier.align(Alignment.CenterHorizontally)) { 
                    Text("Back to Login") 
                }
            }
        }
    }
}

@Composable
internal fun AuthScreen(
    screen: String,
    blue: Color,
    scope: CoroutineScope,
    storage: SessionStorage,
    onNavigate: (String) -> Unit,
    onLoginSuccess: (User, List<Task>) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Card(Modifier.fillMaxWidth().padding(24.dp), RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(24.dp), Arrangement.spacedBy(12.dp)) {
                if (screen == "login") {
                    Text("Welcome back", color = blue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(username, { username = it }, Modifier.fillMaxWidth(), label = { Text("Username") })
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description = if (passwordVisible) "Hide password" else "Show password"
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = description)
                            }
                        }
                    )
                    if (error.isNotEmpty()) Text(error, color = Color.Red, fontSize = 12.sp)
                    Button(onClick = {
                        isLoading = true
                        scope.launch {
                            when (val res = Repo.login(username, password)) {
                                is AuthResponse.Success -> {
                                    val u = User(res.auth.user.name, res.auth.user.username, res.auth.user.email)
                                    Repo.token = res.auth.token
                                    storage.save(res.auth.user.username, res.auth.user.name, res.auth.user.email, res.auth.token)
                                    onLoginSuccess(u, Repo.tasks())
                                }
                                is AuthResponse.Error -> error = res.message
                            }
                            isLoading = false
                        }
                    }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(blue)) { Text(if (isLoading) "Loading..." else "Login") }
                    
                    TextButton(
                        onClick = { onNavigate("forgotPassword"); error = "" },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) { 
                        Text("Forgot Password?", color = blue.copy(alpha = 0.7f), fontSize = 14.sp)
                    }

                    TextButton(onClick = { onNavigate("register"); error = "" }, Modifier.align(Alignment.CenterHorizontally)) { Text("New user? Register") }
                } else {
                    Text("Create account", color = blue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Full Name") })
                    OutlinedTextField(username, { username = it }, Modifier.fillMaxWidth(), label = { Text("Username") })
                    OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") })
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description = if (passwordVisible) "Hide password" else "Show password"
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = description)
                            }
                        }
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Confirm Password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description = if (passwordVisible) "Hide password" else "Show password"
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = description)
                            }
                        }
                    )
                    if (error.isNotEmpty()) Text(error, color = Color.Red, fontSize = 12.sp)
                    Button(onClick = {
                        if (password != confirmPassword) { error = "Passwords mismatch"; return@Button }
                        isLoading = true
                        scope.launch {
                            when (val res = Repo.register(name, username, email, password)) {
                                is AuthResponse.Success -> {
                                    val u = User(res.auth.user.name, res.auth.user.username, res.auth.user.email)
                                    Repo.token = res.auth.token
                                    storage.save(res.auth.user.username, res.auth.user.name, res.auth.user.email, res.auth.token)
                                    onLoginSuccess(u, Repo.tasks())
                                }
                                is AuthResponse.Error -> error = res.message
                            }
                            isLoading = false
                        }
                    }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(blue)) { Text("Register") }
                    TextButton(onClick = { onNavigate("login"); error = "" }, Modifier.align(Alignment.CenterHorizontally)) { Text("Back to Login") }
                }
            }
        }
    }
}
