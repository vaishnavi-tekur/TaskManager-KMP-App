package com.example.taskmanagerkmpapp

import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.security.MessageDigest

class Database(private val file: String = "data/taskmanager.db") {
    private val connection: Connection

    init {
        Path.of(file).parent?.let { Files.createDirectories(it) }
        connection = DriverManager.getConnection("jdbc:sqlite:$file")
        connection.createStatement().use { statement ->
            statement.executeUpdate("PRAGMA foreign_keys = ON")
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    username TEXT UNIQUE NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL
                )
            """.trimIndent())
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT NOT NULL,
                    priority TEXT NOT NULL,
                    is_completed INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
                )
            """.trimIndent())
        }
    }

    fun register(request: RegisterRequest): User? {
        connection.prepareStatement("INSERT INTO users(name, username, email, password_hash) VALUES (?, ?, ?, ?)").use { statement ->
            statement.setString(1, request.name.trim())
            statement.setString(2, request.username.trim())
            statement.setString(3, request.email.trim().lowercase())
            statement.setString(4, hash(request.password))
            return try {
                statement.executeUpdate()
                findUser(request.username)
            } catch (_: Exception) {
                null
            }
        }
    }

    fun authenticate(username: String, password: String): User? {
        connection.prepareStatement("SELECT id, name, username, email FROM users WHERE username = ? AND password_hash = ?").use { statement ->
            statement.setString(1, username.trim())
            statement.setString(2, hash(password))
            statement.executeQuery().use { result -> return if (result.next()) result.toUser() else null }
        }
    }

    fun resetPassword(email: String, newPassword: String): Boolean {
        connection.prepareStatement("UPDATE users SET password_hash = ? WHERE email = ?").use { statement ->
            statement.setString(1, hash(newPassword))
            statement.setString(2, email.trim().lowercase())
            return statement.executeUpdate() == 1
        }
    }

    fun tasks(userId: Long): List<Task> = connection.prepareStatement("SELECT id, title, description, priority, is_completed FROM tasks WHERE user_id = ? ORDER BY id").use { statement ->
        statement.setLong(1, userId)
        statement.executeQuery().use { result ->
            buildList { while (result.next()) add(result.toTask()) }
        }
    }

    fun addTask(userId: Long, task: TaskRequest): Task? = connection.prepareStatement("INSERT INTO tasks(user_id, title, description, priority) VALUES (?, ?, ?, ?)", arrayOf("id")).use { statement ->
        statement.setLong(1, userId); statement.setString(2, task.title); statement.setString(3, task.description); statement.setString(4, task.priority)
        statement.executeUpdate()
        statement.generatedKeys.use { keys -> if (keys.next()) Task(keys.getLong(1), task.title, task.description, task.priority, false) else null }
    }

    fun updateTask(userId: Long, id: Long, completed: Boolean): Boolean = connection.prepareStatement("UPDATE tasks SET is_completed = ? WHERE id = ? AND user_id = ?").use { statement ->
        statement.setInt(1, if (completed) 1 else 0); statement.setLong(2, id); statement.setLong(3, userId); statement.executeUpdate() == 1
    }

    fun deleteTask(userId: Long, id: Long): Boolean = connection.prepareStatement("DELETE FROM tasks WHERE id = ? AND user_id = ?").use { statement ->
        statement.setLong(1, id); statement.setLong(2, userId); statement.executeUpdate() == 1
    }

    private fun findUser(username: String): User? = connection.prepareStatement("SELECT id, name, username, email FROM users WHERE username = ?").use { statement ->
        statement.setString(1, username.trim()); statement.executeQuery().use { result -> if (result.next()) result.toUser() else null }
    }

    private fun java.sql.ResultSet.toUser() = User(getLong("id"), getString("name"), getString("username"), getString("email"))
    private fun java.sql.ResultSet.toTask() = Task(getLong("id"), getString("title"), getString("description"), getString("priority"), getInt("is_completed") == 1)

    private fun hash(value: String) = MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
}
