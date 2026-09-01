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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private object DemoRepo {
    data class User(val name:String,val username:String,val email:String)
    data class Task(val id:Int,val title:String,val desc:String,val priority:String,val done:Boolean=false)
    val users = mutableMapOf("admin" to User("Jane Doe","admin","jane@example.com"))
    val pass = mutableMapOf("admin" to "password123")
    val tasks = mutableMapOf("admin" to listOf(Task(1,"Project", "Finalize app flow","High"), Task(2,"Groceries","Milk, Eggs","Medium")))
    suspend fun login(u:String,p:String): User? = if (pass[u]==p) users[u] else null
    suspend fun register(name:String,u:String,email:String,p:String): User? {
        if (users.containsKey(u)) return null
        users[u]=User(name,u,email); pass[u]=p; tasks[u] = emptyList(); return users[u]
    }
    suspend fun reset(email:String,newPass:String): Boolean {
        val user = users.values.firstOrNull { it.email == email } ?: return false
        pass[user.username] = newPass; return true
    }
}

@Composable
fun App() {
    var screen by remember { mutableStateOf("login") }
    var user by remember { mutableStateOf<DemoRepo.User?>(null) }
    var mode by remember { mutableStateOf("login") }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("admin") }
    var email by remember { mutableStateOf("jane@example.com") }
    var password by remember { mutableStateOf("password123") }
    var newPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val blue = Color(0xFF1A237E)
    fun saveTask(task: DemoRepo.Task) { val u=user?.username ?: return; val list = DemoRepo.tasks[u] ?: emptyList(); DemoRepo.tasks[u] = list + task }
    fun toggleTask(id:Int) { val u=user?.username ?: return; DemoRepo.tasks[u] = (DemoRepo.tasks[u] ?: emptyList()).map { if (it.id==id) it.copy(done=!it.done) else it } }
    fun deleteTask(id:Int) { val u=user?.username ?: return; DemoRepo.tasks[u] = (DemoRepo.tasks[u] ?: emptyList()).filter { it.id!=id } }

    MaterialTheme {
        Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
            Card(Modifier.fillMaxWidth().padding(horizontal=24.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                if (screen == "login") {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(if (mode == "register") "Create account" else if (mode == "forgot") "Reset password" else "Welcome back", color = blue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        if (mode == "register") OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Full Name") }, singleLine = true)
                        if (mode != "forgot") OutlinedTextField(username, { username = it }, Modifier.fillMaxWidth(), label = { Text("Username") }, singleLine = true)
                        if (mode != "login") OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true)
                        if (mode != "forgot") OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
                        if (mode == "forgot") OutlinedTextField(newPassword, { newPassword = it }, Modifier.fillMaxWidth(), label = { Text("New Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
                        if (error.isNotEmpty()) Text(error, color = Color(0xFFB00020), fontSize = 12.sp)
                        Button(onClick = {
                            scope.launch {
                                val result = when (mode) {
                                    "register" -> DemoRepo.register(name, username, email, password)
                                    "forgot" -> if (DemoRepo.reset(email, newPassword)) DemoRepo.User("","",email) else null
                                    else -> DemoRepo.login(username, password)
                                }
                                when {
                                    mode == "register" && result != null -> { user = result; screen = "tasks" }
                                    mode == "forgot" && result != null -> { mode = "login"; error = "Password updated" }
                                    mode == "login" && result != null -> { user = result; screen = "tasks" }
                                    else -> error = "Invalid input"
                                }
                            }
                        }, Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = blue)) {
                            Text(if (mode == "register") "Register" else if (mode == "forgot") "Update" else "Login", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            TextButton(onClick = { mode = if (mode == "login") "register" else "login"; error = "" }) { Text(if (mode == "login") "New user? Register" else "Back to Login", color = blue) }
                            if (mode == "login") TextButton(onClick = { mode = "forgot"; error = "" }) { Text("Forgot password", color = blue) }
                        }
                    }
                } else {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("${user?.name ?: "User"}'s Tasks", color = blue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        val tasks = DemoRepo.tasks[user?.username ?: ""] ?: emptyList()
                        tasks.forEach { task ->
                            Card(Modifier.fillMaxWidth(), shape=RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F4FF))) {
                                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(Modifier.weight(1f)) { Text(task.title, fontWeight = FontWeight.Bold); Text(task.desc, fontSize = 12.sp, color = Color.Gray) }
                                    Checkbox(task.done, onCheckedChange = { toggleTask(task.id) })
                                }
                            }
                        }
                        Button(onClick = { val next = (tasks.size + 1); saveTask(DemoRepo.Task(next, "New Task", "Add description", "Medium")); }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = blue)) { Text("Add Task", color = Color.White) }
                        OutlinedButton(onClick = { user = null; screen = "login"; mode = "login" }, Modifier.fillMaxWidth()) { Text("Logout") }
                    }
                }
            }
        }
    }
}
