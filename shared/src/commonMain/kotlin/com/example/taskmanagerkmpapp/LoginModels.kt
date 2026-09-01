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

@Serializable
data class RegisterRequest(
    val name: String,
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class RegisterResponse(
    val success: Boolean,
    val message: String = "",
    val user: UserProfile? = null
)

interface AuthRepository {
    suspend fun login(username: String, password: String): LoginResponse
    suspend fun register(name: String, username: String, email: String, password: String): RegisterResponse
}

class MockAuthRepository : AuthRepository {
    private val users = mutableMapOf<String, UserProfile>()
    private val passwords = mutableMapOf<String, String>()

    init {
        val defaultUser = UserProfile(
            id = "user_101",
            username = "admin",
            name = "Jane Doe",
            email = "jane@example.com"
        )
        users[defaultUser.username] = defaultUser
        passwords[defaultUser.username] = "password123"
    }

    override suspend fun login(username: String, password: String): LoginResponse {
        val storedUser = users[username.trim()]
        val storedPassword = passwords[username.trim()]

        return if (storedUser != null && storedPassword == password) {
            LoginResponse(
                success = true,
                message = "Login successful",
                user = storedUser,
                token = "mock-token-${storedUser.id}"
            )
        } else {
            LoginResponse(
                success = false,
                message = "Invalid username or password"
            )
        }
    }

    override suspend fun register(name: String, username: String, email: String, password: String): RegisterResponse {
        val trimmedUsername = username.trim()
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()

        if (trimmedUsername.isBlank() || trimmedName.isBlank() || trimmedEmail.isBlank() || password.isBlank()) {
            return RegisterResponse(success = false, message = "All fields are required")
        }

        if (users.containsKey(trimmedUsername)) {
            return RegisterResponse(success = false, message = "Username already exists")
        }

        val newUser = UserProfile(
            id = "user_${users.size + 101}",
            username = trimmedUsername,
            name = trimmedName,
            email = trimmedEmail
        )

        users[trimmedUsername] = newUser
        passwords[trimmedUsername] = password

        return RegisterResponse(
            success = true,
            message = "Registration successful",
            user = newUser
        )
    }
}
