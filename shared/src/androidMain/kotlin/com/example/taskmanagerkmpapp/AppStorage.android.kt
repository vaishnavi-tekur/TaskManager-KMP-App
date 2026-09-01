package com.example.taskmanagerkmpapp

import android.content.Context
import androidx.core.content.edit

private var appContext: Context? = null

fun initAppContext(context: Context) {
    appContext = context.applicationContext
}

class AndroidAppStorage(private val context: Context) : AppStorage {
    private val prefs by lazy {
        context.getSharedPreferences("task_manager_prefs", Context.MODE_PRIVATE)
    }

    override fun saveString(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }

    override fun getString(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }
}

actual fun provideAppStorage(): AppStorage {
    val context = appContext ?: return InMemoryAppStorage
    return AndroidAppStorage(context)
}
