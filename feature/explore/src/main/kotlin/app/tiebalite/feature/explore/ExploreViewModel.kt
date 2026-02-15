package app.tiebalite.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.tiebalite.core.data.auth.di.AuthGraphProvider
import app.tiebalite.core.data.recommend.repository.RecommendLoadType
import app.tiebalite.core.data.recommend.repository.RecommendRepository
import app.tiebalite.core.data.recommend.repository.RecommendRepositoryFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val repository: RecommendRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()
    private val _uiEvents = MutableSharedFlow<ExploreUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<ExploreUiEvent> = _uiEvents.asSharedFlow()

    private var requestJob: Job? = null

    init {
        refreshInternal(initial = true)
    }

    fun refresh() {
        refreshInternal(initial = false)
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isInitialLoading || state.isRefreshing || state.isLoadingMore) {
            return
        }
        if (state.items.isEmpty()) {
            return
        }

        requestJob?.cancel()
        _uiState.update {
            it.copy(
                isLoadingMore = true,
                errorMessage = null,
            )
        }

        val pageToLoad = state.nextPage
        requestJob =
            viewModelScope.launch {
                repository.loadFeed(
                    loadType = RecommendLoadType.LoadMore,
                    page = pageToLoad,
                ).fold(
                    onSuccess = { fetchedItems ->
                        _uiState.update { current ->
                            current.copy(
                                items = (current.items + fetchedItems).distinctBy { item -> item.id },
                                isLoadingMore = false,
                                nextPage = current.nextPage + 1,
                                errorMessage = null,
                            )
                        }
                    },
                    onFailure = {
                        emitNetworkError()
                        _uiState.update { current ->
                            current.copy(
                                isLoadingMore = false,
                                errorMessage = null,
                            )
                        }
                    },
                )
            }
    }

    private fun refreshInternal(initial: Boolean) {
        requestJob?.cancel()
        _uiState.update { current ->
            current.copy(
                isInitialLoading = initial && current.items.isEmpty(),
                isRefreshing = !initial,
                isLoadingMore = false,
                errorMessage = null,
            )
        }

        requestJob =
            viewModelScope.launch {
                repository.loadFeed(
                    loadType = RecommendLoadType.Refresh,
                    page = FIRST_PAGE,
                ).fold(
                    onSuccess = { fetchedItems ->
                        _uiState.update { current ->
                            current.copy(
                                items = fetchedItems,
                                isInitialLoading = false,
                                isRefreshing = false,
                                isLoadingMore = false,
                                nextPage = FIRST_PAGE + 1,
                                errorMessage = null,
                            )
                        }
                    },
                    onFailure = {
                        val hasItems = _uiState.value.items.isNotEmpty()
                        if (hasItems) {
                            emitNetworkError()
                        }
                        _uiState.update { current ->
                            current.copy(
                                isInitialLoading = false,
                                isRefreshing = false,
                                isLoadingMore = false,
                                errorMessage =
                                    if (current.items.isEmpty()) {
                                        NETWORK_ERROR_MESSAGE
                                    } else {
                                        null
                                    },
                            )
                        }
                    },
                )
            }
    }

    private fun emitNetworkError() {
        _uiEvents.tryEmit(
            ExploreUiEvent.ShowToast(message = NETWORK_ERROR_MESSAGE),
        )
    }

    companion object {
        private const val FIRST_PAGE = 1
        private const val NETWORK_ERROR_MESSAGE = "网络错误"

        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = checkNotNull(this[APPLICATION_KEY])
                    val authGraph =
                        (application as? AuthGraphProvider)?.authGraph
                            ?: error("Application must implement AuthGraphProvider")
                    val authReader = authGraph.authReader
                    ExploreViewModel(
                        repository =
                            RecommendRepositoryFactory.create(
                                sessionProvider = { authReader.currentSession() },
                                tbsProvider = { authReader.currentSession()?.tbs },
                            ),
                    )
                }
            }
    }
}
