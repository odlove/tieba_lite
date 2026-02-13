package app.tiebalite.auth

import app.tiebalite.core.data.auth.AuthStore
import app.tiebalite.core.model.auth.AuthAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AuthState(
    authStore: AuthStore,
    scope: CoroutineScope,
) {
    val state: StateFlow<AuthUiState> =
        combine(
            authStore.accounts,
            authStore.activeAccountId,
        ) { accounts, activeAccountId ->
            val activeAccount =
                accounts.firstOrNull { account ->
                    account.accountId == activeAccountId
                }
            AuthUiState(
                activeAccount = activeAccount,
                accounts = accounts,
            )
        }.stateIn(
            scope,
            SharingStarted.Eagerly,
            AuthUiState(
                activeAccount = authStore.currentActiveAccount(),
                accounts = authStore.accounts.value,
            ),
        )
}

data class AuthUiState(
    val activeAccount: AuthAccount?,
    val accounts: List<AuthAccount>,
) {
    val isLoggedIn: Boolean
        get() = activeAccount?.session?.isValid == true
}
