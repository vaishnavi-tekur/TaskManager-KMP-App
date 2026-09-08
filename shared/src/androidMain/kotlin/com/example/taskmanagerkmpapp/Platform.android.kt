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
}

actual fun googleLogin(scope: kotlinx.coroutines.CoroutineScope, onResult: (String?) -> Unit) {
    val context = appContext ?: return onResult(null)
    val credentialManager = CredentialManager.create(context)

    if (googleClientId.isEmpty()) {
        println("GOOGLE LOGIN ERROR: Client ID not initialized")
        return onResult(null)
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setAutoSelectEnabled(true)
        .setServerClientId(googleClientId)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val cred = result.credential
            if (cred is CustomCredential && cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(cred.data)
                onResult(googleIdTokenCredential.id)
            }
        } catch (e: Exception) {
            println("GOOGLE LOGIN ERROR: ${e.message}")
            e.printStackTrace()
            onResult(null)
        }
    }
}

private var appContext: Context? = null
fun initializePlatform(context: Context) {
    appContext = context
}
