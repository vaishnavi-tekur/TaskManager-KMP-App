package com.example.taskmanagerkmpapp

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable data class RegisterRequest(val name: String, val username: String, val email: String, val password: String)
@Serializable data class LoginRequest(val username: String, val password: String, val isGoogle: Boolean = false)
@Serializable data class ResetPasswordRequest(val email: String, val newPassword: String)
@Serializable data class User(val id: Long, val name: String, val username: String, val email: String, val isActive: Boolean = true)
@Serializable data class AuthResponse(val token: String, val user: User)
@Serializable data class MessageResponse(val message: String)
@Serializable data class TaskRequest(val title: String, val description: String, val priority: String = "Medium", val completed: Boolean = false, val dueDate: Long? = null)
@Serializable data class TaskResponse(val id: Long, val title: String, val description: String, val priority: String, val completed: Boolean, val dueDate: Long? = null)

fun main() {
    System.setProperty("io.ktor.development", "true")
    System.setProperty("java.net.preferIPv4Stack", "true")
    
    println(">>> Starting Ktor Backend Server on http://0.0.0.0:8088 ...")
    
    try {
        embeddedServer(Netty, port = 8088, host = "0.0.0.0") {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            val db = Database()
            val s = mutableMapOf<String, Long>()
            
            routing {
                post("/register") { 
                    val r = call.receive<RegisterRequest>()
                    if (r.password.length < 6) return@post call.respond(HttpStatusCode.BadRequest, MessageResponse("Short pass"))
                    val u = db.register(r) ?: return@post call.respond(HttpStatusCode.Conflict, MessageResponse("Exists"))
                    call.respond(HttpStatusCode.Created, u) 
                }
                
                post("/login") { 
                    val r = call.receive<LoginRequest>()
                    val u = if (r.isGoogle) {
                        val email = r.username.lowercase().trim()
                        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
                        if (!emailRegex.matches(email)) {
                            return@post call.respond(
                                HttpStatusCode.BadRequest,
                                MessageResponse("Please enter a valid email address.")
                            )
                        }
                        
                        val isBhrishDomain = email.endsWith("@bhrish.com") || email.endsWith("@bhrish")
                        
                        if (isBhrishDomain) {
                            // @bhrish.com domain -> Automatic SSO login & database registration
                            db.getOrCreateByEmail(email, email.substringBefore("@"))
                        } else {
                            // Non-@bhrish.com domain -> Check if user exists in database
                            val existingUser = db.findByEmail(email) ?: db.find(email)
                            if (existingUser == null) {
                                return@post call.respond(
                                    HttpStatusCode.Unauthorized, 
                                    MessageResponse("User is not registered. Please register first.")
                                )
                            }
                            
                            // If auto-login request (no password yet), require password challenge
                            if (r.password == "google_auto_login") {
                                return@post call.respond(
                                    HttpStatusCode.Forbidden,
                                    MessageResponse("REQUIRE_PASSWORD:${existingUser.name}:$email")
                                )
                            }
                            
                            // Verify entered password for non-@bhrish account
                            val authenticatedUser = db.authenticate(email, r.password)
                            if (authenticatedUser == null) {
                                return@post call.respond(
                                    HttpStatusCode.Unauthorized,
                                    MessageResponse("Invalid password.")
                                )
                            }
                            authenticatedUser
                        }
                    } else {
                        db.authenticate(r.username, r.password)
                    }
                    
                    if (u == null) return@post call.respond(HttpStatusCode.Unauthorized, MessageResponse("Invalid credentials."))
                    
                    val t = UUID.randomUUID().toString()
                    s[t] = u.id
                    call.respond(AuthResponse(t, u)) 
                }
                
                post("/forgot-password") { 
                    val r = call.receive<ResetPasswordRequest>()
                    if (!db.resetPassword(r.email, r.newPassword)) call.respond(HttpStatusCode.NotFound, MessageResponse("No account")) 
                    else call.respond(MessageResponse("Updated")) 
                }
                
                delete("/delete-account") { 
                    val id = call.uid(s) ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                    if (db.deleteUser(id)) { 
                        s.entries.removeIf { it.value == id }
                        call.respond(MessageResponse("Account deleted")) 
                    } else call.respond(HttpStatusCode.InternalServerError) 
                }
                
                route("/tasks") {
                    get { 
                        val id = call.uid(s) ?: return@get call.respond(HttpStatusCode.Unauthorized)
                        call.respond(db.getTasks(id))
                    }
                    post { 
                        val u = call.uid(s) ?: return@post call.respond(HttpStatusCode.Unauthorized)
                        val r = call.receive<TaskRequest>()
                        if (db.addTask(u, r.title, r.description, r.priority, r.dueDate)) call.respond(HttpStatusCode.Created) 
                        else call.respond(HttpStatusCode.InternalServerError) 
                    }
                    put("/{id}") { 
                        val u = call.uid(s) ?: return@put call.respond(HttpStatusCode.Unauthorized)
                        val tid = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
                        val r = call.receive<TaskRequest>()
                        if (db.updateTask(tid, u, r.title, r.description, r.priority, r.completed, r.dueDate)) call.respond(HttpStatusCode.OK) 
                        else call.respond(HttpStatusCode.NotFound) 
                    }
                    delete("/{id}") { 
                        val u = call.uid(s) ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                        val tid = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
                        if (db.deleteTask(tid, u)) call.respond(HttpStatusCode.OK) 
                        else call.respond(HttpStatusCode.NotFound) 
                    }
                }
            }
        }.start(wait = true)
    } catch (e: Exception) {
        println(">>> SERVER STARTUP ERROR: ${e.message}")
        e.printStackTrace()
    }
}

private fun ApplicationCall.uid(s: Map<String, Long>) = request.headers["Authorization"]?.removePrefix("Bearer ")?.let(s::get)
