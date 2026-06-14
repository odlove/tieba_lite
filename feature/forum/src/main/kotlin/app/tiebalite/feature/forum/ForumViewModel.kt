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
import app.tiebalite.core.model.error.userVisibleMessageOrNull
import app.tiebalite.core.model.forum.ForumHeader
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
    private var followJob: Job? = null
    private var followRevision = 0L

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

        val requestFollowRevision = followRevision
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
                                header =
                                    mergePageHeader(
                                        pageHeader = page.header,
                                        currentHeader = current.header,
                                        requestFollowRevision = requestFollowRevision,
                                    ),
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

    fun toggleForumLike() {
        val state = _uiState.value
        val header = state.header ?: return
        if (state.isFollowUpdating || header.forumId <= 0L) {
            return
        }

        followJob?.cancel()
        _uiState.update { current ->
            current.copy(isFollowUpdating = true)
        }
        followJob =
            viewModelScope.launch {
                val result =
                    if (header.isLiked) {
                        repository.unfollowForum(
                            forumId = header.forumId,
                            forumName = header.forumName,
                        )
                    } else {
                        repository.followForum(
                            forumId = header.forumId,
                            forumName = header.forumName,
                        )
                    }
                result.fold(
                    onSuccess = {
                        val liked = !header.isLiked
                        followRevision += 1
                        _uiState.update { current ->
                            current.copy(
                                header =
                                    current.header?.copy(
                                        isLiked = liked,
                                        isSigned = if (liked) current.header.isSigned else false,
                                        continuousSignDays = if (liked) current.header.continuousSignDays else 0,
                                    ),
                                isFollowUpdating = false,
                            )
                        }
                        _uiEvents.tryEmit(ForumUiEvent.ShowToast(if (liked) FOLLOW_SUCCESS_MESSAGE else UNFOLLOW_SUCCESS_MESSAGE))
                    },
                    onFailure = { throwable ->
                        _uiState.update { current ->
                            current.copy(isFollowUpdating = false)
                        }
                        _uiEvents.tryEmit(
                            ForumUiEvent.ShowToast(
                                forumLikeFailureMessage(
                                    isUnfollow = header.isLiked,
                                    throwable = throwable,
                                ),
                            ),
                        )
                    },
                )
            }
    }

    private fun refreshInternal(initial: Boolean) {
        requestJob?.cancel()
        val requestFollowRevision = followRevision
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
                        _uiState.update { current ->
                            current.copy(
                                header =
                                    mergePageHeader(
                                        pageHeader = page.header,
                                        currentHeader = current.header,
                                        requestFollowRevision = requestFollowRevision,
                                    ),
                                items = page.items,
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

    private fun mergePageHeader(
        pageHeader: ForumHeader,
        currentHeader: ForumHeader?,
        requestFollowRevision: Long,
    ): ForumHeader =
        if (currentHeader != null && requestFollowRevision != followRevision) {
            pageHeader.copy(
                isLiked = currentHeader.isLiked,
                isSigned = currentHeader.isSigned,
                continuousSignDays = currentHeader.continuousSignDays,
            )
        } else {
            pageHeader
        }

    private fun forumLikeFailureMessage(
        isUnfollow: Boolean,
        throwable: Throwable,
    ): String {
        val action = if (isUnfollow) UNFOLLOW_ACTION_NAME else FOLLOW_ACTION_NAME
        val reason = throwable.userVisibleMessageOrNull()
        return if (reason.isNullOrBlank()) {
            "${action}失败，请稍后重试"
        } else {
            "${action}失败：$reason"
        }
    }

    companion object {
        private const val FIRST_PAGE = 1
        private const val LOAD_TYPE_REFRESH = 1
        private const val LOAD_TYPE_MORE = 2
        private const val NETWORK_ERROR_MESSAGE = "网络错误"
        private const val FOLLOW_ACTION_NAME = "关注"
        private const val UNFOLLOW_ACTION_NAME = "取消关注"
        private const val FOLLOW_SUCCESS_MESSAGE = "关注成功"
        private const val UNFOLLOW_SUCCESS_MESSAGE = "已取消关注"

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
                                accountProvider = { authReader.currentAccount() },
                            ),
                    )
                }
            }
    }
}
