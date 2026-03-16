package app.tiebalite.core.model.myforums

data class MyForumItem(
    val forumId: Long,
    val forumName: String,
    val avatarUrl: String? = null,
    val levelId: Int = 0,
    val isSigned: Boolean = false,
    val hotNum: Int = 0,
)
