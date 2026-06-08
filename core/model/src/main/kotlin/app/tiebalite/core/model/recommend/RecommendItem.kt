package app.tiebalite.core.model.recommend

import app.tiebalite.core.model.text.RichText

data class RecommendItem(
    val id: String,
    val title: RichText,
    val forumName: String? = null,
    val forumAvatarUrl: String? = null,
    val snippet: RichText? = null,
    val authorName: String? = null,
    val authorAvatarUrl: String? = null,
    val images: List<RecommendImage> = emptyList(),
    val replyCount: Int = 0,
    val agreeCount: Int = 0,
    val shareCount: Long = 0,
    val lastTimeTimestampSeconds: Long? = null,
    val isTop: Boolean = false,
) {
    val coverImageUrl: String?
        get() = images.firstOrNull()?.url

    val titleText: String
        get() = title.plainText

    val snippetText: String?
        get() = snippet?.plainText
}

data class RecommendImage(
    val url: String,
    val width: Int? = null,
    val height: Int? = null,
)
