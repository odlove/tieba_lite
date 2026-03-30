package app.tiebalite.feature.thread.main.state

import app.tiebalite.core.model.thread.ThreadFirstFloorPost
import app.tiebalite.core.model.thread.ThreadPost

data class ThreadUiState(
    val forumId: Long? = null,
    val forumName: String? = null,
    val forumAvatarUrl: String? = null,
    val firstFloorPost: ThreadFirstFloorPost? = null,
    val posts: List<ThreadPost> = emptyList(),
    val sortType: Int = ThreadReplySortType.Ascending,
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val totalPage: Int = 1,
    val nextPagePostId: Long = 0L,
    val hasMore: Boolean = true,
    val canLoadMoreBelow: Boolean = true,
    val hasPrevious: Boolean = false,
    val errorMessage: String? = null,
)
