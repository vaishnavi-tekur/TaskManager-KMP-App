package com.example.taskmanagerkmpapp.model

import kotlinx.serialization.Serializable

@Serializable
enum class Priority {
    High, Medium, Low
}

@Serializable
data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val priority: Priority
)
