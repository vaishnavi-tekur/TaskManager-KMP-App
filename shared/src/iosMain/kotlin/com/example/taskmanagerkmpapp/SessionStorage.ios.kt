package com.example.taskmanagerkmpapp

import platform.Foundation.NSUserDefaults

private class IOSSessionStorage : SessionStorage {
    private val defaults = NSUserDefaults.standardUserDefaults
    override fun save(user: String, name: String, email: String, token: String) {
        defaults.setObject(user, "user"); defaults.setObject(name, "name")
        defaults.setObject(email, "email"); defaults.setObject(token, "token")
    }
    override fun read(key: String): String = defaults.stringForKey(key) ?: ""
    override fun clear() { listOf("user", "name", "email", "token").forEach { defaults.removeObjectForKey(it) } }
}

actual fun sessionStorage(): SessionStorage = IOSSessionStorage()
