package com.example.taskmanagerkmpapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class User(val name: String, val username: String, val email: String)
private object Auth {
    private val users = mutableMapOf("admin" to User("Jane Doe", "admin", "jane@example.com"))
    private val passwords = mutableMapOf("admin" to "password123")
    fun login(user: String, password: String) = users[user]?.takeIf { passwords[user] == password }
    fun register(name: String, user: String, email: String, password: String): User? {
        if (user.isBlank() || users.containsKey(user)) return null
        val result = User(name, user, email)
        users[user] = result; passwords[user] = password
        return result
    }
}

@Composable
fun App() {
    val storage = remember { sessionStorage() }
    var user by remember {
        mutableStateOf(storage.read("token").takeIf { it.isNotBlank() }?.let {
            User(storage.read("name"), storage.read("user"), storage.read("email"))
        })
    }
    var register by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberLogin by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }
    val blue = Color(0xFF1A237E)

    MaterialTheme {
        Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
            Card(Modifier.fillMaxWidth().padding(24.dp), shape = RoundedCornerShape(18.dp)) {
                if (user == null) Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (register) "Create account" else "Welcome back", color = blue, fontSize = 28.sp)
                    if (register) OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Name") }, singleLine = true)
                    OutlinedTextField(username, { username = it }, Modifier.fillMaxWidth(), label = { Text("Username") }, singleLine = true)
                    if (register) OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true)
                    OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
                    if (!register) Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(rememberLogin, { rememberLogin = it }); Text("Remember me")
                    }
                    if (message.isNotBlank()) Text(message, color = Color(0xFFB00020))
                    Button(onClick = {
                        val result = if (register) Auth.register(name, username, email, password) else Auth.login(username, password)
                        if (result == null) message = "Please check your details"
                        else {
                            user = result; message = ""
                            if (rememberLogin) storage.save(result.username, result.name, result.email, "session-${result.username}")
                        }
                    }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = blue)) {
                        Text(if (register) "Register" else "Login", color = Color.White)
                    }
                    TextButton(onClick = { register = !register; message = "" }) {
                        Text(if (register) "Already registered? Login" else "New user? Register", color = blue)
                    }
                } else Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Welcome, ${user?.name}", color = blue, fontSize = 26.sp)
                    Text("You are logged in as ${user?.username}.")
                    OutlinedButton(onClick = { user = null; storage.clear() }, Modifier.fillMaxWidth()) { Text("Logout") }
                }
            }
        }
    }
}