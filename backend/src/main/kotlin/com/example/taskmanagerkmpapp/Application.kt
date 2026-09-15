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
@Serializable data class User(val id: Long, val name: String, val username: String, val email: String)
@Serializable data class AuthResponse(val token: String, val user: User)
@Serializable data class MessageResponse(val message: String)

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        install(ContentNegotiation) { json() }
        val database = Database(); val sessions = mutableMapOf<String, Long>()
        routing {
            post("/register") { val r = call.receive<RegisterRequest>(); if (r.password.length < 6) return@post call.respond(HttpStatusCode.BadRequest, MessageResponse("Short pass")); val u = database.register(r) ?: return@post call.respond(HttpStatusCode.Conflict, MessageResponse("Exists")); call.respond(HttpStatusCode.Created, u) }
            post("/login") { val r = call.receive<LoginRequest>(); val u = database.authenticate(r.username, r.password) ?: return@post call.respond(HttpStatusCode.Unauthorized, MessageResponse("Invalid")); val t = UUID.randomUUID().toString(); sessions[t] = u.id; call.respond(AuthResponse(t, u)) }
        }
    }.start(wait = true)
}
