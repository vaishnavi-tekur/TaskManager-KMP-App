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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlinx.coroutines.*
import org.jetbrains.compose.resources.painterResource
import taskmanagerkmpapp.shared.generated.resources.Res
import taskmanagerkmpapp.shared.generated.resources.ic_google
import kotlinx.datetime.*

@Composable
internal fun TaskListScreen(user: User?, items: List<Task>, blue: Color, scope: CoroutineScope, onNavigate: (String) -> Unit, onTasksUpdated: (List<Task>) -> Unit) {
    // 1. Correct Date Calculation
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val dateString = "${now.dayOfMonth.toString().padStart(2, '0')}.${now.monthNumber.toString().padStart(2, '0')}.${now.year}"

    LaunchedEffect(Unit) { onTasksUpdated(Repo.tasks()) }

    fun logout() = onNavigate("login")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigate("addTask") }, containerColor = Color(0xFFFF4081), contentColor = Color.White, shape = RoundedCornerShape(50)) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.fillMaxWidth().background(blue).padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton({ logout() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                        Column {
                            Text("${user?.name ?: "User"}'s Tasks", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // 2. Use the dateString variable
                                Text(dateString, color = Color.White.copy(0.7f), fontSize = 14.sp)
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
            Text("Add New Task", Modifier.fillMaxWidth().padding(32.dp), color = blue, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Task Title") }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))
                TextField(desc, { desc = it }, Modifier.fillMaxWidth(), label = { Text("Task Description") }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))
                ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
                    TextField(priority, {}, Modifier.menuAnchor().fillMaxWidth(), readOnly = true, label = { Text("Priority") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))
                    ExposedDropdownMenu(expanded, { expanded = false }) { listOf("Low", "Medium", "High").forEach { DropdownMenuItem(text = { Text(it) }, onClick = { priority = it; expanded = false }) } }
                }
                Button({ if (title.isNotBlank()) scope.launch { Repo.add(Task(0, title, desc, priority)); onTasksUpdated(Repo.tasks()); onNavigate("tasks") } }, Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(4.dp), colors = ButtonDefaults.buttonColors(blue)) { Text("SAVE TASK", fontWeight = FontWeight.Bold) }
                OutlinedButton({ onNavigate("tasks") }, Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(4.dp)) { Text("CANCEL", color = Color.

                Gray) }
            }
        }
    }
}

