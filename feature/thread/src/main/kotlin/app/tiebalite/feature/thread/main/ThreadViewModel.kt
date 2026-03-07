package app.tiebalite.feature.thread.main

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

    fun loadMore() {
        val state = _uiState.value
        if (state.isInitialLoading || state.isRefreshing || state.isLoadingMore) {
            return
        }
        if (state.firstFloorPost == null && state.posts.isEmpty()) {
            return
        }

        val loadLatestPosts = !state.hasMore

        requestJob?.cancel()
        _uiState.update {
            it.copy(
                isLoadingMore = true,
                errorMessage = null,
            )
        }

        val latestPostId =
            state.posts
                .maxByOrNull { post -> post.floor }
                ?.id
                ?: state.posts.lastOrNull()?.id
                ?: 0L
        val pageToLoad = if (loadLatestPosts) LATEST_POSTS_PAGE else state.currentPage + 1
        val postId = if (loadLatestPosts) latestPostId else 0L
        val lastPostId = if (loadLatestPosts) latestPostId else null

        requestJob =
            viewModelScope.launch {
                repository.loadThreadPage(
                    threadId = threadId,
                    page = pageToLoad,
                    postId = postId,
                    lastPostId = lastPostId,
                ).fold(
                    onSuccess = { page ->
                        _uiState.update { current ->
                            current.copy(
                                forumName = page.forumName,
                                forumAvatarUrl = page.forumAvatarUrl,
                                firstFloorPost = current.firstFloorPost ?: page.firstFloorPost,
                                posts = (current.posts + page.posts).distinctBy { post -> post.id },
                                isLoadingMore = false,
                                currentPage = maxOf(current.currentPage, page.currentPage),
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
                isInitialLoading = initial && current.posts.isEmpty(),
                isRefreshing = !initial,
                isLoadingMore = false,
                errorMessage = null,
            )
        }

        requestJob =
            viewModelScope.launch {
                repository.loadThreadPage(
                    threadId = threadId,
                    page = FIRST_PAGE,
                ).fold(
                    onSuccess = { page ->
                        _uiState.update {
                            it.copy(
                                forumName = page.forumName,
                                forumAvatarUrl = page.forumAvatarUrl,
                                firstFloorPost = page.firstFloorPost,
                                posts = page.posts,
                                isInitialLoading = false,
                                isRefreshing = false,
                                isLoadingMore = false,
                                currentPage = page.currentPage.takeIf { value -> value > 0 } ?: FIRST_PAGE,
                                hasMore = page.hasMore,
                                errorMessage = null,
                            )
                        }
                    },
                    onFailure = {
                        val hasContent =
                            _uiState.value.firstFloorPost != null ||
                                _uiState.value.posts.isNotEmpty()
                        if (hasContent) {
                            emitNetworkError()
                        }
                        _uiState.update { current ->
                            current.copy(
                                isInitialLoading = false,
                                isRefreshing = false,
                                isLoadingMore = false,
                                errorMessage =
                                    if (current.firstFloorPost == null && current.posts.isEmpty()) {
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
        _uiEvents.tryEmit(ThreadUiEvent.ShowToast(NETWORK_ERROR_MESSAGE))
    }

    companion object {
        private const val FIRST_PAGE = 1
        private const val LATEST_POSTS_PAGE = 0
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
