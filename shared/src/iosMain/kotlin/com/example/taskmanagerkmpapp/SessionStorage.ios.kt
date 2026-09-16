package com.example.taskmanagerkmpapp
import platform.Foundation.NSUserDefaults
private class IOSSessionStorage : SessionStorage {
    private val d = NSUserDefaults.standardUserDefaults
    override fun save(u: String, n: String, e: String, t: String) { d.setObject(u, "user"); d.setObject(n, "name"); d.setObject(e, "email"); d.setObject(t, "token") }
    override fun read(k: String): String = d.stringForKey(k) ?: ""
    override fun clear() = listOf("user", "name", "email", "token").forEach { d.removeObjectForKey(it) }
}
actual fun sessionStorage(): SessionStorage = IOSSessionStorage()
