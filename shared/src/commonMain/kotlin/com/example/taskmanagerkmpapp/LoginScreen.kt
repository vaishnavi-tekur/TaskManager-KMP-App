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
    var isRegisterMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("admin") }
    var email by remember { mutableStateOf("jane@example.com") }
    var password by remember { mutableStateOf("password123") }
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
                    text = if (isRegisterMode) "Create account" else "Welcome back",
                    color = darkBlue,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (isRegisterMode) "Register to continue" else "Sign in to continue",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                if (isRegisterMode) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (isRegisterMode) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

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
                        if (isRegisterMode) {
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
                        } else {
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
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = darkBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isLoading) {
                            if (isRegisterMode) "Creating account..." else "Signing in..."
                        } else {
                            if (isRegisterMode) "Register" else "Login"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = { isRegisterMode = !isRegisterMode; errorMessage = "" },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = if (isRegisterMode) "Already have an account? Login" else "New user? Register",
                        color = darkBlue
                    )
                }
            }
        }
    }
}
