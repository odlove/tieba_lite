package app.tiebalite.core.model.thread

data class ThreadFirstFloorPost(
    val title: String,
    val authorId: Long,
    val authorName: String? = null,
    val authorLevel: Int = 0,
    val authorAvatarUrl: String? = null,
    val ipLocation: String? = null,
    val body: ThreadPostBody = ThreadPostBody(),
    val timestampSeconds: Long? = null,
    val agreeCount: Long = 0,
)
