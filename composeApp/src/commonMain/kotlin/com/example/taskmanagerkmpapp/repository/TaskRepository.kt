package com.example.taskmanagerkmpapp.repository

import com.example.taskmanagerkmpapp.model.Task
import com.example.taskmanagerkmpapp.model.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import taskmanagerkmpapp.composeapp.generated.resources.Res

class TaskRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    @OptIn(ExperimentalResourceApi::class)
    suspend fun initialize() {
        try {
            val jsonBytes = Res.readBytes("files/tasks.json")
            val jsonString = jsonBytes.decodeToString()
            val initialTasks = json.decodeFromString<List<Task>>(jsonString)
            _tasks.value = initialTasks
        } catch (e: Exception) {
            println("Error loading tasks: ${e.message}")
        }
    }

    fun addTask(title: String, description: String, priority: Priority) {
        val newId = (_tasks.value.maxOfOrNull { it.id } ?: 0) + 1
        val newTask = Task(
            id = newId,
            title = title,
            description = description,
            isCompleted = false,
            priority = priority
        )
        _tasks.update { it + newTask }
    }

    fun deleteTask(id: Int) {
        _tasks.update { it.filter { task -> task.id != id } }
    }

    fun toggleTaskCompletion(id: Int) {
        _tasks.update { it.map { task ->
            if (task.id == id) task.copy(isCompleted = !task.isCompleted) else task
        }}
    }
}
