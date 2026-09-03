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
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

expect fun getBackendEngine(): io.ktor.client.engine.HttpClientEngine
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

class BackendApi {
    private val client = HttpClient(getBackendEngine()) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun login(user: String, password: String): AuthResponse = try {
        val resp = client.post("${backendUrl()}/login") { 
            setBody(LoginBody(user, password)) 
        }
        if (resp.status == HttpStatusCode.OK) AuthResponse.Success(resp.body<AuthBody>())
        else AuthResponse.Error(resp.bodyOrMessage())
    } catch (e: Exception) { AuthResponse.Error("Network error: ${e.message}") }

    suspend fun register(body: RegisterBody): AuthResponse = try {
        val resp = client.post("${backendUrl()}/register") { 
            setBody(body) 
        }
        if (resp.status == HttpStatusCode.Created) {
            login(body.username, body.password)
        } else AuthResponse.Error(resp.bodyOrMessage())
    } catch (e: Exception) { AuthResponse.Error("Network error: ${e.message}") }

    suspend fun reset(body: ResetBody): Boolean = try { 
        client.post("${backendUrl()}/forgot-password") { 
            setBody(body) 
        }.status == HttpStatusCode.OK 
    } catch (e: Exception) { false }

    suspend fun tasks(token: String): List<ApiTask> = try { 
        client.get("${backendUrl()}/tasks") { 
            auth(token) 
        }.body<List<ApiTask>>() 
    } catch (e: Exception) { emptyList() }

    suspend fun add(token: String, body: TaskBody): ApiTask? = try { 
        client.post("${backendUrl()}/tasks") { 
            auth(token)
            setBody(body) 
        }.body() 
    } catch (e: Exception) { null }

    suspend fun complete(token: String, id: Long, done: Boolean): Boolean = try { 
        client.put("${backendUrl()}/tasks/$id") { 
            auth(token)
            setBody(CompleteBody(done)) 
        }.status.value in 200..299 
    } catch (e: Exception) { false }

    suspend fun delete(token: String, id: Long): Boolean = try { 
        client.delete("${backendUrl()}/tasks/$id") { 
            auth(token) 
        }.status.value in 200..299 
    } catch (e: Exception) { false }

    private fun io.ktor.client.request.HttpRequestBuilder.auth(token: String) { header("Authorization", "Bearer $token") }
    private suspend fun io.ktor.client.statement.HttpResponse.bodyOrMessage(): String = try { body<MessageBody>().message } catch (e: Exception) { "Error $status" }
}

sealed class AuthResponse {
    data class Success(val auth: AuthBody) : AuthResponse()
    data class Error(val message: String) : AuthResponse()
}
