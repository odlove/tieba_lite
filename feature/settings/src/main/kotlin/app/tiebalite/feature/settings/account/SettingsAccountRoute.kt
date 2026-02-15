package app.tiebalite.feature.settings.account

import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsAccountRoute(
    paddingValues: PaddingValues,
    onOpenWebLogin: () -> Unit,
    onOpenCredentialLogin: () -> Unit,
    onOpenAccountDetail: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: SettingsAccountViewModel = viewModel(factory = SettingsAccountViewModel.Factory),
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is SettingsAccountUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                SettingsAccountUiEvent.LoginSucceeded -> Unit
            }
        }
    }
    SettingsAccountScreen(
        paddingValues = paddingValues,
        isLoggedIn = state.isLoggedIn,
        accounts = state.accounts,
        onOpenWebLogin = onOpenWebLogin,
        onOpenCredentialLogin = onOpenCredentialLogin,
        onOpenAccountDetail = onOpenAccountDetail,
        onRemoveAccount = viewModel::removeAccount,
        onLogoutActive = viewModel::logoutActive,
        onBack = onBack,
    )
}
