package com.example.taskmanagerkmpapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        initializeSessionStorage(this)
        initializePlatform(this)

<<<<<<< HEAD
        // Ensure these use the correct BuildConfig
=======
        // Use the values from .env (via BuildConfig)
>>>>>>> task/kmp-task-scheduling
        initializeGoogleLogin(BuildConfig.GOOGLE_CLIENT_ID)
        initializeBackendUrl(BuildConfig.BACKEND_URL)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
