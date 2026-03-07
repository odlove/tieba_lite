package app.tiebalite.feature.thread.subposts

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ThreadSubPostsViewModel(
    private val threadId: Long,
    private val postId: Long,
    private val repository: ThreadRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ThreadSubPostsUiState())
    val uiState: StateFlow<ThreadSubPostsUiState> = _uiState.asStateFlow()

    private var requestJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        requestJob?.cancel()
        _uiState.update { current ->
            current.copy(
                isInitialLoading = current.subPosts.isEmpty(),
                isLoadingMore = false,
                errorMessage = null,
            )
        }
        requestJob =
            viewModelScope.launch {
                repository.loadThreadSubPostsPage(
                    threadId = threadId,
                    postId = postId,
                    page = FIRST_PAGE,
                ).fold(
                    onSuccess = { page ->
                        _uiState.update { current ->
                            current.copy(
                                post = page.post,
                                subPosts = page.subPosts,
                                threadAuthorId = page.threadAuthorId,
                                currentPage = page.currentPage.takeIf { value -> value > 0 } ?: FIRST_PAGE,
                                hasMore = page.hasMore,
                                totalCount = page.totalCount,
                                isInitialLoading = false,
                                isLoadingMore = false,
                                errorMessage = null,
                            )
                        }
                    },
                    onFailure = {
                        _uiState.update { current ->
                            current.copy(
                                isInitialLoading = false,
                                isLoadingMore = false,
                                errorMessage =
                                    if (current.subPosts.isEmpty()) {
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

    fun loadMore() {
        val state = _uiState.value
        if (state.isInitialLoading || state.isLoadingMore || !state.hasMore) {
            return
        }
        requestJob?.cancel()
        _uiState.update { current ->
            current.copy(
                isLoadingMore = true,
                errorMessage = null,
            )
        }
        val nextPage = state.currentPage + 1
        requestJob =
            viewModelScope.launch {
                repository.loadThreadSubPostsPage(
                    threadId = threadId,
                    postId = postId,
                    page = nextPage,
                ).fold(
                    onSuccess = { page ->
                        _uiState.update { current ->
                            current.copy(
                                post = current.post ?: page.post,
                                subPosts = (current.subPosts + page.subPosts).distinctBy { subPost -> subPost.id },
                                threadAuthorId = current.threadAuthorId ?: page.threadAuthorId,
                                currentPage = page.currentPage.takeIf { value -> value > 0 } ?: nextPage,
                                hasMore = page.hasMore,
                                totalCount = page.totalCount.takeIf { count -> count > 0 } ?: current.totalCount,
                                isLoadingMore = false,
                                errorMessage = null,
                            )
                        }
                    },
                    onFailure = {
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

    companion object {
        private const val FIRST_PAGE = 1
        private const val NETWORK_ERROR_MESSAGE = "网络错误"

        fun factory(
            threadId: Long,
            postId: Long,
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = checkNotNull(this[APPLICATION_KEY])
                    val authGraph =
                        (application as? AuthGraphProvider)?.authGraph
                            ?: error("Application must implement AuthGraphProvider")
                    val authReader = authGraph.authReader
                    ThreadSubPostsViewModel(
                        threadId = threadId,
                        postId = postId,
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
