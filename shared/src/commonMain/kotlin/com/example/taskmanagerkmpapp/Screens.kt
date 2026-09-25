package com.example.taskmanagerkmpapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.datetime.Clock


import taskmanagerkmpapp.shared.generated.resources.ic_google
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant

@OptIn(ExperimentalMaterial3Api::class)
private fun formatMillis(millis: Long?): String {
    if (millis == null) return ""
    val instant = Instant.fromEpochMilliseconds(millis)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val month = dateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    
    val amPm = if (dateTime.hour < 12) "AM" else "PM"
    val hour12 = when {
        dateTime.hour == 0 -> 12
        dateTime.hour > 12 -> dateTime.hour - 12
        else -> dateTime.hour
    }
    
    val hour = hour12.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "${dateTime.dayOfMonth} $month ${dateTime.year}, $hour:$minute $amPm"
}

@Composable
internal fun TaskListScreen(user: User?, items: List<Task>, blue: Color, scope: CoroutineScope, onNavigate: (String) -> Unit, onTasksUpdated: (List<Task>) -> Unit) {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val day = now.dayOfMonth.toString().padStart(2, '0')
    val monthInt = now.monthNumber
    val month = monthInt.toString().padStart(2, '0')
    val dateString = "$day.$month.${now.year}"

    val rawName = user?.name?.takeIf { it.isNotBlank() } ?: "User"
    val firstName = rawName.split(".", " ", "_", "-").firstOrNull { it.isNotBlank() }?.replaceFirstChar { it.uppercase() } ?: "User"

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
            Column(Modifier.fillMaxWidth().background(blue).padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton({ logout() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                        Spacer(Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "${firstName.uppercase()}'s Tasks",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(dateString, color = Color.White.copy(0.7f), fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.width(75.dp).height(65.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${items.size}",
                                color = blue,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tasks",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
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
                                Text(
                                    text = task.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    style = if (task.done) TextStyle(textDecoration = TextDecoration.LineThrough) else TextStyle.Default,
                                    color = if (task.done) Color.Gray else Color.Unspecified
                                )
                                Text(
                                    text = task.description,
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    style = if (task.done) TextStyle(textDecoration = TextDecoration.LineThrough) else TextStyle.Default
                                )
                                if (task.dueDate != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Event, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                                        Spacer(Modifier.width(4.dp))
                                        Text(formatMillis(task.dueDate), fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
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
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }

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
                
                OutlinedButton(onClick = { showDatePicker = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp)) {
                    Icon(Icons.Default.Schedule, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (selectedDate == null) "Set Date & Time" else formatMillis(selectedDate))
                }

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            scope.launch {
                                // Just one call to add, passing the selectedDate
                                Repo.add(Task(0, title, desc, priority, dueDate = selectedDate))
                                onTasksUpdated(Repo.tasks())
                                onNavigate("tasks")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(blue)
                ) {
                    Text("SAVE TASK", fontWeight = FontWeight.Bold)
                }
                OutlinedButton({ onNavigate("tasks") }, Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(4.dp)) { Text("CANCEL", color = Color.Gray) }
            }
        }
        
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        selectedDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                        showTimePicker = true
                    }) { Text("Next") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showTimePicker) {
            val timePickerState = rememberTimePickerState()
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val dateMillis = selectedDate ?: 0L
                        val utcDate = Instant.fromEpochMilliseconds(dateMillis).toLocalDateTime(TimeZone.UTC).date
                        val localDateTime = LocalDateTime(
                            year = utcDate.year,
                            month = utcDate.month,
                            dayOfMonth = utcDate.dayOfMonth,
                            hour = timePickerState.hour,
                            minute = timePickerState.minute
                        )
                        selectedDate = localDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                        showTimePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Back") }
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Select Time", Modifier.padding(bottom = 16.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        TimePicker(state = timePickerState)
                    }
                }
            )
        }
    }
}

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")

