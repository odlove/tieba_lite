package app.tiebalite.feature.settings.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.tiebalite.core.data.auth.di.AuthGraphProvider
import app.tiebalite.core.data.auth.service.AuthReader
import app.tiebalite.core.data.auth.service.AuthReaderState
import app.tiebalite.core.data.auth.service.AuthService
import app.tiebalite.core.model.auth.AuthSession
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsAccountViewModel(
    private val authReader: AuthReader,
    private val authService: AuthService,
) : ViewModel() {
    private val mutableUiEvents = MutableSharedFlow<SettingsAccountUiEvent>(extraBufferCapacity = 2)
    val uiEvents: SharedFlow<SettingsAccountUiEvent> = mutableUiEvents.asSharedFlow()

    val uiState: StateFlow<SettingsAccountUiState> =
        authReader.state
            .map { state ->
                state.toUiState()
            }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = authReader.state.value.toUiState(),
        )

    fun switchAccount(accountId: String) {
        viewModelScope.launch {
            val switched = authService.switchAccount(accountId).isSuccess
            if (!switched) {
                emitToast(OPERATION_FAILED_MESSAGE)
            }
        }
    }

    fun removeAccount(accountId: String) {
        viewModelScope.launch {
            val removed = authService.removeAccount(accountId).isSuccess
            if (!removed) {
                emitToast(OPERATION_FAILED_MESSAGE)
            }
        }
    }

    fun logoutActive() {
        viewModelScope.launch {
            val loggedOut = authService.logoutActive().isSuccess
            if (!loggedOut) {
                emitToast(OPERATION_FAILED_MESSAGE)
            }
        }
    }

    fun loginWithWeb(
        session: AuthSession,
        rawCookie: String,
    ) {
        viewModelScope.launch {
            authService.loginWithWeb(session, rawCookie).onSuccess {
                emitToast(LOGIN_SUCCESS_MESSAGE)
                mutableUiEvents.tryEmit(SettingsAccountUiEvent.LoginSucceeded)
            }.onFailure {
                emitToast(NETWORK_ERROR_MESSAGE)
            }
        }
    }

    fun loginWithCredential(session: AuthSession) {
        viewModelScope.launch {
            authService.loginWithCredential(session).onSuccess {
                emitToast(LOGIN_SUCCESS_MESSAGE)
                mutableUiEvents.tryEmit(SettingsAccountUiEvent.LoginSucceeded)
            }.onFailure {
                emitToast(NETWORK_ERROR_MESSAGE)
            }
        }
    }

    private fun emitToast(message: String) {
        mutableUiEvents.tryEmit(SettingsAccountUiEvent.ShowToast(message = message))
    }

    companion object {
        private const val NETWORK_ERROR_MESSAGE = "网络错误"
        private const val OPERATION_FAILED_MESSAGE = "操作失败"
        private const val LOGIN_SUCCESS_MESSAGE = "登录成功"

        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = checkNotNull(this[APPLICATION_KEY])
                    val authGraph =
                        (application as? AuthGraphProvider)?.authGraph
                            ?: error("Application must implement AuthGraphProvider")
                    SettingsAccountViewModel(
                        authReader = authGraph.authReader,
                        authService = authGraph.authService,
                    )
                }
            }
    }
}

data class SettingsAccountUiState(
    val isLoggedIn: Boolean,
    val accounts: List<SettingsAccountItem>,
)

sealed interface SettingsAccountUiEvent {
    data class ShowToast(
        val message: String,
    ) : SettingsAccountUiEvent

    data object LoginSucceeded : SettingsAccountUiEvent
}

private fun AuthReaderState.toUiState(): SettingsAccountUiState {
    val activeAccountId = activeAccount?.accountId
    val items =
        accounts.map { account ->
            SettingsAccountItem(
                accountId = account.accountId,
                title =
                    account.profile?.displayName
                        ?.takeIf { it.isNotBlank() }
                        ?: "账号 ${account.accountId.take(6)}",
                subtitle = "BDUSS: ${account.session.bduss.take(8)}...",
                isActive = account.accountId == activeAccountId,
                isSessionValid = account.session.isValid,
                bduss = account.session.bduss,
                stoken = account.session.stoken,
                tbs = account.session.tbs,
                rawCookie = cookiesByAccountId[account.accountId],
                userId = account.profile?.userId,
                userName = account.profile?.userName,
                displayName = account.profile?.displayName,
                avatarUrl = account.profile?.avatarUrl,
                updatedAtMillis = account.updatedAtMillis,
            )
        }
    return SettingsAccountUiState(
        isLoggedIn = activeAccountId != null,
        accounts = items,
    )
}
