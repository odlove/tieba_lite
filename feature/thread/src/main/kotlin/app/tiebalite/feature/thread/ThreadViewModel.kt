package app.tiebalite.feature.thread

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.tiebalite.core.data.auth.di.AuthGraphProvider
import app.tiebalite.core.data.thread.repository.ThreadRepository
import app.tiebalite.core.data.thread.repository.ThreadRepositoryFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ThreadViewModel(
    private val threadId: Long,
    private val repository: ThreadRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ThreadUiState())
    val uiState: StateFlow<ThreadUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<ThreadUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<ThreadUiEvent> = _uiEvents.asSharedFlow()

    private var requestJob: Job? = null

    init {
        refreshInternal(initial = true)
    }

    fun refresh() {
        refreshInternal(initial = false)
    }

    private fun refreshInternal(initial: Boolean) {
        requestJob?.cancel()
        _uiState.update { current ->
            current.copy(
                isInitialLoading = initial && current.posts.isEmpty(),
                isRefreshing = !initial,
                errorMessage = null,
            )
        }

        requestJob =
            viewModelScope.launch {
                repository.loadThreadPage(threadId = threadId).fold(
                    onSuccess = { page ->
                        _uiState.update {
                            it.copy(
                                title = page.title,
                                forumName = page.forumName,
                                forumAvatarUrl = page.forumAvatarUrl,
                                posts = page.posts,
                                isInitialLoading = false,
                                isRefreshing = false,
                                errorMessage = null,
                            )
                        }
                    },
                    onFailure = {
                        val hasPosts = _uiState.value.posts.isNotEmpty()
                        if (hasPosts) {
                            _uiEvents.tryEmit(ThreadUiEvent.ShowToast(NETWORK_ERROR_MESSAGE))
                        }
                        _uiState.update { current ->
                            current.copy(
                                isInitialLoading = false,
                                isRefreshing = false,
                                errorMessage =
                                    if (current.posts.isEmpty()) {
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

    companion object {
        private const val NETWORK_ERROR_MESSAGE = "网络错误"

        fun factory(threadId: Long): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = checkNotNull(this[APPLICATION_KEY])
                    val authGraph =
                        (application as? AuthGraphProvider)?.authGraph
                            ?: error("Application must implement AuthGraphProvider")
                    val authReader = authGraph.authReader
                    ThreadViewModel(
                        threadId = threadId,
                        repository =
                            ThreadRepositoryFactory.create(
                                sessionProvider = { authReader.currentSession() },
                                tbsProvider = { authReader.currentSession()?.tbs },
                            ),
                    )
                }
            }
    }
}
