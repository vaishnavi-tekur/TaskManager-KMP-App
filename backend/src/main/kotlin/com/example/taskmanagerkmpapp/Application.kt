package com.example.taskmanagerkmpapp

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable data class RegisterRequest(val name: String, val username: String, val email: String, val password: String)
@Serializable data class LoginRequest(val username: String, val password: String)
@Serializable data class ResetPasswordRequest(val email: String, val newPassword: String)
@Serializable data class User(val id: Long, val name: String, val username: String, val email: String)
@Serializable data class AuthResponse(val token: String, val user: User)
@Serializable data class MessageResponse(val message: String)
@Serializable data class TaskRequest(val title: String, val description: String, val priority: String = "Medium")
@Serializable data class Task(val id: Long, val title: String, val description: String, val priority: String, val completed: Boolean)

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) { json() }
    val database = Database()
    val sessions = mutableMapOf<String, Long>()
    routing {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            if (request.password.length < 6) return@post call.respond(HttpStatusCode.BadRequest, MessageResponse("Password must be at least 6 characters"))
            val user = database.register(request) ?: return@post call.respond(HttpStatusCode.Conflict, MessageResponse("Username or email already exists"))
            call.respond(HttpStatusCode.Created, user)
        }
        post("/login") {
            val request = call.receive<LoginRequest>()
            val user = database.authenticate(request.username, request.password) ?: return@post call.respond(HttpStatusCode.Unauthorized, MessageResponse("Invalid username or password"))
            val token = UUID.randomUUID().toString()
            sessions[token] = user.id
            call.respond(AuthResponse(token, user))
        }
        post("/forgot-password") {
            val request = call.receive<ResetPasswordRequest>()
            val updated = database.resetPassword(request.email, request.newPassword)
            if (!updated) call.respond(HttpStatusCode.NotFound, MessageResponse("No account found for this email"))
            else call.respond(MessageResponse("Password updated"))
        }
        get("/tasks") {
            val userId = call.userId(sessions) ?: return@get call.respond(HttpStatusCode.Unauthorized)
            call.respond(database.tasks(userId))
        }
        post("/tasks") {
            val userId = call.userId(sessions) ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val task = database.addTask(userId, call.receive())
            call.respond(HttpStatusCode.Created, task ?: MessageResponse("Unable to create task"))
        }
        put("/tasks/{id}") {
            val userId = call.userId(sessions) ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
            val completed = call.receive<Map<String, Boolean>>() ["completed"] ?: false
            if (!database.updateTask(userId, id, completed)) call.respond(HttpStatusCode.NotFound)
            else call.respond(MessageResponse("Task updated"))
        }
        delete("/tasks/{id}") {
            val userId = call.userId(sessions) ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (!database.deleteTask(userId, id)) call.respond(HttpStatusCode.NotFound)
            else call.respond(MessageResponse("Task deleted"))
        }
    }
}

private fun ApplicationCall.userId(sessions: Map<String, Long>) = request.headers["Authorization"]?.removePrefix("Bearer ")?.let(sessions::get)
