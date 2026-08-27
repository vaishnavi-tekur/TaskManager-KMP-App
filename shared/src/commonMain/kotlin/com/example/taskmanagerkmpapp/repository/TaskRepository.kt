package com.example.taskmanagerkmpapp.repository

import com.example.taskmanagerkmpapp.model.Task
import kotlinx.serialization.json.Json

class TaskRepository {
    private val json = Json { ignoreUnknownKeys = true }

    fun parseTasks(jsonString: String): List<Task> {
        return try {
            json.decodeFromString<List<Task>>(jsonString)
        } catch (e: Exception) {
            println("Error parsing tasks: ${e.message}")
            emptyList()
        }
    }
}
