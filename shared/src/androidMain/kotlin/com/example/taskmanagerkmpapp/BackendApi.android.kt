package com.example.taskmanagerkmpapp

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual fun getBackendEngine(): HttpClientEngine = OkHttp.create()

private var internalBackendUrl: String = ""
fun initializeBackendUrl(url: String) {
    internalBackendUrl = url
}

actual fun backendUrl(): String = internalBackendUrl.ifEmpty { "http://10.126.56.69:8088" }
