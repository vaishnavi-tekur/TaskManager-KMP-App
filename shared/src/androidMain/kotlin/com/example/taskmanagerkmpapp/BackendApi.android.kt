package com.example.taskmanagerkmpapp

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual fun getBackendEngine(): HttpClientEngine = OkHttp.create()
actual fun backendUrl(): String = "http://10.0.2.2:8080"
