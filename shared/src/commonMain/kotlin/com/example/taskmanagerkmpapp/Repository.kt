package com.example.taskmanagerkmpapp

internal data class User(val name:String, val username:String, val email:String)

// Mock repository for the foundation PR to demonstrate UI flow
internal object Repo {
    fun login(u:String, p:String): User? {
        if (u == "admin" && p == "password") return User("Jane Doe", "admin", "jane@example.com")
        return null
    }
    fun register(n:String, u:String, e:String, p:String) = User(n, u, e)
}