@Composable
internal fun AuthScreen(screen: String, blue: Color, scope: CoroutineScope, storage: SessionStorage, onNavigate: (String) -> Unit, onLoginSuccess: (User, List<Task>) -> Unit) {
    var u by remember { mutableStateOf("") }; var p by remember { mutableStateOf("") }; var n by remember { mutableStateOf("") }
    var e by remember { mutableStateOf("") }; var cp by remember { mutableStateOf("") }; var vis by remember { mutableStateOf(false) }
    var vis2 by remember { mutableStateOf(false)}
    var err by remember { mutableStateOf("") }; var load by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), Alignment.Center) {
        Card(Modifier.fillMaxWidth().padding(24.dp), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color.White)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(screen == "login") "Welcome Back" else "Create Account", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(if(screen == "login") "Sign in to your account below" else "Sign up for a new account", color = Color.Gray, fontSize = 14.sp)

                Spacer(Modifier.height(16.dp))
                if (screen != "login") {
                    Text("Full Name", Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(n, { n = it }, Modifier.fillMaxWidth(), placeholder = { Text("Enter your name") })
                    Spacer(Modifier.height(8.dp))
                }

                Text("Username", Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(u, { u = it }, Modifier.fillMaxWidth(), placeholder = { Text("Enter your username") })
                // Add this block for the Email field
                if (screen != "login") {
                    Spacer(Modifier.height(8.dp))
                    Text("Email", Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = e,
                        onValueChange = { e = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your email") }
                    )
                }



                Spacer(Modifier.height(8.dp))
                Text("Password", Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(p, { p = it }, Modifier.fillMaxWidth(), placeholder = { Text("Enter your password") }, visualTransformation = if (vis) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton({ vis = !vis }) { Icon(if (vis) Icons.Default.Visibility else Icons.Default.VisibilityOff, null) } })

                if (screen != "login") {
                    Spacer(Modifier.height(8.dp))
                    Text("Confirm Password", Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = cp,
                        onValueChange = { cp = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Confirm your password") },
                        // Use the new 'vis2' variable here
                        visualTransformation = if (vis2) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton({ vis2 = !vis2 }) {
                                Icon(if (vis2) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                            }
                        }
                    )
                }
                }
                if (err.isNotEmpty()) Text(err, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp), textAlign = TextAlign.Center)

                if (screen == "login") {
                    // We keep the Row with Arrangement.End to push the button to the right
                    Row(Modifier.fillMaxWidth(), Arrangement.End, Alignment.CenterVertically) {
                        TextButton({ onNavigate("forgotPassword") }) {
                            Text("Forgot password?", color = blue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        // 1. Validation Checks
                        if (u.isBlank()) {
                            err = "Enter username"
                            return@Button
                        }
                        if (p.isBlank()) {
                            err = "Enter password"
                            return@Button
                        }
                        if (screen != "login" && p != cp) {
                            err = "Passwords mismatch"
                            return@Button
                        }

                        // 2. Start Loading
                        load = true
                        scope.launch {
                            // 3. ACTUAL LOGIN/REGISTER CALL (This was missing)
                            val res = if (screen == "login") Repo.login(u, p, isGoogle = false) else Repo.register(n, u, e, p)

                            if (res is AuthResponse.Success) {
                                storage.save(res.auth.user.username, res.auth.user.name, res.auth.user.email, res.auth.token)
                                Repo.token = res.auth.token
                                onLoginSuccess(User(res.auth.user.name, res.auth.user.username, res.auth.user.email), Repo.tasks())
                            } else {
                                // Show the error from backend
                                err = (res as AuthResponse.Error).message
                            }

                            // 4. STOP LOADING (Always stop loading when finished)
                            load = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(blue)
                ) {
                    Text(if (load) "Loading..." else if (screen == "login") "Login" else "Register")
                }

                if (screen == "login") {
                    Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        HorizontalDivider(Modifier.weight(1f)) ; Text(" or ", color = Color.LightGray) ; HorizontalDivider(Modifier.weight(1f))
                    }
                    OutlinedButton(
                        onClick = {
                            googleLogin(scope) { result ->
                                val email = result as? String
                                if (email != null && email.endsWith("@bhrish.com")) {
                                    scope.launch {
                                        val res = Repo.login(email, "google_auto_login", isGoogle = true)
                                        if (res is AuthResponse.Success) {
                                            storage.save(res.auth.user.username, res.auth.user.name, res.auth.user.email, res.auth.token)
                                            Repo.token = res.auth.token
                                            onLoginSuccess(User(res.auth.user.name, res.auth.user.username, res.auth.user.email), Repo.tasks())
                                        } else err = (res as AuthResponse.Error).message
                                    }
                                } else if (email != null) {
                                    err = "Please register first to access the application"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_google),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Continue with Google", color = Color.Black)
                    }
                }

                TextButton({ onNavigate(if (screen == "login") "register" else "login"); err = "" }, Modifier.padding(top = 16.dp)) {
                    Text(if (screen == "login") "Don't have an account? Register now" else "Already have an account? Login", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }


@Composable
internal fun ForgotPasswordScreen(blue: Color, scope: CoroutineScope, onNavigate: (String) -> Unit) {
    var e by remember { mutableStateOf("") }; var np by remember { mutableStateOf("") }; var cp by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }; var err by remember { mutableStateOf("") }; var load by remember { mutableStateOf(false) }
    var vis by remember { mutableStateOf(false) }
    var vis2 by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), Alignment.Center) {
        Card(Modifier.fillMaxWidth().padding(24.dp), RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(Color.White)) {
            Column(Modifier.padding(24.dp), Arrangement.spacedBy(12.dp)) {
                Text("Reset Password", color = blue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(e, { e = it }, Modifier.fillMaxWidth(), label = { Text("Email Address") })
                // For New Password
                OutlinedTextField(
                    value = np,
                    onValueChange = { np = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("New Password") },
                    visualTransformation = if (vis) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton({ vis = !vis }) {
                            Icon(if (vis) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                        }
                    }
                )

// For Confirm New Password

                OutlinedTextField(
                    value = cp,
                    onValueChange = { cp = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Confirm New Password") },
                    visualTransformation = if (vis2) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton({ vis2 = !vis2 }) {
                            Icon(if (vis2) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                        }
                    }
                )
                if (err.isNotEmpty()) Text(err, color = Color.Red, fontSize = 12.sp)
                if (msg.isNotEmpty()) Text(msg, color = Color.Green, fontSize = 12.sp)
                Button({
                    if (np != cp) { err = "Passwords mismatch"; return@Button }
                    load = true; scope.launch {
                    val res = Repo.reset(e, np)
                    when (res) {
                        is ResetResponse.Success -> {
                            msg = "Success!"; delay(1500); onNavigate("login")
                        }
                        is ResetResponse.Error -> {
                            err = res.message; msg = ""
                        }
                    }
                    load = false
                }
                }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(blue)) { Text(if (load) "Processing..." else "Reset Password") }
                TextButton({ onNavigate("login") }, Modifier.align(Alignment.CenterHorizontally)) { Text("Back to Login") }
            }
        }
    }
}