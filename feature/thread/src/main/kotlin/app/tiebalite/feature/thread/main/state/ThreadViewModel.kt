package app.tiebalite.feature.thread.main.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.tiebalite.core.data.auth.di.AuthGraphProvider
import app.tiebalite.core.data.di.ApplicationScopeProvider
import app.tiebalite.core.data.history.repository.ThreadHistoryRepository
import app.tiebalite.core.data.history.repository.ThreadHistoryRepositoryFactory
import app.tiebalite.core.data.thread.repository.ThreadRepository
import app.tiebalite.core.data.thread.repository.ThreadRepositoryFactory
import app.tiebalite.core.model.history.ThreadHistoryEntry
import app.tiebalite.core.model.thread.ThreadPage
import kotlinx.coroutines.CoroutineScope
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
    private val historyRepository: ThreadHistoryRepository,
    private val applicationScope: CoroutineScope,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ThreadUiState())
    val uiState: StateFlow<ThreadUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<ThreadUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<ThreadUiEvent> = _uiEvents.asSharedFlow()

    private var requestJob: Job? = null
    private var activeVisitLogId: Long? = null

    init {
        refreshInternal(initial = true)
    }

    fun refresh() {
        refreshInternal(initial = false)
    }

    fun copyThreadLink() {
        _uiEvents.tryEmit(ThreadUiEvent.CopyThreadLink(threadId))
    }

    fun setSortType(sortType: Int) {
        if (_uiState.value.sortType == sortType) {
            return
        }
        refreshInternal(initial = false, sortType = sortType)
    }

    fun setSeeLz(seeLz: Boolean) {
        if (_uiState.value.seeLz == seeLz) {
            return
        }
        refreshInternal(initial = false, seeLz = seeLz)
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isInitialLoading || state.isRefreshing || state.isLoadingMore) {
            return
        }
        if (state.firstFloorPost == null && state.posts.isEmpty()) {
            return
        }

        if (!state.canLoadMoreBelow && state.sortType == ThreadReplySortType.Descending) {
            return
        }

        val loadLatestPosts = !state.canLoadMoreBelow && state.sortType == ThreadReplySortType.Ascending

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
        val pageToLoad =
            if (loadLatestPosts) {
                FIRST_PAGE
            } else {
                nextPageToLoad(state)
            }
        val postId =
            when {
                loadLatestPosts -> latestPostId
                state.sortType == ThreadReplySortType.Descending -> state.nextPagePostId
                else -> 0L
            }
        val lastPostId = if (loadLatestPosts) latestPostId else null

        requestJob =
            viewModelScope.launch {
                repository.loadThreadPage(
                    threadId = threadId,
                    page = pageToLoad,
                    postId = postId,
                    seeLz = state.seeLz,
                    sortType = state.sortType,
                    lastPostId = lastPostId,
                ).fold(
                    onSuccess = { page ->
                        val canLoadMoreBelow = resolveCanLoadMoreBelow(page = page, sortType = state.sortType)
                        val nextPagePostId = resolveNextPagePostId(page = page, sortType = state.sortType)
                        _uiState.update { current ->
                            current.copy(
                                forumId = page.forumId ?: current.forumId,
                                forumName = page.forumName ?: current.forumName,
                                forumAvatarUrl = page.forumAvatarUrl ?: current.forumAvatarUrl,
                                firstFloorPost = current.firstFloorPost ?: page.firstFloorPost,
                                posts = (current.posts + page.posts).distinctBy { post -> post.id },
                                isLoadingMore = false,
                                currentPage = page.currentPage.takeIf { value -> value > 0 } ?: current.currentPage,
                                totalPage = page.totalPage.takeIf { value -> value > 0 } ?: current.totalPage,
                                nextPagePostId = nextPagePostId,
                                hasMore = page.hasMore,
                                canLoadMoreBelow = canLoadMoreBelow,
                                hasPrevious = page.hasPrevious,
                                errorMessage = null,
                            )
                        }
                        recordThreadEnteredIfNeeded(page)
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

    private fun refreshInternal(
        initial: Boolean,
        seeLz: Boolean = _uiState.value.seeLz,
        sortType: Int = _uiState.value.sortType,
    ) {
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
                    seeLz = seeLz,
                    sortType = sortType,
                ).fold(
                    onSuccess = { page ->
                        val canLoadMoreBelow = resolveCanLoadMoreBelow(page = page, sortType = sortType)
                        val nextPagePostId = resolveNextPagePostId(page = page, sortType = sortType)
                        _uiState.update { current ->
                            current.copy(
                                forumId = page.forumId,
                                forumName = page.forumName,
                                forumAvatarUrl = page.forumAvatarUrl,
                                firstFloorPost = page.firstFloorPost ?: current.firstFloorPost,
                                posts = page.posts,
                                seeLz = seeLz,
                                isInitialLoading = false,
                                isRefreshing = false,
                                isLoadingMore = false,
                                currentPage = page.currentPage.takeIf { value -> value > 0 } ?: FIRST_RESPONSE_PAGE,
                                totalPage = page.totalPage.takeIf { value -> value > 0 } ?: FIRST_RESPONSE_PAGE,
                                nextPagePostId = nextPagePostId,
                                hasMore = page.hasMore,
                                canLoadMoreBelow = canLoadMoreBelow,
                                hasPrevious = page.hasPrevious,
                                sortType = sortType,
                                errorMessage = null,
                            )
                        }
                        recordThreadEnteredIfNeeded(page)
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

    override fun onCleared() {
        val visitLogId = activeVisitLogId
        if (visitLogId != null) {
            applicationScope.launch {
                runCatching {
                    historyRepository.onThreadLeft(
                        visitLogId = visitLogId,
                    )
                }
            }
        }
        super.onCleared()
    }

    private suspend fun recordThreadEnteredIfNeeded(page: ThreadPage) {
        if (activeVisitLogId != null) {
            return
        }
        val firstFloorPost = page.firstFloorPost ?: return
        runCatching {
            historyRepository.onThreadEntered(
                ThreadHistoryEntry(
                    threadId = page.threadId,
                    title = firstFloorPost.title,
                    authorName = firstFloorPost.authorName,
                    authorAvatarUrl = firstFloorPost.authorAvatarUrl,
                    forumId = page.forumId,
                    forumName = page.forumName,
                    forumAvatarUrl = page.forumAvatarUrl,
                ),
            )
        }.onSuccess { visitLogId ->
            activeVisitLogId = visitLogId
        }
    }

    private fun emitNetworkError() {
        _uiEvents.tryEmit(ThreadUiEvent.ShowToast(NETWORK_ERROR_MESSAGE))
    }

    private fun nextPageToLoad(state: ThreadUiState): Int =
        // currentPage is only kept as a best-effort input for the next pn value.
        // The API can report unstable paging metadata, especially in descending mode.
        when (state.sortType) {
            ThreadReplySortType.Descending ->
                (state.totalPage - state.currentPage)
                    .coerceAtLeast(FIRST_RESPONSE_PAGE)

            else -> state.currentPage + 1
        }

    private fun resolveCanLoadMoreBelow(
        page: ThreadPage,
        sortType: Int,
    ): Boolean =
        when (sortType) {
            // Descending paging metadata is unreliable: the API can keep hasMore=true even on
            // the final chunk and does not expose a clear "reached oldest reply" flag, so we
            // treat "this response already contains floor 1" as the end condition.
            ThreadReplySortType.Descending -> !page.containsFirstFloorPost

            else -> page.hasMore
        }

    private fun resolveNextPagePostId(
        page: ThreadPage,
        sortType: Int,
    ): Long =
        when (sortType) {
            // In descending mode, using the raw pids order can make the next request overlap
            // with replies we already have, especially right after the first load. Use the
            // lowest-floor reply from this response as the next pid instead.
            ThreadReplySortType.Descending -> page.posts.minByOrNull { post -> post.floor }?.id ?: page.nextPagePostId

            else -> page.nextPagePostId
        }

    companion object {
        private const val FIRST_PAGE = 0
        private const val FIRST_RESPONSE_PAGE = 1
        private const val NETWORK_ERROR_MESSAGE = "网络错误"

        fun factory(threadId: Long): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = checkNotNull(this[APPLICATION_KEY])
                    val authGraph =
                        (application as? AuthGraphProvider)?.authGraph
                            ?: error("Application must implement AuthGraphProvider")
                    val applicationScope =
                        (application as? ApplicationScopeProvider)?.applicationScope
                            ?: error("Application must implement ApplicationScopeProvider")
                    val authReader = authGraph.authReader
                    ThreadViewModel(
                        threadId = threadId,
                        repository =
                            ThreadRepositoryFactory.create(
                                sessionProvider = { authReader.currentSession() },
                                tbsProvider = { authReader.currentSession()?.tbs },
                            ),
                        historyRepository = ThreadHistoryRepositoryFactory.create(application),
                        applicationScope = applicationScope,
                    )
                }
            }
    }
}
