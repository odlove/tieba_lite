package app.tiebalite.feature.thread.main.state

import app.tiebalite.core.model.thread.ThreadFirstFloorPost
import app.tiebalite.core.model.thread.ThreadPost

data class ThreadUiState(
    val forumId: Long? = null,
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
