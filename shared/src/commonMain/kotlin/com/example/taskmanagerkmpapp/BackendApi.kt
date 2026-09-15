package com.example.taskmanagerkmpapp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

expect fun getBackendEngine(): io.ktor.client.engine.HttpClientEngine
expect fun backendUrl(): String

@Serializable data class ApiUser(val id: Long, val name: String, val username: String, val email: String)
@Serializable data class LoginBody(val username: String, val password: String)
@Serializable data class RegisterBody(val name: String, val username: String, val email: String, val password: String)
@Serializable data class AuthBody(val token: String, val user: ApiUser)
@Serializable data class MessageBody(val message: String)

class BackendApi {
    private val client = HttpClient(getBackendEngine()) { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }) } }
    suspend fun login(u: String, p: String): AuthResponse = try {
        val resp = client.post("${backendUrl()}/login") { contentType(ContentType.Application.Json); setBody(LoginBody(u, p)) }
        if (resp.status == HttpStatusCode.OK) AuthResponse.Success(resp.body<AuthBody>()) else AuthResponse.Error(resp.bodyOrMessage())
    } catch (e: Exception) { AuthResponse.Error("Network error: ${e.message}") }
    suspend fun register(b: RegisterBody): AuthResponse = try {
        val resp = client.post("${backendUrl()}/register") { contentType(ContentType.Application.Json); setBody(b) }
        if (resp.status == HttpStatusCode.Created || resp.status == HttpStatusCode.OK) login(b.username, b.password) else AuthResponse.Error(resp.bodyOrMessage())
    } catch (e: Exception) { AuthResponse.Error("Network error: ${e.message}") }
    private suspend fun HttpResponse.bodyOrMessage(): String = try {
        val t = bodyAsText()
        if (t.contains("\"message\"")) Json { ignoreUnknownKeys = true }.decodeFromString<MessageBody>(t).message else t.ifBlank { "Error $status" }
    } catch (e: Exception) { "Error $status" }
}
sealed class AuthResponse {
    data class Success(val auth: AuthBody) : AuthResponse()
    data class Error(val message: String) : AuthResponse()
}
