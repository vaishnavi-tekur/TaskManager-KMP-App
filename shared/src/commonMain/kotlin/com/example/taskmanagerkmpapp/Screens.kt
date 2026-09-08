package com.example.taskmanagerkmpapp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
internal fun TaskListScreen(user: User?, items: List<Task>, blue: Color, scope: CoroutineScope, onNavigate: (String) -> Unit, onTasksUpdated: (List<Task>) -> Unit) {
    LaunchedEffect(Unit) {
        // This code runs automatically as soon as the screen is displayed
        val latestTasks = Repo.tasks()
        onTasksUpdated(latestTasks)
    }
    fun logout() = onNavigate("login")
    Scaffold(floatingActionButton = { FloatingActionButton(onClick = { onNavigate("addTask") }, containerColor = Color(0xFFFF4081), contentColor = Color.White, shape = RoundedCornerShape(50)) { Icon(Icons.Default.Add, null) } }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.fillMaxWidth().background(blue).padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton({ logout() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                        Column {
                            Text("${user?.name ?: "User"}'s Tasks", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("03.09.2026", color = Color.White.copy(0.7f), fontSize = 14.sp)
                                TextButton({ scope.launch { if(Repo.deleteAccount()) logout() } }) {
                                    Text("Delete Account", color = Color(0xFFEF9A9A), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(4.dp), modifier = Modifier.size(70.dp, 75.dp)) {
                        Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                            Text("${items.size}", color = blue, fontSize = 26.sp, fontWeight = FontWeight.Bold); Text("Tasks", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
            LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                items(items) { task ->
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(task.done, { scope.launch { Repo.complete(task.id, !task.done); onTasksUpdated(Repo.tasks()) } })
                            Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                                Text(task.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(task.description, fontSize = 13.sp, color = Color.Gray)
                                Text(task.priority, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (task.priority == "High") Color.Red else if (task.priority == "Medium") Color(0xFFF57C00) else Color.Green)
                            }
                            IconButton({ scope.launch { Repo.delete(task.id); onTasksUpdated(Repo.tasks()) } }) { Icon(Icons.Default.Delete, null, tint = Color(0xFFEF9A9A)) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddTaskScreen(blue: Color, scope: CoroutineScope, onNavigate: (String) -> Unit, onTasksUpdated: (List<Task>) -> Unit) {
    var title by remember { mutableStateOf("") }; var desc by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }; var expanded by remember { mutableStateOf(false) }
    Scaffold(topBar = { Box(Modifier.fillMaxWidth().height(56.dp).background(Color(0xFF0D1B4D))) }) { p ->
        Column(Modifier.fillMaxSize().padding(p).background(Color.White)) {
            Text("Add New Task", Modifier.fillMaxWidth().padding(32.dp), color = blue, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Task Title") }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))
                TextField(desc, { desc = it }, Modifier.fillMaxWidth(), label = { Text("Task Description") }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))
                ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
                    TextField(priority, {}, Modifier.menuAnchor().fillMaxWidth(), readOnly = true, label = { Text("Priority") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))
                    ExposedDropdownMenu(expanded, { expanded = false }) { listOf("Low", "Medium", "High").forEach { DropdownMenuItem(text = { Text(it) }, onClick = { priority = it; expanded = false }) } }
                }
                Button({ if (title.isNotBlank()) scope.launch { Repo.add(Task(0, title, desc, priority)); onTasksUpdated(Repo.tasks()); onNavigate("tasks") } }, Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(4.dp), colors = ButtonDefaults.buttonColors(blue)) { Text("SAVE TASK", fontWeight = FontWeight.Bold) }
                OutlinedButton({ onNavigate("tasks") }, Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(4.dp)) { Text("CANCEL", color = Color.Gray) }
            }
        }
    }
}

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
                // Change the label so users know they can use their email
                OutlinedTextField(
                    value = u,
                    onValueChange = { u = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Username or Bhrish Email") }
                )
                Button({
                    if (screen != "login" && p != cp) { err = "Passwords mismatch"; return@Button }
                    load = true; scope.launch {
                        val res = if (screen == "login") Repo.login(u, p) else Repo.register(n, u, e, p)
                        if (res is AuthResponse.Success) {
                            storage.save(res.auth.user.username, res.auth.user.name, res.auth.user.email, res.auth.token)
                            Repo.token = res.auth.token; onLoginSuccess(User(res.auth.user.name, res.auth.user.username, res.auth.user.email), Repo.tasks())
                        } else err = (res as AuthResponse.Error).message
                        load = false
                    }
                }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(blue)) { Text(if (load) "Loading..." else if (screen == "login") "Login" else "Register") }
                if (screen == "login") TextButton({ onNavigate("forgotPassword") }, Modifier.align(Alignment.CenterHorizontally)) { Text("Forgot Password?", color = blue.copy(0.7f)) }
                TextButton({ onNavigate(if (screen == "login") "register" else "login"); err = "" }, Modifier.align(Alignment.CenterHorizontally)) { Text(if (screen == "login") "New user? Register" else "Back to Login") }
            }
        }
    }
}

@Composable
internal fun ForgotPasswordScreen(blue: Color, scope: CoroutineScope, onNavigate: (String) -> Unit) {
    var e by remember { mutableStateOf("") };
    var np by remember { mutableStateOf("") };
    var cp by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") };
    var err by remember { mutableStateOf("") };
    var load by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Card(Modifier.fillMaxWidth().padding(24.dp), RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(24.dp), Arrangement.spacedBy(12.dp)) {
                Text("Reset Password", color = blue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    e,
                    { e = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("Email Address") })
                OutlinedTextField(
                    np,
                    { np = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    cp,
                    { cp = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                if (err.isNotEmpty()) Text(err, color = Color.Red, fontSize = 12.sp)
                if (msg.isNotEmpty()) Text(msg, color = Color.Green, fontSize = 12.sp)
                Button({
                    if (np != cp) {
                        err = "Passwords mismatch"
                        return@Button
                    }
                    load = true
                    scope.launch {
                        val res = Repo.reset(e, np)
                        when (res) {
                            is ResetResponse.Success -> {
                                msg = "Password reset successfully!"
                                delay(1500)
                                onNavigate("login")
                            }

                            is ResetResponse.Error -> {
                                // This will now display "user email is not registered" if returned by backend
                                err = res.message
                                msg = ""
                            }
                        }
                        load = false
                    }
                }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(blue)) {
                    Text(if (load) "Processing..." else "Reset Password")
                }
            }
        }

    }
}
