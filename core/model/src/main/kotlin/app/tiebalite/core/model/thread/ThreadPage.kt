package app.tiebalite.core.model.thread

data class ThreadPage(
    val threadId: Long,
    val title: String,
    val forumName: String? = null,
    val forumAvatarUrl: String? = null,
    val authorName: String? = null,
    val authorAvatarUrl: String? = null,
    val currentPage: Int = 1,
    val hasMore: Boolean = false,
    val posts: List<ThreadPost> = emptyList(),
)
