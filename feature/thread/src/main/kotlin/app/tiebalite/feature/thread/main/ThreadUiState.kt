package app.tiebalite.feature.thread.main

import app.tiebalite.core.model.thread.ThreadFirstFloorPost
import app.tiebalite.core.model.thread.ThreadPost

data class ThreadUiState(
    val forumName: String? = null,
    val forumAvatarUrl: String? = null,
    val firstFloorPost: ThreadFirstFloorPost? = null,
    val posts: List<ThreadPost> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val errorMessage: String? = null,
)
