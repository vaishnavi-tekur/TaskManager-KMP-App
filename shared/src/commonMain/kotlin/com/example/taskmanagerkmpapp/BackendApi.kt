package com.example.taskmanagerkmpapp

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

expect fun createBackendClient(): HttpClient
expect fun backendUrl(): String

@Serializable data class ApiUser(val id: Long, val name: String, val username: String, val email: String)
@Serializable data class LoginBody(val username: String, val password: String)
@Serializable data class RegisterBody(val name: String, val username: String, val email: String, val password: String)
@Serializable data class ResetBody(val email: String, val newPassword: String)
@Serializable data class AuthBody(val token: String, val user: ApiUser)
@Serializable data class ApiTask(val id: Long, val title: String, val description: String, val priority: String, val completed: Boolean)
@Serializable data class TaskBody(val title: String, val description: String, val priority: String = "Medium")
@Serializable data class CompleteBody(val completed: Boolean)
@Serializable data class MessageBody(val message: String)

class BackendApi(private val client: HttpClient = createBackendClient()) {
    suspend fun login(user: String, password: String): AuthBody? = client.post("${backendUrl()}/login") { setBody(LoginBody(user, password)) }.bodyOrNull()
    suspend fun register(body: RegisterBody): ApiUser? = client.post("${backendUrl()}/register") { setBody(body) }.bodyOrNull()
    suspend fun reset(body: ResetBody): Boolean = client.post("${backendUrl()}/forgot-password") { setBody(body) }.status == HttpStatusCode.OK
    suspend fun tasks(token: String): List<ApiTask> = client.get("${backendUrl()}/tasks") { auth(token) }.bodyOrNull<List<ApiTask>>() ?: emptyList()
    suspend fun add(token: String, body: TaskBody): ApiTask? = client.post("${backendUrl()}/tasks") { auth(token); setBody(body) }.bodyOrNull()
    suspend fun complete(token: String, id: Long, done: Boolean): Boolean = client.put("${backendUrl()}/tasks/$id") { auth(token); setBody(CompleteBody(done)) }.status.value in 200..299
    suspend fun delete(token: String, id: Long): Boolean = client.delete("${backendUrl()}/tasks/$id") { auth(token) }.status.value in 200..299
    private fun io.ktor.client.request.HttpRequestBuilder.auth(token: String) { header("Authorization", "Bearer $token") }
    private suspend inline fun <reified T> io.ktor.client.statement.HttpResponse.bodyOrNull(): T? = try { if (status.value in 200..299) body() else null } catch (e: Exception) { null }
}
