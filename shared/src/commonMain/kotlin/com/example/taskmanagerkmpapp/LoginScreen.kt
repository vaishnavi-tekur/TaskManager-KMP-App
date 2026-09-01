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

@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onLoginSuccess: (UserProfile) -> Unit
) {
    var currentMode by remember { mutableStateOf("login") }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("admin") }
    var email by remember { mutableStateOf("jane@example.com") }
    var password by remember { mutableStateOf("password123") }
    var newPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val darkBlue = Color(0xFF1A237E)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = when (currentMode) {
                        "register" -> "Create account"
                        "forgot" -> "Reset password"
                        else -> "Welcome back"
                    },
                    color = darkBlue,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = when (currentMode) {
                        "register" -> "Register to continue"
                        "forgot" -> "Enter your email to reset password"
                        else -> "Sign in to continue"
                    },
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                if (currentMode == "register") {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                if (currentMode != "forgot") {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                if (currentMode == "register" || currentMode == "forgot") {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                if (currentMode == "forgot") {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                }

                if (currentMode == "login" || currentMode == "register") {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFB00020),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = {
                        when (currentMode) {
                            "register" -> {
                                if (name.isBlank() || username.isBlank() || email.isBlank() || password.isBlank()) {
                                    errorMessage = "All fields are required."
                                    return@Button
                                }

                                isLoading = true
                                errorMessage = ""

                                scope.launch {
                                    val result = authRepository.register(name, username, email, password)
                                    isLoading = false
                                    if (result.success && result.user != null) {
                                        onLoginSuccess(result.user)
                                    } else {
                                        errorMessage = result.message
                                    }
                                }
                            }
                            "forgot" -> {
                                if (email.isBlank() || newPassword.isBlank()) {
                                    errorMessage = "Email and new password are required."
                                    return@Button
                                }

                                isLoading = true
                                errorMessage = ""

                                scope.launch {
                                    val result = authRepository.resetPasswordByEmail(email, newPassword)
                                    isLoading = false
                                    if (result.success) {
                                        currentMode = "login"
                                        errorMessage = result.message
                                        password = newPassword
                                    } else {
                                        errorMessage = result.message
                                    }
                                }
                            }
                            else -> {
                                if (username.isBlank() || password.isBlank()) {
                                    errorMessage = "Username and password are required."
                                    return@Button
                                }

                                isLoading = true
                                errorMessage = ""

                                scope.launch {
                                    val result = authRepository.login(username, password)
                                    isLoading = false
                                    if (result.success && result.user != null) {
                                        onLoginSuccess(result.user)
                                    } else {
                                        errorMessage = result.message
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = darkBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when (currentMode) {
                            "register" -> if (isLoading) "Creating account..." else "Register"
                            "forgot" -> if (isLoading) "Updating..." else "Reset Password"
                            else -> if (isLoading) "Signing in..." else "Login"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { currentMode = "login"; errorMessage = "" }
                    ) {
                        Text(
                            text = if (currentMode == "login") "New user? Register" else "Back to Login",
                            color = darkBlue
                        )
                    }

                    if (currentMode == "login") {
                        TextButton(
                            onClick = { currentMode = "register"; errorMessage = "" }
                        ) {
                            Text(
                                text = "Register",
                                color = darkBlue
                            )
                        }
                    }

                    if (currentMode == "login") {
                        TextButton(
                            onClick = { currentMode = "forgot"; errorMessage = "" }
                        ) {
                            Text(
                                text = "Forgot password",
                                color = darkBlue
                            )
                        }
                    }
                }
            }
        }
    }
}
