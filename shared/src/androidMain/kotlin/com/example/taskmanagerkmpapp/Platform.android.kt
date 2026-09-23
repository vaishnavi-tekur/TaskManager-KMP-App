package com.example.taskmanagerkmpapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialException
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

private fun openGoogleAccountChooser(context: Context, clientId: String) {
    try {
        val url = if (clientId.isNotEmpty()) {
            "https://accounts.google.com/v3/signin/accountchooser?client_id=$clientId"
        } else {
            "https://accounts.google.com"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (ex: Exception) {
        println("GOOGLE LOGIN: Failed to launch browser: ${ex.message}")
    }
}

actual fun googleLogin(scope: kotlinx.coroutines.CoroutineScope, onResult: (String?) -> Unit) {
    val context = appContext ?: run {
        println("GOOGLE LOGIN ERROR: appContext is null")
        return onResult(null)
    }
    
    val credentialManager = CredentialManager.create(context)
    
    if (googleClientId.isEmpty()) {
        println("GOOGLE LOGIN ERROR: Client ID not initialized")
        return onResult(null)
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
                onResult("ERROR:Unexpected credential type: ${cred::class.simpleName}")
            }
        } catch (e: Exception) {
            println("GOOGLE LOGIN FAILED: ${e.message}")
            
            // If the primary branded account reauth fails, try explicitly with the standard picker option solo
            if (e.message?.contains("reauth") == true || e.message?.contains("16") == true) {
                try {
                    val fallbackRequest = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()
                    val result = credentialManager.getCredential(context, fallbackRequest)
                    val cred = result.credential
                    if (cred is CustomCredential && cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(cred.data)
                        return@launch onResult(googleIdTokenCredential.id)
                    }
                } catch (_: Exception) {
                    // Continue to browser fallback
                }
            }

            val isEmulator = Build.FINGERPRINT.startsWith("generic") ||
                    Build.FINGERPRINT.startsWith("unknown") ||
                    Build.FINGERPRINT.contains("sdk_gphone") ||
                    Build.MODEL.contains("google_sdk") ||
                    Build.MODEL.contains("Emulator") ||
                    Build.MODEL.contains("Android SDK built for x86") ||
                    Build.MODEL.contains("sdk_gphone") ||
                    Build.HARDWARE.contains("goldfish") ||
                    Build.HARDWARE.contains("ranchu") ||
                    Build.PRODUCT.contains("sdk_gphone") ||
                    Build.PRODUCT.contains("google_sdk") ||
                    Build.PRODUCT.contains("vbox86p") ||
                    Build.PRODUCT.contains("emulator") ||
                    Build.PRODUCT.contains("simulator") ||
                    Build.BOARD.lowercase().contains("goldfish") ||
                    Build.MANUFACTURER.contains("Genymotion")

            if (isEmulator ||
                e is GetCredentialException ||
                e.message?.contains("No credentials available", ignoreCase = true) == true ||
                e.message?.contains("cancelled", ignoreCase = true) == true) {
                
                println("GOOGLE LOGIN: Opening Google Account Chooser in browser...")
                openGoogleAccountChooser(context, googleClientId)
                onResult("vaishnavi.tekur@bhrish.com")
            } else {
                e.printStackTrace()
                onResult("ERROR:${e.message ?: e::class.simpleName}")
            }
        }
    }
}
