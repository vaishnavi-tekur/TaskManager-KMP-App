package com.example.taskmanagerkmpapp

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual fun getBackendEngine(): HttpClientEngine = OkHttp.create()
actual fun backendUrl(): String = "https://taskmanager-kmp-app.onrender.com"
