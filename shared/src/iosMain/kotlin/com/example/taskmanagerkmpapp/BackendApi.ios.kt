package com.example.taskmanagerkmpapp

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

actual fun createBackendClient(): HttpClient = HttpClient(Darwin) { install(ContentNegotiation) { json() } }
actual fun backendUrl(): String = "http://127.0.0.1:8080"
