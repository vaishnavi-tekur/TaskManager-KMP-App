package com.example.taskmanagerkmpapp

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual fun getBackendEngine(): HttpClientEngine = Darwin.create()
actual fun backendUrl(): String = "http://127.0.0.1:8080"
