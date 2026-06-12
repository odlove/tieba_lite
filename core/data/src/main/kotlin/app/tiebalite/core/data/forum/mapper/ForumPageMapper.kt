package app.tiebalite.core.data.forum.mapper

import app.tiebalite.core.data.common.mapper.FeedItemMappingContext
import app.tiebalite.core.data.common.mapper.normalizeUrl
import app.tiebalite.core.data.common.mapper.toRecommendItem
import app.tiebalite.core.model.forum.ForumHeader
import app.tiebalite.core.model.forum.ForumPage
import app.tiebalite.core.network.source.tbclient.forum.FrsPageRaw

class ForumPageMapper {
    fun map(
        raw: FrsPageRaw,
        requestedForumName: String,
        fallbackCurrentPage: Int,
    ): ForumPage {
        val data = raw.response.data
        val forum = data.forum
        val forumName = forum.name.ifBlank { requestedForumName }
        val mappingContext = FeedItemMappingContext(userMap = data.userListList.associateBy { it.id })
        return ForumPage(
            header =
                ForumHeader(
                    forumId = forum.id,
                    forumName = forumName,
                    avatarUrl = normalizeUrl(forum.avatar),
                    slogan = forum.slogan.ifBlank { null },
                    memberCount = forum.memberNum,
                    threadCount = forum.threadNum,
                    postCount = forum.postNum,
                    isLiked = forum.isLike == 1,
                    userLevel = forum.userLevel,
                    levelName = forum.levelName.ifBlank { null },
                    currentScore = forum.curScore,
                    nextLevelScore = forum.levelupScore,
                    isSigned = forum.signInInfo.userInfo.isSignIn == 1,
                    continuousSignDays = forum.signInInfo.userInfo.contSignNum,
                ),
            items =
                data.pageData.feedListList.mapNotNull { layout ->
                    if (layout.layout != FEED_LAYOUT) {
                        return@mapNotNull null
                    }
                    layout.feed.toRecommendItem(mappingContext)
                },
            currentPage = data.page.currentPage.takeIf { it > 0 } ?: fallbackCurrentPage,
            hasMore = data.page.hasMore == 1,
        )
    }

    private companion object {
        private const val FEED_LAYOUT = "feed"
    }
}
