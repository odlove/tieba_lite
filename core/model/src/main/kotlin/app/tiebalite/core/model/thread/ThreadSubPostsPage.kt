package app.tiebalite.core.model.thread

data class ThreadSubPostsPage(
    val threadId: Long,
    val forumId: Long = 0L,
    val forumName: String? = null,
    val threadAuthorId: Long? = null,
    val post: ThreadPost? = null,
    val currentPage: Int = 1,
    val hasMore: Boolean = false,
    val totalCount: Int = 0,
    val subPosts: List<ThreadSubPost> = emptyList(),
)
