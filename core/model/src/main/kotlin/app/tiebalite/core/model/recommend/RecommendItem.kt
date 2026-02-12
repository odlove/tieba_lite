package app.tiebalite.core.model.recommend

data class RecommendItem(
    val id: String,
    val title: String,
    val snippet: String? = null,
    val authorName: String? = null,
    val authorAvatarUrl: String? = null,
    val coverImageUrl: String? = null,
    val replyCount: Int = 0,
    val agreeCount: Int = 0,
    val shareCount: Long = 0,
    val lastTimeTimestampSeconds: Long? = null,
)
