package app.tiebalite.core.model.thread

data class ThreadPost(
    val id: Long,
    val floor: Int,
    val authorId: Long,
    val authorName: String? = null,
    val authorLevel: Int = 0,
    val authorAvatarUrl: String? = null,
    val ipLocation: String? = null,
    val contentText: String,
    val imageUrls: List<String> = emptyList(),
    val timestampSeconds: Long? = null,
)
