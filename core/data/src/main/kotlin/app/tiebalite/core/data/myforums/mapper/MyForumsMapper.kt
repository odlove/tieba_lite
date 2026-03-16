package app.tiebalite.core.data.myforums.mapper

import app.tiebalite.core.model.myforums.MyForumItem
import app.tiebalite.core.network.proto.forumguide.ForumGuideLikeForumLite

class MyForumsMapper {
    fun map(rawForums: List<ForumGuideLikeForumLite>): List<MyForumItem> =
        rawForums.map { forum ->
            MyForumItem(
                forumId = forum.forumId,
                forumName = forum.forumName,
                avatarUrl = forum.avatar.normalizeUrl(),
                levelId = forum.levelId,
                isSigned = forum.isSign == 1,
                hotNum = forum.hotNum,
            )
        }
}

private fun String.normalizeUrl(): String? {
    val value = trim()
    if (value.isBlank()) {
        return null
    }
    return when {
        value.startsWith("http://") -> "https://${value.removePrefix("http://")}"
        value.startsWith("https://") -> value
        value.startsWith("//") -> "https:$value"
        else -> value
    }
}
