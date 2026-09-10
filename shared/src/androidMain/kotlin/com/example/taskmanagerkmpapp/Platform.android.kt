package com.example.taskmanagerkmpapp

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.credentials.*
import com.google.android.libraries.identity.googleid.*
import android.content.Context
import kotlinx.coroutines.launch

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled, onBack)
}

private var googleClientId: String = ""
fun initializeGoogleLogin(clientId: String) {
    googleClientId = clientId
    println("GOOGLE LOGIN: Initialized with ID: $googleClientId")
}

actual fun googleLogin(scope: kotlinx.coroutines.CoroutineScope, onResult: (String?) -> Unit) {
    val context = appContext ?: run {
        println("GOOGLE LOGIN ERROR: appContext is null. Ensure initializePlatform(this) is called in MainActivity.")
        return onResult(null)
    }
    val credentialManager = CredentialManager.create(context)

    if (googleClientId.isEmpty()) {
        println("GOOGLE LOGIN ERROR: Client ID not initialized. Check your .env and MainActivity.")
        return onResult(null)
    }

    println("GOOGLE LOGIN: Starting request with Client ID: $googleClientId")
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setAutoSelectEnabled(false) // Disable auto-select to force the account picker for debugging
        .setServerClientId(googleClientId)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    scope.launch {
        try {
            println("GOOGLE LOGIN: Awaiting credential selection UI...")
            val result = credentialManager.getCredential(context, request)
            val cred = result.credential
            println("GOOGLE LOGIN: Credential received. Type: ${cred.type}")
            
            if (cred is CustomCredential && cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(cred.data)
                val email = googleIdTokenCredential.id
                println("GOOGLE LOGIN SUCCESS: User selected email: $email")
                onResult(email)
            } else {
                println("GOOGLE LOGIN FAILED: Unexpected credential type: ${cred.type}")
                onResult(null)
            }
        } catch (e: Exception) {
            val errorMsg = when (e) {
                is androidx.credentials.exceptions.GetCredentialException -> "Credential Manager Error: ${e.type} - ${e.message}"
                else -> "Google Login Error: ${e.message}"
            }
            println("GOOGLE LOGIN FAILED: $errorMsg")
            e.printStackTrace()
            onResult(null)
        }
    }
}

private var appContext: Context? = null
fun initializePlatform(context: Context) {
    appContext = context
}
