package app.tiebalite.feature.forum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.tiebalite.core.data.auth.di.AuthGraphProvider
import app.tiebalite.core.data.forum.repository.ForumRepository
import app.tiebalite.core.data.forum.repository.ForumRepositoryFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForumViewModel(
    private val forumName: String,
    private val repository: ForumRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForumUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<ForumUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<ForumUiEvent> = _uiEvents.asSharedFlow()

    private var requestJob: Job? = null

    init {
        refreshInternal(initial = true)
    }

    fun refresh() {
        val hasContent = _uiState.value.header != null || _uiState.value.items.isNotEmpty()
        refreshInternal(initial = !hasContent)
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isInitialLoading || state.isRefreshing || state.isLoadingMore || !state.hasMore) {
            return
        }
        if (state.header == null && state.items.isEmpty()) {
            return
        }

        requestJob?.cancel()
        _uiState.update { current ->
            current.copy(
                isLoadingMore = true,
                errorMessage = null,
            )
        }

        requestJob =
            viewModelScope.launch {
                repository.loadForumPage(
                    forumName = forumName,
                    page = state.currentPage + 1,
                    loadType = LOAD_TYPE_MORE,
                ).fold(
                    onSuccess = { page ->
                        _uiState.update { current ->
                            current.copy(
                                header = page.header,
                                items = (current.items + page.items).distinctBy { item -> item.id },
                                isLoadingMore = false,
                                currentPage = page.currentPage.coerceAtLeast(current.currentPage + 1),
                                hasMore = page.hasMore,
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
                isInitialLoading = initial && current.items.isEmpty() && current.header == null,
                isRefreshing = !initial,
                isLoadingMore = false,
                errorMessage = null,
            )
        }

        requestJob =
            viewModelScope.launch {
                repository.loadForumPage(
                    forumName = forumName,
                    page = FIRST_PAGE,
                    loadType = LOAD_TYPE_REFRESH,
                ).fold(
                    onSuccess = { page ->
                        _uiState.value =
                            ForumUiState(
                                header = page.header,
                                items = page.items,
                                isInitialLoading = false,
                                isRefreshing = false,
                                isLoadingMore = false,
                                currentPage = page.currentPage.takeIf { value -> value > 0 } ?: FIRST_PAGE,
                                hasMore = page.hasMore,
                                errorMessage = null,
                            )
                    },
                    onFailure = {
                        val hasContent = _uiState.value.header != null || _uiState.value.items.isNotEmpty()
                        if (hasContent) {
                            emitNetworkError()
                        }
                        _uiState.update { current ->
                            current.copy(
                                isInitialLoading = false,
                                isRefreshing = false,
                                isLoadingMore = false,
                                errorMessage = if (hasContent) null else NETWORK_ERROR_MESSAGE,
                            )
                        }
                    },
                )
            }
    }

    private fun emitNetworkError() {
        _uiEvents.tryEmit(ForumUiEvent.ShowToast(NETWORK_ERROR_MESSAGE))
    }

    companion object {
        private const val FIRST_PAGE = 1
        private const val LOAD_TYPE_REFRESH = 1
        private const val LOAD_TYPE_MORE = 2
        private const val NETWORK_ERROR_MESSAGE = "网络错误"

        fun factory(forumName: String): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = checkNotNull(this[APPLICATION_KEY])
                    val authGraph =
                        (application as? AuthGraphProvider)?.authGraph
                            ?: error("Application must implement AuthGraphProvider")
                    val authReader = authGraph.authReader
                    ForumViewModel(
                        forumName = forumName,
                        repository =
                            ForumRepositoryFactory.create(
                                sessionProvider = { authReader.currentSession() },
                            ),
                    )
                }
            }
    }
}