private fun isValidEmail(email: String): Boolean {
    val trimmed = email.trim()
    return trimmed.isNotEmpty() && EMAIL_REGEX.matches(trimmed)
}

@Composable
internal fun AuthScreen(screen: String, blue: Color, scope: CoroutineScope, storage: SessionStorage, onNavigate: (String) -> Unit, onLoginSuccess: (User, List<Task>) -> Unit) {
    var u by remember { mutableStateOf("") }; var p by remember { mutableStateOf("") }; var n by remember { mutableStateOf("") }
    var e by remember { mutableStateOf("") }; var cp by remember { mutableStateOf("") }; var vis by remember { mutableStateOf(false) }
    var vis2 by remember { mutableStateOf(false)}
    var err by remember { mutableStateOf("") }; var load by remember { mutableStateOf(false) }

    var showAccountPicker by remember { mutableStateOf(false) }
    var showCustomEmailInput by remember { mutableStateOf(false) }
    var customEmailText by remember { mutableStateOf("") }
    var pickerError by remember { mutableStateOf("") }

    var showPasswordChallengeDialog by remember { mutableStateOf(false) }
    var challengeUserName by remember { mutableStateOf("") }
    var challengeUserEmail by remember { mutableStateOf("") }
    var challengePassword by remember { mutableStateOf("") }
    var challengeError by remember { mutableStateOf("") }

    LaunchedEffect(screen) {
        err = ""
    }

    fun performGoogleLogin(selectedEmail: String) {
        err = ""
        load = true
        scope.launch {
            val res = Repo.login(selectedEmail, "google_auto_login", isGoogle = true)
            if (res is AuthResponse.Success) {
                storage.addSavedEmail(selectedEmail)
                storage.save(res.auth.user.username, res.auth.user.name, res.auth.user.email, res.auth.token)
                Repo.token = res.auth.token
                onLoginSuccess(User(res.auth.user.name, res.auth.user.username, res.auth.user.email), Repo.tasks())
            } else {
                val errMsg = (res as AuthResponse.Error).message
                if (errMsg.startsWith("REQUIRE_PASSWORD:")) {
                    val parts = errMsg.removePrefix("REQUIRE_PASSWORD:").split(":")
                    val namePart = parts.getOrNull(0) ?: ""
                    val emailPart = parts.getOrNull(1) ?: selectedEmail
                    challengeUserName = namePart
                    challengeUserEmail = emailPart
                    challengePassword = ""
                    challengeError = ""
                    showPasswordChallengeDialog = true
                } else {
                    err = errMsg
                }
            }
            load = false
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), Alignment.Center) {
        Card(Modifier.fillMaxWidth().padding(24.dp), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color.White)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(screen == "login") "Welcome Back" else "Create Account", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(if(screen == "login") "Sign in to your account below" else "Sign up for a new account", color = Color.Gray, fontSize = 14.sp)

                Spacer(Modifier.height(16.dp))
                if (screen != "login") {
                    Text("Full Name", Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(n, { n = it; err = "" }, Modifier.fillMaxWidth(), placeholder = { Text("Enter your name") })
                    Spacer(Modifier.height(8.dp))
                }

                Text("Username", Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(u, { u = it; err = "" }, Modifier.fillMaxWidth(), placeholder = { Text("Enter your username") })

                if (screen != "login") {
                    Spacer(Modifier.height(8.dp))
                    Text("Email", Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(e, { e = it; err = "" }, Modifier.fillMaxWidth(), placeholder = { Text("Enter your email") })
                }

                Spacer(Modifier.height(8.dp))
                Text("Password", Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(p, { p = it; err = "" }, Modifier.fillMaxWidth(), placeholder = { Text("Enter your password") }, visualTransformation = if (vis) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton({ vis = !vis }) { Icon(if (vis) Icons.Default.Visibility else Icons.Default.VisibilityOff, null) } })

                if (screen != "login") {
                    Spacer(Modifier.height(8.dp))
                    Text("Confirm Password", Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = cp,
                        onValueChange = { cp = it; err = "" },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Confirm your password") },
                        visualTransformation = if (vis2) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton({ vis2 = !vis2 }) {
                                Icon(if (vis2) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                            }
                        }
                    )
                }

                if (err.isNotEmpty()) Text(err, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp), textAlign = TextAlign.Center)

                if (screen == "login") {
                    Row(Modifier.fillMaxWidth(), Arrangement.End, Alignment.CenterVertically) {
                        TextButton({ onNavigate("forgotPassword") }) {
                            Text("Forgot password?", color = blue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        err = ""
                        if (u.isBlank()) { err = "Enter username and password"; return@Button }
                        if (screen != "login" && e.isBlank()) { err = "Enter email"; return@Button }
                        if (p.isBlank()) { err = "Enter password"; return@Button }
                        if (screen != "login" && p != cp) { err = "Passwords mismatch"; return@Button }
                        if (screen != "login" && p.length < 6) { err = "Password is too short (minimum 6 characters)."; return@Button }

                        load = true
                        scope.launch {
                            val res = if (screen == "login") Repo.login(u, p, isGoogle = false) else Repo.register(n, u, e, p)
                            if (res is AuthResponse.Success) {
                                storage.save(res.auth.user.username, res.auth.user.name, res.auth.user.email, res.auth.token)
                                Repo.token = res.auth.token
                                onLoginSuccess(User(res.auth.user.name, res.auth.user.username, res.auth.user.email), Repo.tasks())
                            } else {
                                err = (res as AuthResponse.Error).message
                            }
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
                            if (load) return@OutlinedButton
                            err = ""
                            load = true
                            googleLogin(scope) { result ->
                                val email = result
                                if (email != null) {
                                    if (email == "SHOW_PICKER") {
                                        load = false
                                        showAccountPicker = true
                                        return@googleLogin
                                    }
                                    if (email.startsWith("ERROR:")) {
                                        err = email.removePrefix("ERROR:")
                                        load = false
                                        return@googleLogin
                                    }
                                    println("UI: Native Google login successful for $email")
                                    performGoogleLogin(email)
                                } else {
                                    load = false
                                    showAccountPicker = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        if (load) {
                            Text("Connecting...", color = Color.Black)
                        } else {
                            Icon(painter = painterResource(Res.drawable.ic_google), contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Unspecified)
                            Spacer(Modifier.width(12.dp))
                            Text("Continue with Google", color = Color.Black)
                        }
                    }
                }

                TextButton({ onNavigate(if (screen == "login") "register" else "login"); err = "" }, Modifier.padding(top = 16.dp)) {
                    Text(if (screen == "login") "Don't have an account? Register now" else "Already have an account? Login", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        if (showAccountPicker) {
            var savedEmailsList by remember { mutableStateOf(storage.getSavedEmails()) }

            AlertDialog(
                onDismissRequest = {
                    showAccountPicker = false
                    showCustomEmailInput = false
                    load = false
                },
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_google),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Choose an account", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("to continue to Task Manager", fontSize = 13.sp, color = Color.Gray)
                    }
                },
                text = {
                    Column(
                        Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!showCustomEmailInput) {
                            if (savedEmailsList.isEmpty()) {
                                Text(
                                    "No saved accounts found. Please add an account below.",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                )
                            } else {
                                savedEmailsList.forEach { emailItem ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f).clickable {
                                                    showAccountPicker = false
                                                    performGoogleLogin(emailItem)
                                                }.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color(0xFFE8F0FE),
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = emailItem.take(1).uppercase(),
                                                            fontWeight = FontWeight.Bold,
                                                            color = blue,
                                                            fontSize = 16.sp
                                                        )
                                                    }
                                                }
                                                Spacer(Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = emailItem.substringBefore("@").replace(".", " "),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = emailItem,
                                                        fontSize = 11.sp,
                                                        color = Color.Gray,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }

                                            IconButton(
                                                onClick = {
                                                    storage.removeSavedEmail(emailItem)
                                                    savedEmailsList = storage.getSavedEmails()
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Remove account",
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(Modifier.padding(vertical = 4.dp))

                            Card(
                                onClick = { showCustomEmailInput = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        tint = blue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Text("Use another account", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = blue)
                                }
                            }
                        } else {
                            Column {
                                OutlinedTextField(
                                    value = customEmailText,
                                    onValueChange = { customEmailText = it; pickerError = "" },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Google Email") },
                                    placeholder = { Text("e.g. user@gmail.com") },
                                    singleLine = true,
                                    isError = pickerError.isNotEmpty()
                                )
                                if (pickerError.isNotEmpty()) {
                                    Text(
                                        text = pickerError,
                                        color = Color.Red,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    if (showCustomEmailInput) {
                        TextButton(onClick = {
                            val emailToUse = customEmailText.trim().lowercase()
                            if (!isValidEmail(emailToUse)) {
                                pickerError = "Please enter a valid email address (e.g. user@gmail.com)."
                                return@TextButton
                            }
                            pickerError = ""
                            showAccountPicker = false
                            showCustomEmailInput = false
                            performGoogleLogin(emailToUse)
                        }) {
                            Text("Continue", fontWeight = FontWeight.Bold, color = blue)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        if (showCustomEmailInput) {
                            showCustomEmailInput = false
                        } else {
                            showAccountPicker = false
                            load = false
                        }
                    }) {
                        Text(if (showCustomEmailInput) "Back" else "Cancel", color = Color.Gray)
                    }
                }
            )
        }

        if (showPasswordChallengeDialog) {
            AlertDialog(
                onDismissRequest = {
                    showPasswordChallengeDialog = false
                    challengePassword = ""
                    challengeError = ""
                    load = false
                },
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_google),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Hi ${challengeUserName.ifBlank { challengeUserEmail.substringBefore("@") }}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF1F3F4),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = challengeUserEmail,
                                fontSize = 12.sp,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        var vis by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = challengePassword,
                            onValueChange = { challengePassword = it; challengeError = "" },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Enter your password") },
                            placeholder = { Text("Enter password") },
                            singleLine = true,
                            visualTransformation = if (vis) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { vis = !vis }) {
                                    Icon(
                                        if (vis) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            isError = challengeError.isNotEmpty()
                        )
                        if (challengeError.isNotEmpty()) {
                            Text(
                                text = challengeError,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (challengePassword.isBlank()) {
                                challengeError = "Enter your password"
                                return@Button
                            }
                            challengeError = ""
                            load = true
                            scope.launch {
                                val res = Repo.login(challengeUserEmail, challengePassword, isGoogle = true)
                                if (res is AuthResponse.Success) {
                                    showPasswordChallengeDialog = false
                                    storage.addSavedEmail(challengeUserEmail)
                                    storage.save(res.auth.user.username, res.auth.user.name, res.auth.user.email, res.auth.token)
                                    Repo.token = res.auth.token
                                    onLoginSuccess(User(res.auth.user.name, res.auth.user.username, res.auth.user.email), Repo.tasks())
                                } else {
                                    challengeError = (res as AuthResponse.Error).message
                                }
                                load = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(blue),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Next", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showPasswordChallengeDialog = false
                        challengePassword = ""
                        challengeError = ""
                        load = false
                    }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
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
            @Suppress("UNUSED_EXPRESSION")
            Column(Modifier.padding(24.dp), Arrangement.spacedBy(12.dp)) {
                Text("Reset Password", color = blue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(e, { e = it }, Modifier.fillMaxWidth(), label = { Text("Email Address") })
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
                    if (np.length < 6) { err = "Password is too short (minimum 6 characters)."; return@Button }
                    load = true; scope.launch {
                        val res = Repo.reset(e, np)
                        when (res) {
                            is ResetResponse.Success -> {
                                msg = "Password changed succesfully!"; delay(1500); onNavigate("login")
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
