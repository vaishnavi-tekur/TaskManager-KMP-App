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
        initializeGoogleLogin("458266882707-vo57l2c9v2pdd28f2av1rg4l44kpkksr.apps.googleusercontent.com") // In a real app, this would come from BuildConfig

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