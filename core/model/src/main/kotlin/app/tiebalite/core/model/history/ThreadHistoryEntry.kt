package app.tiebalite.core.model.history

data class ThreadHistoryEntry(
    val threadId: Long,
    val title: String,
    val authorName: String? = null,
    val authorAvatarUrl: String? = null,
    val forumId: Long? = null,
    val forumName: String? = null,
    val forumAvatarUrl: String? = null,
)
