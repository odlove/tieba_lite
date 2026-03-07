package app.tiebalite.core.model.thread

data class ThreadSubPost(
    val id: Long,
    val floor: Int,
    val authorId: Long,
    val authorName: String? = null,
    val authorLevel: Int = 0,
    val authorAvatarUrl: String? = null,
    val ipLocation: String? = null,
    val body: ThreadPostBody = ThreadPostBody(),
    val timestampSeconds: Long? = null,
)
