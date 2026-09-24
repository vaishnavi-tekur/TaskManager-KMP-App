package com.example.taskmanagerkmpapp

interface SessionStorage {
    fun save(user: String, name: String, email: String, token: String)
    fun saveTasks(json: String)
    fun read(key: String): String
    fun clear()
    fun getSavedEmails(): List<String>
    fun addSavedEmail(email: String)
    fun removeSavedEmail(email: String)
}

expect fun sessionStorage(): SessionStorage
