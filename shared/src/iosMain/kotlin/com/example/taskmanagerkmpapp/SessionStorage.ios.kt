package com.example.taskmanagerkmpapp

import platform.Foundation.NSUserDefaults

private class IOSSessionStorage : SessionStorage {
    private val d = NSUserDefaults.standardUserDefaults
    override fun save(u: String, n: String, e: String, t: String) {
        d.setObject(u, "user"); d.setObject(n, "name"); d.setObject(e, "email"); d.setObject(t, "token")
        if (e.isNotBlank()) addSavedEmail(e)
    }
    override fun saveTasks(json: String) { d.setObject(json, "tasks_cache") }
    override fun read(k: String): String = d.stringForKey(k) ?: ""
    override fun clear() = listOf("user", "name", "token").forEach { d.removeObjectForKey(it) }

    override fun getSavedEmails(): List<String> {
        val raw = d.stringForKey("saved_emails") ?: ""
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
            d.setObject(current.joinToString(","), "saved_emails")
        }
    }

    override fun removeSavedEmail(email: String) {
        if (email.isBlank()) return
        val current = getSavedEmails().toMutableList()
        current.remove(email)
        d.setObject(current.joinToString(","), "saved_emails")
    }
}

actual fun sessionStorage(): SessionStorage = IOSSessionStorage()
