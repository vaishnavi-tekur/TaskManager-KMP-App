package com.example.taskmanagerkmpapp

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.credentials.*
import com.google.android.libraries.identity.googleid.*
import kotlinx.coroutines.launch

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled, onBack)
}

private var appContext: Context? = null
private var googleClientId: String = ""

fun initializePlatform(context: Context) {
    appContext = context
}

fun initializeGoogleLogin(clientId: String) {
    googleClientId = clientId
}

actual fun googleLogin(scope: kotlinx.coroutines.CoroutineScope, onResult: (String?) -> Unit) {
    val context = appContext ?: run {
        println("GOOGLE LOGIN ERROR: appContext is null")
        return onResult("SHOW_PICKER")
    }
    
    val credentialManager = CredentialManager.create(context)
    
    if (googleClientId.isEmpty()) {
        println("GOOGLE LOGIN ERROR: Client ID not initialized")
        return onResult("SHOW_PICKER")
    }

    val signInOption = GetSignInWithGoogleOption.Builder(googleClientId)
        .build()

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setAutoSelectEnabled(false)
        .setServerClientId(googleClientId)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .addCredentialOption(googleIdOption)
        .build()

    scope.launch {
        try {
            println("GOOGLE LOGIN: Starting request...")
            val result = credentialManager.getCredential(context, request)
            val cred = result.credential
            
            if (cred is CustomCredential && cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(cred.data)
                onResult(googleIdTokenCredential.id)
            } else {
                onResult("SHOW_PICKER")
            }
        } catch (e: Exception) {
            println("GOOGLE LOGIN FAILED: ${e.message}")
            onResult("SHOW_PICKER")
        }
    }
}
