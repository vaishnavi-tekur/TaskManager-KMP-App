package com.example.taskmanagerkmpapp

import kotlinx.serialization.Serializable

internal data class User(val name:String, val username:String, val email:String)

@Serializable
internal data class Task(val id:Int, val title:String, val description:String, val priority:String = "Medium", val done:Boolean = false)

internal object Repo {
    private val api = BackendApi()
    var token = ""
    suspend fun login(u:String,p:String): AuthResponse = api.login(u,p)
    suspend fun register(n:String,u:String,e:String,p:String): AuthResponse = api.register(RegisterBody(n,u,e,p))
    suspend fun reset(e:String,np:String) = api.reset(ResetBody(e,np))
    suspend fun tasks(): List<Task> = api.tasks(token).map { Task(it.id.toInt(),it.title,it.description,it.priority,it.completed) }
    suspend fun add(task:Task) = api.add(token,TaskBody(task.title,task.description,task.priority))
    suspend fun complete(id:Int,done:Boolean) = api.complete(token,id.toLong(),done)
    suspend fun delete(id:Int) = api.delete(token,id.toLong())
}