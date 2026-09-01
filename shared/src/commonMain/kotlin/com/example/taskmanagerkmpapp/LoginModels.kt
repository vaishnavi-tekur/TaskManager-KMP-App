package com.example.taskmanagerkmpapp

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class UserProfile(
    val id: String,
    val username: String,
    val name: String,
    val email: String
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String = "",
    val user: UserProfile? = null,
    val token: String? = null
)

interface AuthRepository {
    suspend fun login(username: String, password: String): LoginResponse
}

class MockAuthRepository : AuthRepository {
    private val validUser = UserProfile(
        id = "user_101",
        username = "admin",
        name = "Jane Doe",
        email = "jane@example.com"
    )

    override suspend fun login(username: String, password: String): LoginResponse {
        return if (username.trim() == validUser.username && password == "password123") {
            LoginResponse(
                success = true,
                message = "Login successful",
                user = validUser,
                token = "mock-token-123"
            )
        } else {
            LoginResponse(
                success = false,
                message = "Invalid username or password"
            )
        }
    }
}
