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
    suspend fun resetPasswordByEmail(email: String, newPassword: String): ForgotPasswordResponse
}

@Serializable
data class ForgotPasswordRequest(
    val email: String,
    val newPassword: String
)

@Serializable
data class ForgotPasswordResponse(
    val success: Boolean,
    val message: String = ""
)

object MockAuthRepository : AuthRepository {
    private val users = AppDataStorage.loadUsers().toMutableMap()
    private val passwords = AppDataStorage.loadPasswords().toMutableMap()
    private val emailIndex = mutableMapOf<String, String>()

    init {
        if (users.isEmpty()) {
            val defaultUser = UserProfile(
                id = "user_101",
                username = "admin",
                name = "Jane Doe",
                email = "jane@example.com"
            )
            users[defaultUser.username] = defaultUser
            passwords[defaultUser.username] = "password123"
            emailIndex[defaultUser.email.lowercase()] = defaultUser.username
            persistData()
        } else {
            users.forEach { (username, user) ->
                emailIndex[user.email.lowercase()] = username
            }
        }
    }

    private fun persistData() {
        AppDataStorage.saveUsers(users)
        AppDataStorage.savePasswords(passwords)
    }

    override suspend fun login(username: String, password: String): LoginResponse {
        val trimmedUsername = username.trim()
        val storedUser = users[trimmedUsername]
        val storedPassword = passwords[trimmedUsername]

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

        val normalizedEmail = trimmedEmail.lowercase()
        if (emailIndex.containsKey(normalizedEmail)) {
            return RegisterResponse(success = false, message = "Email already registered")
        }

        val newUser = UserProfile(
            id = "user_${users.size + 101}",
            username = trimmedUsername,
            name = trimmedName,
            email = trimmedEmail
        )

        users[trimmedUsername] = newUser
        passwords[trimmedUsername] = password
        emailIndex[normalizedEmail] = trimmedUsername
        persistData()

        return RegisterResponse(
            success = true,
            message = "Registration successful",
            user = newUser
        )
    }

    override suspend fun resetPasswordByEmail(email: String, newPassword: String): ForgotPasswordResponse {
        val normalizedEmail = email.trim().lowercase()

        if (normalizedEmail.isBlank() || newPassword.isBlank()) {
            return ForgotPasswordResponse(success = false, message = "Email and new password are required")
        }

        val username = emailIndex[normalizedEmail] ?: return ForgotPasswordResponse(
            success = false,
            message = "No account found for this email"
        )

        if (newPassword.length < 6) {
            return ForgotPasswordResponse(success = false, message = "Password must be at least 6 characters")
        }

        passwords[username] = newPassword
        persistData()
        return ForgotPasswordResponse(success = true, message = "Password updated successfully")
    }
}
