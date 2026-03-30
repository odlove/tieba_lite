package app.tiebalite.core.model.thread

data class ThreadPage(
    val threadId: Long,
    val forumId: Long? = null,
    val forumName: String? = null,
    val forumAvatarUrl: String? = null,
    val firstFloorPost: ThreadFirstFloorPost? = null,
    val currentPage: Int = 1,
    val totalPage: Int = 1,
    val nextPagePostId: Long = 0L,
    val containsFirstFloorPost: Boolean = false,
    val hasMore: Boolean = false,
    val hasPrevious: Boolean = false,
    val posts: List<ThreadPost> = emptyList(),
)
