package com.example.taskmanagerkmpapp

import android.os.Build
import androidx.compose.runtime.Composable

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled, onBack)
}

actual fun googleLogin(scope: kotlinx.coroutines.CoroutineScope, onResult: (String?) -> Unit) {
    // This is a mock implementation for Android. 
    // In a real app, you would use Credential Manager here.
    // For now, it returns null or a test email based on your previous requirements.
    onResult("test@bhrish.com") 
}
