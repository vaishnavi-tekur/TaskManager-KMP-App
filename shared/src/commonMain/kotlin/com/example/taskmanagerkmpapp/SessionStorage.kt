package com.example.taskmanagerkmpapp

interface SessionStorage {
    fun save(user: String, name: String, email: String, token: String)
    fun read(key: String): String
    fun clear()
}

// Simple memory-based storage for the foundation PR
internal object MemoryStorage : SessionStorage {
    private val values = mutableMapOf<String, String>()
    override fun save(user: String, name: String, email: String, token: String) {
        values["user"] = user; values["name"] = name; values["email"] = email; values["token"] = token
    }
    override fun read(key: String): String = values[key] ?: ""
    override fun clear() = values.clear()
}

fun sessionStorage(): SessionStorage = MemoryStorage
