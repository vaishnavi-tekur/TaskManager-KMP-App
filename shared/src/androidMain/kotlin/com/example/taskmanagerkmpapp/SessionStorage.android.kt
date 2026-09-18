package com.example.taskmanagerkmpapp

import android.content.Context

private var appContext: Context? = null
fun initializeSessionStorage(c: Context) {
    appContext = c.applicationContext
}

private class AndroidSessionStorage(c: Context) : SessionStorage {
    private val p = c.getSharedPreferences("task_manager_session", Context.MODE_PRIVATE)
    
    override fun save(u: String, n: String, e: String, t: String) = p.edit()
        .putString("user", u)
        .putString("name", n)
        .putString("email", e)
        .putString("token", t)
        .apply()

    override fun saveTasks(json: String) = p.edit()
        .putString("tasks_cache", json)
        .apply()

    override fun read(k: String): String = p.getString(k, "") ?: ""
    
    override fun clear() = p.edit().clear().apply()
}

actual fun sessionStorage(): SessionStorage = appContext?.let(::AndroidSessionStorage) ?: M

private object M : SessionStorage {
    private val v = mutableMapOf<String, String>()
    override fun save(u: String, n: String, e: String, t: String) {
        v["user"] = u; v["name"] = n; v["email"] = e; v["token"] = t
    }
    override fun saveTasks(json: String) {
        v["tasks_cache"] = json
    }
    override fun read(k: String): String = v[k] ?: ""
    override fun clear() = v.clear()
}
