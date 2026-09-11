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
@Serializable data class LoginRequest(val username: String, val password: String, val isGoogle: Boolean = false)
@Serializable data class ResetPasswordRequest(val email: String, val newPassword: String)
@Serializable data class User(val id: Long, val name: String, val username: String, val email: String, val isActive: Boolean = true)
@Serializable data class AuthResponse(val token: String, val user: User)
@Serializable data class MessageResponse(val message: String)
@Serializable data class TaskRequest(val title: String, val description: String, val priority: String = "Medium")
@Serializable data class Task(val id: Long, val title: String, val description: String, val priority: String, val completed: Boolean)

fun main() {
    println("STARTING SERVER ON 0.0.0.0:8088")
    // Port 8088 to avoid conflicts, host 0.0.0.0 to allow phone connections
    embeddedServer(Netty, port = 8088, host = "0.0.0.0") {
        install(ContentNegotiation) { json() }
        val database = Database(); val sessions = mutableMapOf<String, Long>()
        routing {
            post("/register") {
                val r = call.receive<RegisterRequest>()
                if (r.password.length < 6) return@post call.respond(HttpStatusCode.BadRequest, MessageResponse("Password is too short (minimum 6 characters)."))
                val u = database.register(r) ?: return@post call.respond(HttpStatusCode.Conflict, MessageResponse("Exists"))
                call.respond(HttpStatusCode.Created, u)
            }

            post("/login") {
                val r = call.receive<LoginRequest>()
                val usernameInput = r.username.trim()

                val u = if (r.isGoogle) {
                    val input = usernameInput.lowercase()
                    println("BACKEND: Google Login attempt for email: $input")
                    val existing = database.find(input)
                    if (existing != null) {
                        println("BACKEND: Existing user found: ${existing.username}")
                        existing
                    } else if (input.endsWith("@bhrish.com")) {
                        println("BACKEND: Creating/Getting user for domain @bhrish.com")
                        val created = database.getOrCreateByEmail(input, input.substringBefore("@"))
                        if (created == null) println("BACKEND ERROR: Failed to get or create user for $input")
                        created
                    } else {
                        println("BACKEND: Forbidden domain: $input")
                        return@post call.respond(HttpStatusCode.Forbidden, MessageResponse("User is not registered"))
                    }
                } else {
                    println("BACKEND: Manual Login attempt for username: $usernameInput")
                    // 1. Manually check if the user exists first
                    val existingUser = database.find(usernameInput)
                    if (existingUser == null) {
                        println("BACKEND: User not found: $usernameInput")
                        return@post call.respond(HttpStatusCode.NotFound, MessageResponse("User doesn't exist"))
                    }

                    // 2. If user exists, check the password
                    val auth = database.authenticate(usernameInput, r.password)
                    if (auth == null) {
                        println("BACKEND: Invalid password for user: $usernameInput")
                        return@post call.respond(HttpStatusCode.Unauthorized, MessageResponse("Invalid password. Please try again."))
                    }
                    auth
                }

                if (u == null) {
                    println("BACKEND ERROR: Login failed for $usernameInput (u is null)")
                    return@post call.respond(HttpStatusCode.Unauthorized, MessageResponse("Login failed"))
                }

                val t = UUID.randomUUID().toString()
                sessions[t] = u.id
                call.respond(AuthResponse(t, u))
            }

            post("/forgot-password") {
                val r = call.receive<ResetPasswordRequest>()

                // Add this validation check here
                if (r.newPassword.length < 6) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        MessageResponse("Password is too short (minimum 6 characters).")
                    )
                }

                if (!database.resetPassword(r.email, r.newPassword)) {
                    call.respond(HttpStatusCode.NotFound, MessageResponse("user email is not registered"))
                } else {
                    call.respond(MessageResponse("Updated"))
                }
            }

            get("/tasks") {
                val id = call.uid(sessions) ?: return@get call.respond(HttpStatusCode.Unauthorized)
                call.respond(database.tasks(id))
            }

            post("/tasks") {
                val id = call.uid(sessions) ?: return@post call.respond(HttpStatusCode.Unauthorized)
                call.respond(HttpStatusCode.Created, database.addTask(id, call.receive()) ?: MessageResponse("Error"))
            }

            put("/tasks/{id}") {
                val id = call.uid(sessions) ?: return@put call.respond(HttpStatusCode.Unauthorized)
                val tid = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
                val c = call.receive<Map<String, Boolean>>()["completed"] ?: false
                if (!database.updateTask(id, tid, c)) call.respond(HttpStatusCode.NotFound) else call.respond(MessageResponse("Updated"))
            }

            delete("/tasks/{id}") {
                val id = call.uid(sessions) ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                val tid = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
                if (!database.deleteTask(id, tid)) call.respond(HttpStatusCode.NotFound) else call.respond(MessageResponse("Deleted"))
            }

            delete("/delete-account") {
                val id = call.uid(sessions) ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                if (database.deleteUser(id)) {
                    sessions.entries.removeIf { it.value == id }
                    call.respond(MessageResponse("Account deleted"))
                } else call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }.start(wait = true)
}

private fun ApplicationCall.uid(s: Map<String, Long>) =
    request.headers["Authorization"]?.removePrefix("Bearer ")?.let(s::get)