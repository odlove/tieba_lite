package app.tiebalite.feature.thread

import app.tiebalite.core.model.thread.ThreadPost

data class ThreadUiState(
    val title: String = "帖子",
    val forumName: String? = null,
    val forumAvatarUrl: String? = null,
    val posts: List<ThreadPost> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)
