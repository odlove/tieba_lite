package app.tiebalite.core.model.recommend

data class RecommendItem(
    val id: String,
    val title: String,
    val forumName: String? = null,
    val forumAvatarUrl: String? = null,
    val snippet: String? = null,
    val authorName: String? = null,
    val authorAvatarUrl: String? = null,
    val images: List<RecommendImage> = emptyList(),
    val replyCount: Int = 0,
    val agreeCount: Int = 0,
    val shareCount: Long = 0,
    val lastTimeTimestampSeconds: Long? = null,
) {
    val coverImageUrl: String?
        get() = images.firstOrNull()?.url
}

data class RecommendImage(
    val url: String,
    val width: Int? = null,
    val height: Int? = null,
)
