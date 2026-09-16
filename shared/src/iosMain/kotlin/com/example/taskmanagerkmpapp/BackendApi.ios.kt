package com.example.taskmanagerkmpapp

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual fun getBackendEngine(): HttpClientEngine = Darwin.create()
actual fun backendUrl(): String = "http://localhost:8080"
