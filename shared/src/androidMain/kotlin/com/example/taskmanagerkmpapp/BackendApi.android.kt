package com.example.taskmanagerkmpapp

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

actual fun createBackendClient(): HttpClient = HttpClient(OkHttp) { install(ContentNegotiation) { json() } }
actual fun backendUrl(): String = "http://10.0.2.2:8080"
