package app.tiebalite.feature.thread.subposts

import app.tiebalite.core.model.thread.ThreadPost
import app.tiebalite.core.model.thread.ThreadSubPost

data class ThreadSubPostsUiState(
    val post: ThreadPost? = null,
    val subPosts: List<ThreadSubPost> = emptyList(),
    val threadAuthorId: Long? = null,
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val totalCount: Int = 0,
    val isInitialLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
)
