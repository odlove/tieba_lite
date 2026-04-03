package app.tiebalite.core.data.auth.service

import app.tiebalite.core.data.auth.store.AuthStore
import app.tiebalite.core.data.auth.store.AuthStoreSnapshot
import app.tiebalite.core.model.auth.AuthAccount
import app.tiebalite.core.model.auth.AuthSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

interface AuthReader {
    val state: StateFlow<AuthReaderState>

    fun currentSession(): AuthSession?

    fun currentCookie(): String?
}

internal class DefaultAuthReader(
    private val authStore: AuthStore,
    scope: CoroutineScope,
) : AuthReader {
    override val state: StateFlow<AuthReaderState> =
        authStore.state
            .map { snapshot ->
                snapshot.toReaderState()
            }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = authStore.state.value.toReaderState(),
        )

    override fun currentSession(): AuthSession? =
        authStore.state.value.activeAccount?.session

    override fun currentCookie(): String? {
        val activeAccountId =
            authStore.state.value.activeAccountId
                ?: return null
        return authStore.cookieOf(activeAccountId)
    }
}

private fun AuthStoreSnapshot.toReaderState(): AuthReaderState =
    AuthReaderState(
        activeAccount = activeAccount,
        accounts = accounts,
        cookiesByAccountId = cookies,
        isLoaded = isLoaded,
    )

data class AuthReaderState(
    val activeAccount: AuthAccount?,
    val accounts: List<AuthAccount>,
    val cookiesByAccountId: Map<String, String>,
    val isLoaded: Boolean,
) {
    val isLoggedIn: Boolean
        get() = activeAccount?.session?.isValid == true
}
