package com.example.taskmanagerkmpapp

import kotlinx.serialization.Serializable

internal data class User(val name:String, val username:String, val email:String)

@Serializable
internal data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val priority: String = "Medium",
    val done: Boolean = false,
    val dueDate: Long? = null
)

internal object Repo {
    private val api = BackendApi()
    var token = ""

    suspend fun login(u: String, p: String, isGoogle: Boolean = false): AuthResponse {
        return api.login(u, p, isGoogle)
    }

    suspend fun register(n: String, u: String, e: String, p: String): AuthResponse {
        return api.register(RegisterBody(n, u, e, p))
    }

    suspend fun reset(e: String, np: String): ResetResponse {
        return api.reset(ResetBody(e, np))
    }

    suspend fun tasks(): List<Task> {
        val apiTasks = api.getTasks(token)
        return apiTasks.map { it: ApiTask ->
            Task(
                id = it.id.toInt(),
                title = it.title,
                description = it.description,
                priority = it.priority,
                done = it.completed,
                dueDate = it.dueDate
            )
        }
    }

    suspend fun add(task: Task): Boolean {
        return api.addTask(
            token = token,
            title = task.title,
            desc = task.description,
            priority = task.priority,
            dueDate = task.dueDate
        )
    }

    suspend fun complete(id: Int, done: Boolean): Boolean {
        return api.complete(
            token = token,
            id = id.toLong(),
            done = done,
            dueDate = null
        )
    }

    suspend fun delete(id: Int): Boolean {
        return api.delete(
            token = token,
            id = id.toLong()
        )
    }

    suspend fun deleteAccount(): Boolean {
        return api.deleteAccount(token)
    }
}
