package com.example.taskmanagerkmpapp

interface SessionStorage {
    fun save(user: String, name: String, email: String, token: String)
    fun read(key: String): String
    fun clear()
}

expect fun sessionStorage(): SessionStorage
