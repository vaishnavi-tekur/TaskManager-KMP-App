package com.example.taskmanagerkmpapp.repository

import com.example.taskmanagerkmpapp.model.Task
import com.example.taskmanagerkmpapp.model.Priority
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import taskmanagerkmpapp.composeapp.generated.resources.Res

class TaskRepository {
    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadInitialTasks(): List<Task> {
        return try {
            val jsonBytes = Res.readBytes("files/tasks.json")
            val jsonString = jsonBytes.decodeToString()
            json.decodeFromString<List<Task>>(jsonString)
        } catch (e: Exception) {
            println("Error loading tasks: ${e.message}")
            emptyList()
        }
    }
}
