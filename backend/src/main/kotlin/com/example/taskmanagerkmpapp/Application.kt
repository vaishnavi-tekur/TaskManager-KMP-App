package com.example.taskmanagerkmpapp
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
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
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        install(ContentNegotiation) { json() }
        val database = Database(); val sessions = mutableMapOf<String, Long>()
        routing {
            post("/register") { val r = call.receive<RegisterRequest>(); if (r.password.length < 6) return@post call.respond(HttpStatusCode.BadRequest, MessageResponse("Short pass")); val u = database.register(r) ?: return@post call.respond(HttpStatusCode.Conflict, MessageResponse("Exists")); call.respond(HttpStatusCode.Created, u) }
            post("/login") { val r = call.receive<LoginRequest>(); val u = database.authenticate(r.username, r.password) ?: return@post call.respond(HttpStatusCode.Unauthorized, MessageResponse("Invalid")); val t = UUID.randomUUID().toString(); sessions[t] = u.id; call.respond(AuthResponse(t, u)) }
            post("/forgot-password") { val r = call.receive<ResetPasswordRequest>(); if (!database.resetPassword(r.email, r.newPassword)) call.respond(HttpStatusCode.NotFound, MessageResponse("No account")) else call.respond(MessageResponse("Updated")) }
            get("/tasks") { val id = call.uid(sessions) ?: return@get call.respond(HttpStatusCode.Unauthorized); call.respond(database.tasks(id)) }
            post("/tasks") { val id = call.uid(sessions) ?: return@post call.respond(HttpStatusCode.Unauthorized); call.respond(HttpStatusCode.Created, database.addTask(id, call.receive()) ?: MessageResponse("Error")) }
            put("/tasks/{id}") { val id = call.uid(sessions) ?: return@put call.respond(HttpStatusCode.Unauthorized); val tid = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest); val c = call.receive<Map<String, Boolean>>()["completed"] ?: false; if (!database.updateTask(id, tid, c)) call.respond(HttpStatusCode.NotFound) else call.respond(MessageResponse("Updated")) }
            delete("/tasks/{id}") { val id = call.uid(sessions) ?: return@delete call.respond(HttpStatusCode.Unauthorized); val tid = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest); if (!database.deleteTask(id, tid)) call.respond(HttpStatusCode.NotFound) else call.respond(MessageResponse("Deleted")) }
        }
    }.start(wait = true)
}
private fun ApplicationCall.uid(s: Map<String, Long>) = request.headers["Authorization"]?.removePrefix("Bearer ")?.let(s::get)
