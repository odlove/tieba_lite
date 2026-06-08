package app.tiebalite.feature.myforums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.tiebalite.core.data.auth.di.AuthGraphProvider
import app.tiebalite.core.data.auth.service.AuthReader
import app.tiebalite.core.data.myforums.repository.MyForumsRepository
import app.tiebalite.core.data.myforums.repository.MyForumsRepositoryFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MyForumsViewModel(
    private val repository: MyForumsRepository,
    private val authReader: AuthReader,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyForumsUiState())
    val uiState: StateFlow<MyForumsUiState> = _uiState.asStateFlow()
    private val _uiEvents = MutableSharedFlow<MyForumsUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<MyForumsUiEvent> = _uiEvents.asSharedFlow()

    private var requestJob: Job? = null

    init {
        viewModelScope.launch {
            authReader.state
                .map { state ->
                    state.activeAccount?.let { account ->
                        Triple(
                            account.accountId,
                            account.session.bduss,
                            account.session.stoken,
                        )
                    }
                }.distinctUntilChanged()
                .collect { activeAccount ->
                    if (activeAccount == null) {
                        requestJob?.cancel()
                        _uiState.value =
                            MyForumsUiState(
                                isLoggedIn = false,
                                isLoading = false,
                            )
                    } else {
                        loadMyForums(showRefreshWhenContent = false)
                    }
                }
        }
    }

    fun refresh() {
        loadMyForums(showRefreshWhenContent = true)
    }

    private fun loadMyForums(showRefreshWhenContent: Boolean) {
        if (!authReader.state.value.isLoggedIn) {
            requestJob?.cancel()
            _uiState.value =
                MyForumsUiState(
                    isLoggedIn = false,
                    isLoading = false,
                    isRefreshing = false,
                )
            return
        }

        requestJob?.cancel()
        _uiState.update { current ->
            val showRefreshing = showRefreshWhenContent && current.items.isNotEmpty()
            current.copy(
                isLoggedIn = true,
                isLoading = !showRefreshing,
                isRefreshing = showRefreshing,
                items = if (showRefreshWhenContent) current.items else emptyList(),
                errorMessage = null,
            )
        }

        requestJob =
            viewModelScope.launch {
                repository.loadMyForums().fold(
                    onSuccess = { items ->
                        _uiState.value =
                            MyForumsUiState(
                                isLoggedIn = true,
                                isLoading = false,
                                isRefreshing = false,
                                items = items,
                            )
                        if (showRefreshWhenContent) {
                            _uiEvents.tryEmit(MyForumsUiEvent.ShowToast(REFRESH_SUCCESS_MESSAGE))
                        }
                    },
                    onFailure = {
                        val hasItems = _uiState.value.items.isNotEmpty()
                        _uiState.update { current ->
                            current.copy(
                                isLoggedIn = true,
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = if (hasItems) null else NETWORK_ERROR_MESSAGE,
                            )
                        }
                    },
                )
            }
    }

    companion object {
        private const val NETWORK_ERROR_MESSAGE = "加载失败，请重试"
        private const val REFRESH_SUCCESS_MESSAGE = "刷新成功"

        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = checkNotNull(this[APPLICATION_KEY])
                    val authGraph =
                        (application as? AuthGraphProvider)?.authGraph
                            ?: error("Application must implement AuthGraphProvider")
                    val authReader = authGraph.authReader
                    MyForumsViewModel(
                        repository =
                            MyForumsRepositoryFactory.create(
                                sessionProvider = { authReader.currentSession() },
                            ),
                        authReader = authReader,
                    )
                }
            }
    }
}
