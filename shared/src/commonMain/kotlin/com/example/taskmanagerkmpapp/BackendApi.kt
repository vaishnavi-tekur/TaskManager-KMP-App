package com.example.taskmanagerkmpapp
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
expect fun getBackendEngine(): io.ktor.client.engine.HttpClientEngine
expect fun backendUrl(): String
@Serializable data class ApiUser(val id: Long, val name: String, val username: String, val email: String)
@Serializable data class LoginBody(val username: String, val password: String, val isGoogle: Boolean = false)
@Serializable data class RegisterBody(val name: String, val username: String, val email: String, val password: String)
@Serializable data class ResetBody(val email: String, val newPassword: String)
@Serializable data class AuthBody(val token: String, val user: ApiUser)
@Serializable data class MessageBody(val message: String)
@Serializable data class ApiTask(val id: Long, val title: String, val description: String, val completed: Boolean)
class BackendApi {
    private val client = HttpClient(getBackendEngine()) { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }) } }
    suspend fun getTasks(t: String): List<ApiTask> = try { client.get("${backendUrl()}/tasks") { auth(t) }.body() } catch (e: Exception) { emptyList() }
    suspend fun addTask(t: String, ti: String, d: String): Boolean = try { client.post("${backendUrl()}/tasks") { auth(t); contentType(ContentType.Application.Json); setBody(ApiTask(0, ti, d, false)) }.status == HttpStatusCode.Created } catch (e: Exception) { false }
    suspend fun updateTask(t: String, ta: ApiTask): Boolean = try { client.put("${backendUrl()}/tasks/${ta.id}") { auth(t); contentType(ContentType.Application.Json); setBody(ta) }.status == HttpStatusCode.OK } catch (e: Exception) { false }
    suspend fun deleteTask(t: String, id: Long): Boolean = try { client.delete("${backendUrl()}/tasks/$id") { auth(t) }.status == HttpStatusCode.OK } catch (e: Exception) { false }
    suspend fun login(u: String, p: String, g: Boolean = false): AuthResponse = try { val r = client.post("${backendUrl()}/login") { contentType(ContentType.Application.Json); setBody(LoginBody(u, p, g)) }; if (r.status == HttpStatusCode.OK) AuthResponse.Success(r.body()) else AuthResponse.Error(r.bOrM()) } catch (e: Exception) { AuthResponse.Error("Network error") }
    suspend fun register(b: RegisterBody): AuthResponse = try { val r = client.post("${backendUrl()}/register") { contentType(ContentType.Application.Json); setBody(b) }; if (r.status == HttpStatusCode.Created || r.status == HttpStatusCode.OK) login(b.username, b.password) else AuthResponse.Error(r.bOrM()) } catch (e: Exception) { AuthResponse.Error("Network error") }
    suspend fun reset(b: ResetBody): Boolean = try { client.post("${backendUrl()}/forgot-password") { contentType(ContentType.Application.Json); setBody(b) }.status == HttpStatusCode.OK } catch (e: Exception) { false }
    suspend fun deleteAccount(t: String): Boolean = try { client.delete("${backendUrl()}/delete-account") { auth(t) }.status == HttpStatusCode.OK } catch (e: Exception) { false }
    private fun HttpRequestBuilder.auth(t: String) { header("Authorization", "Bearer $t") }
    private suspend fun HttpResponse.bOrM(): String = try { val t = bodyAsText(); if (t.contains("\"message\"")) Json { ignoreUnknownKeys = true }.decodeFromString<MessageBody>(t).message else t.ifBlank { "Error $status" } } catch (e: Exception) { "Error $status" }
}
sealed class AuthResponse { data class Success(val auth: AuthBody) : AuthResponse(); data class Error(val message: String) : AuthResponse() }
