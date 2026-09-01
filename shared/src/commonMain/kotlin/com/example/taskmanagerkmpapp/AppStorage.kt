package com.example.taskmanagerkmpapp

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface AppStorage {
    fun saveString(key: String, value: String)
    fun getString(key: String, defaultValue: String = ""): String
}

object InMemoryAppStorage : AppStorage {
    private val memoryStore = mutableMapOf<String, String>()

    override fun saveString(key: String, value: String) {
        memoryStore[key] = value
    }

    override fun getString(key: String, defaultValue: String): String {
        return memoryStore[key] ?: defaultValue
    }
}

expect fun provideAppStorage(): AppStorage

object AppDataStorage {
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    fun saveUsers(users: Map<String, UserProfile>) {
        val storage = provideAppStorage()
        storage.saveString("task_manager_users", json.encodeToString(users))
    }

    fun loadUsers(): Map<String, UserProfile> {
        val storage = provideAppStorage()
        val raw = storage.getString("task_manager_users")
        if (raw.isBlank()) return emptyMap()
        return try {
            json.decodeFromString(raw)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun savePasswords(passwords: Map<String, String>) {
        val storage = provideAppStorage()
        storage.saveString("task_manager_passwords", json.encodeToString(passwords))
    }

    fun loadPasswords(): Map<String, String> {
        val storage = provideAppStorage()
        val raw = storage.getString("task_manager_passwords")
        if (raw.isBlank()) return emptyMap()
        return try {
            json.decodeFromString(raw)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun saveTasks(tasks: Map<String, List<Task>>) {
        val storage = provideAppStorage()
        storage.saveString("task_manager_tasks", json.encodeToString(tasks))
    }

    fun loadTasks(): Map<String, List<Task>> {
        val storage = provideAppStorage()
        val raw = storage.getString("task_manager_tasks")
        if (raw.isBlank()) return emptyMap()
        return try {
            json.decodeFromString(raw)
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
