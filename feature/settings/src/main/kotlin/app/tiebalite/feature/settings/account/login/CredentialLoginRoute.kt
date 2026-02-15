package app.tiebalite.feature.settings.account.login

import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import app.tiebalite.feature.settings.account.SettingsAccountViewModel
import app.tiebalite.feature.settings.account.SettingsAccountUiEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CredentialLoginRoute(
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    viewModel: SettingsAccountViewModel = viewModel(factory = SettingsAccountViewModel.Factory),
) {
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is SettingsAccountUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                SettingsAccountUiEvent.LoginSucceeded -> onBack()
            }
        }
    }
    CredentialLoginScreen(
        paddingValues = paddingValues,
        onBack = onBack,
        onLoginSuccess = { session ->
            viewModel.loginWithCredential(session)
        },
    )
}
