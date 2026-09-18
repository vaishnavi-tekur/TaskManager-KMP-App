package com.example.taskmanagerkmpapp

import kotlinx.serialization.Serializable

internal data class User(val name:String, val username:String, val email:String)

@Serializable
<<<<<<< HEAD
internal data class Task(val id:Int, val title:String, val description:String, val priority:String = "Medium", val done:Boolean = false)
=======
internal data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val priority: String = "Medium",
    val done: Boolean = false,
    val dueDate: Long? = null
)
>>>>>>> task/kmp-task-scheduling

internal object Repo {
    private val api = BackendApi()
    var token = ""
<<<<<<< HEAD
    suspend fun login(u:String, p:String, isGoogle: Boolean = false): AuthResponse = api.login(u, p, isGoogle)
    suspend fun register(n:String,u:String,e:String,p:String): AuthResponse = api.register(RegisterBody(n,u,e,p))
    suspend fun reset(e: String, np: String): ResetResponse = api.reset(ResetBody(e, np))
    suspend fun tasks(): List<Task> = api.getTasks(token).map { Task(it.id.toInt(),it.title,it.description,it.priority,it.completed) }
    suspend fun add(task:Task) = api.addTask(token, task.title, task.description, task.priority)
    suspend fun complete(id:Int,done:Boolean) = api.complete(token,id.toLong(),done)
    suspend fun delete(id:Int) = api.delete(token,id.toLong())
    suspend fun deleteAccount() = api.deleteAccount(token)
=======

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
>>>>>>> task/kmp-task-scheduling
}
