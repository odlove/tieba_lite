package app.tiebalite.feature.settings.account

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsAccountDetailRoute(
    paddingValues: PaddingValues,
    accountId: String,
    onBack: () -> Unit,
    viewModel: SettingsAccountViewModel = viewModel(factory = SettingsAccountViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsState()
    val account = state.accounts.firstOrNull { it.accountId == accountId }

    SettingsAccountDetailScreen(
        paddingValues = paddingValues,
        account = account,
        onSwitchAccount = { viewModel.switchAccount(accountId) },
        onBack = onBack,
    )
}
