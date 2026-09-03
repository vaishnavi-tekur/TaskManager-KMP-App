package com.example.taskmanagerkmpapp

import android.content.Context

private var appContext: Context? = null

fun initializeSessionStorage(context: Context) {
    appContext = context.applicationContext
}

private class AndroidSessionStorage(context: Context) : SessionStorage {
    private val preferences = context.getSharedPreferences("task_manager_session", Context.MODE_PRIVATE)
    override fun save(user: String, name: String, email: String, token: String) {
        preferences.edit().putString("user", user).putString("name", name).putString("email", email).putString("token", token).apply()
    }
    override fun read(key: String): String = preferences.getString(key, "") ?: ""
    override fun clear() = preferences.edit().clear().apply()
}

actual fun sessionStorage(): SessionStorage = appContext?.let(::AndroidSessionStorage) ?: MemorySessionStorage

private object MemorySessionStorage : SessionStorage {
    private val values = mutableMapOf<String, String>()
    override fun save(user: String, name: String, email: String, token: String) {
        values["user"] = user; values["name"] = name; values["email"] = email; values["token"] = token
    }
    override fun read(key: String): String = values[key] ?: ""
    override fun clear() = values.clear()
}
