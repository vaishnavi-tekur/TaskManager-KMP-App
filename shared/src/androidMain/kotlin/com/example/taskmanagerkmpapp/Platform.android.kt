package com.example.taskmanagerkmpapp
import androidx.credentials.*
import com.google.android.libraries.identity.googleid.*
import android.content.Context
import kotlinx.coroutines.launch
import android.os.Build
import androidx.compose.runtime.Composable
// In Platform.android.kt

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled, onBack)
}
private var appContext: Context? = null
fun initializePlatform(context: Context) { appContext = context }

actual fun googleLogin(scope: kotlinx.coroutines.CoroutineScope, onResult: (String?) -> Unit) {
    val context = appContext ?: return onResult(null)
    val credentialManager = CredentialManager.create(context)

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setAutoSelectEnabled(true)
        .setServerClientId("458266882707-vo57l2c9v2pdd28f2av1rg4l44kpkksr.apps.googleusercontent.com")
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
