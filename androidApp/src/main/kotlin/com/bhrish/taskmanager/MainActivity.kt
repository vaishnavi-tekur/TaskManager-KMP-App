package com.bhrish.taskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.taskmanagerkmpapp.App
import com.example.taskmanagerkmpapp.initializeGoogleLogin
import com.example.taskmanagerkmpapp.initializePlatform
import com.example.taskmanagerkmpapp.initializeSessionStorage
import com.example.taskmanagerkmpapp.initializeBackendUrl

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        initializeSessionStorage(this)
        initializePlatform(this)

        // Ensure these use the correct BuildConfig
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
