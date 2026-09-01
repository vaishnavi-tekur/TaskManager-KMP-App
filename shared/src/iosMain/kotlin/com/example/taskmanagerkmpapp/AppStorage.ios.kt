package com.example.taskmanagerkmpapp

import platform.Foundation.NSUserDefaults

class IOSAppStorage : AppStorage {
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults

    override fun saveString(key: String, value: String) {
        defaults.setObject(value, key)
        defaults.synchronize()
    }

    override fun getString(key: String, defaultValue: String): String {
        return defaults.stringForKey(key) ?: defaultValue
    }
}

actual fun provideAppStorage(): AppStorage = IOSAppStorage()
