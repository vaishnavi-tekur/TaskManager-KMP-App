package com.example.taskmanagerkmpapp

import androidx.compose.runtime.Composable
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()
actual fun googleLogin(scope: kotlinx.coroutines.CoroutineScope, onResult: (String?) -> Unit) {
    // Placeholder for iOS - returns null for now
    onResult(null)
}

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op for iOS as it doesn't have a hardware back button
}
