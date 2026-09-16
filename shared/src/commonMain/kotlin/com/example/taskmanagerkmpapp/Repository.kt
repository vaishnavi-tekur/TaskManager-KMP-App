package com.example.taskmanagerkmpapp
import kotlinx.serialization.Serializable
internal data class User(val name:String, val username:String, val email:String)
internal data class Task(val id: Long, val title: String, val description: String, val completed: Boolean)
internal object Repo {
    private val api = BackendApi(); var token = ""
    suspend fun login(u:String,p:String,g:Boolean=false) = api.login(u,p,g)
    suspend fun register(n:String,u:String,e:String,p:String) = api.register(RegisterBody(n,u,e,p))
    suspend fun reset(e:String,np:String) = api.reset(ResetBody(e,np))
    suspend fun deleteAccount() = api.deleteAccount(token)
    suspend fun getTasks() = api.getTasks(token).map { Task(it.id, it.title, it.description, it.completed) }
    suspend fun addTask(t:String, d:String) = api.addTask(token, t, d)
    suspend fun updateTask(t:Task) = api.updateTask(token, ApiTask(t.id, t.title, t.description, t.completed))
    suspend fun deleteTask(id:Long) = api.deleteTask(token, id)
}
