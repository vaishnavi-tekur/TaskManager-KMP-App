package com.example.taskmanagerkmpapp
import android.content.Context; import android.os.Build; import androidx.compose.runtime.Composable; import androidx.credentials.*; import com.google.android.libraries.identity.googleid.*; import kotlinx.coroutines.launch
class AndroidPlatform : Platform { override val name: String = "Android ${Build.VERSION.SDK_INT}" }
actual fun getPlatform(): Platform = AndroidPlatform()
@Composable actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) { androidx.activity.compose.BackHandler(enabled, onBack) }
private var appContext: Context? = null; private var googleClientId: String = ""
fun initializePlatform(context: Context) { appContext = context }
fun initializeGoogleLogin(clientId: String) { googleClientId = clientId }
actual fun googleLogin(scope: kotlinx.coroutines.CoroutineScope, onResult: (String?) -> Unit) {
    val context = appContext ?: return onResult(null)
    val credentialManager = CredentialManager.create(context)
    if (googleClientId.isEmpty()) return onResult(null)
    val googleIdOption = GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(false).setAutoSelectEnabled(false).setServerClientId(googleClientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val cred = result.credential
            if (cred is CustomCredential && cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                onResult(GoogleIdTokenCredential.createFrom(cred.data).id)
            } else onResult(null)
        } catch (e: Exception) { e.printStackTrace(); onResult(null) }
    }
}
