package app.tiebalite.core.model.forum

data class ForumHeader(
    val forumId: Long = 0L,
    val forumName: String,
    val avatarUrl: String? = null,
    val slogan: String? = null,
    val memberCount: Int = 0,
    val threadCount: Int = 0,
    val postCount: Int = 0,
    val isLiked: Boolean = false,
    val userLevel: Int = 0,
    val levelName: String? = null,
    val currentScore: Int = 0,
    val nextLevelScore: Int = 0,
    val isSigned: Boolean = false,
    val continuousSignDays: Int = 0,
)
