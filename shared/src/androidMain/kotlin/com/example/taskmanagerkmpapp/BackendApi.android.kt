package com.example.taskmanagerkmpapp

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual fun getBackendEngine(): HttpClientEngine = OkHttp.create()

private var internalBackendUrl: String = ""
fun initializeBackendUrl(url: String) {
    internalBackendUrl = url.trim().removeSuffix("/")
    println("BACKEND URL INITIALIZED: $internalBackendUrl")
}

actual fun backendUrl(): String {
    // If we are on an emulator, 10.0.2.2 is usually the way to go.
    // If the provided internalBackendUrl is set via initializeBackendUrl, we use it.
    val url = internalBackendUrl.ifEmpty { "http://10.0.2.2:8088" }
    println("USING BACKEND URL: $url")
    return url
}
