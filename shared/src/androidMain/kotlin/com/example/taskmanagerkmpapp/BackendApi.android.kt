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
    val url = internalBackendUrl.ifEmpty { "http://192.168.1.34:8088" }
    println("USING BACKEND URL: $url")
    return url
}
