package app.tiebalite.core.data.myforums.mapper

import app.tiebalite.core.data.common.mapper.normalizeUrl
import app.tiebalite.core.model.myforums.MyForumItem
import app.tiebalite.core.network.proto.forumguide.ForumGuideLikeForumLite

class MyForumsMapper {
    fun map(rawForums: List<ForumGuideLikeForumLite>): List<MyForumItem> =
        rawForums.map { forum ->
            MyForumItem(
                forumId = forum.forumId,
                forumName = forum.forumName,
                avatarUrl = normalizeUrl(forum.avatar),
                levelId = forum.levelId,
                isSigned = forum.isSign == 1,
                hotNum = forum.hotNum,
            )
        }
}
