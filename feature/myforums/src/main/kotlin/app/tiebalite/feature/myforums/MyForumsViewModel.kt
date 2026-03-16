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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
                        refresh()
                    }
                }
        }
    }

    fun refresh() {
        if (!authReader.state.value.isLoggedIn) {
            requestJob?.cancel()
            _uiState.value =
                MyForumsUiState(
                    isLoggedIn = false,
                    isLoading = false,
                )
            return
        }

        requestJob?.cancel()
        _uiState.update { current ->
            current.copy(
                isLoggedIn = true,
                isLoading = true,
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
                                items = items,
                            )
                    },
                    onFailure = {
                        _uiState.update { current ->
                            current.copy(
                                isLoggedIn = true,
                                isLoading = false,
                                errorMessage = NETWORK_ERROR_MESSAGE,
                            )
                        }
                    },
                )
            }
    }

    companion object {
        private const val NETWORK_ERROR_MESSAGE = "加载失败，请重试"

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
