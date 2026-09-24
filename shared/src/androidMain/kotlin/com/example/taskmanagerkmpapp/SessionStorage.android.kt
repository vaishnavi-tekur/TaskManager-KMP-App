package com.example.taskmanagerkmpapp

import android.content.Context

private var appContext: Context? = null
fun initializeSessionStorage(c: Context) {
    appContext = c.applicationContext
}

private class AndroidSessionStorage(c: Context) : SessionStorage {
    private val p = c.getSharedPreferences("task_manager_session", Context.MODE_PRIVATE)
    
    override fun save(u: String, n: String, e: String, t: String) {
        p.edit()
            .putString("user", u)
            .putString("name", n)
            .putString("email", e)
            .putString("token", t)
            .apply()
        if (e.isNotBlank()) addSavedEmail(e)
    }

    override fun saveTasks(json: String) = p.edit()
        .putString("tasks_cache", json)
        .apply()

    override fun read(k: String): String = p.getString(k, "") ?: ""
    
    override fun clear() {
        p.edit()
            .remove("user")
            .remove("name")
            .remove("token")
            .apply()
    }

    override fun getSavedEmails(): List<String> {
        val raw = p.getString("saved_emails", "") ?: ""
        val list = if (raw.isBlank()) mutableListOf() else raw.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableList()
        if (!list.contains("vaishnavi.tekur@bhrish.com")) {
            list.add(0, "vaishnavi.tekur@bhrish.com")
        }
        return list.distinct()
    }

    override fun addSavedEmail(email: String) {
        if (email.isBlank()) return
        val current = getSavedEmails().toMutableList()
        if (!current.contains(email)) {
            current.add(0, email)
            p.edit().putString("saved_emails", current.joinToString(",")).apply()
        }
    }

    override fun removeSavedEmail(email: String) {
        if (email.isBlank()) return
        val current = getSavedEmails().toMutableList()
        current.remove(email)
        p.edit().putString("saved_emails", current.joinToString(",")).apply()
    }
}

actual fun sessionStorage(): SessionStorage = appContext?.let(::AndroidSessionStorage) ?: M

private object M : SessionStorage {
    private val v = mutableMapOf<String, String>()
    private val emails = mutableListOf("vaishnavi.tekur@bhrish.com")

    override fun save(u: String, n: String, e: String, t: String) {
        v["user"] = u; v["name"] = n; v["email"] = e; v["token"] = t
        if (e.isNotBlank()) addSavedEmail(e)
    }
    override fun saveTasks(json: String) {
        v["tasks_cache"] = json
    }
    override fun read(k: String): String = v[k] ?: ""
    override fun clear() {
        v.remove("user"); v.remove("name"); v.remove("token")
    }

    override fun getSavedEmails(): List<String> {
        if (!emails.contains("vaishnavi.tekur@bhrish.com")) {
            emails.add(0, "vaishnavi.tekur@bhrish.com")
        }
        return emails.distinct()
    }

    override fun addSavedEmail(email: String) {
        if (email.isNotBlank() && !emails.contains(email)) {
            emails.add(0, email)
        }
    }

    override fun removeSavedEmail(email: String) {
        emails.remove(email)
    }
}
