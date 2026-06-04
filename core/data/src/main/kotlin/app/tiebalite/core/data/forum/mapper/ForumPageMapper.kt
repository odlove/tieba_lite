package app.tiebalite.core.data.forum.mapper

import app.tiebalite.core.data.common.mapper.normalizeUrl
import app.tiebalite.core.data.common.mapper.portraitToAvatarUrl
import app.tiebalite.core.model.forum.ForumHeader
import app.tiebalite.core.model.forum.ForumPage
import app.tiebalite.core.model.recommend.RecommendImage
import app.tiebalite.core.model.recommend.RecommendItem
import app.tiebalite.core.network.proto.frs.FrsFeedLite
import app.tiebalite.core.network.proto.frs.FrsPicLite
import app.tiebalite.core.network.proto.frs.FrsTextGroupLite
import app.tiebalite.core.network.proto.recommend.UserLite
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
        val userMap = data.userListList.associateBy { it.id }
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
                    mapFeed(feed = layout.feed, userMap = userMap)
                },
            currentPage = data.page.currentPage.takeIf { it > 0 } ?: fallbackCurrentPage,
            hasMore = data.page.hasMore == 1,
        )
    }

    private fun mapFeed(
        feed: FrsFeedLite,
        userMap: Map<Long, UserLite>,
    ): RecommendItem? {
        val businessInfo = feed.businessInfoList.associate { item -> item.key to item.value }
        val social =
            feed.componentsList
                .asSequence()
                .firstOrNull { component -> component.component == SOCIAL_COMPONENT }
                ?.feedSocial
        val threadId =
            social
                ?.tid
                ?.takeIf { it > 0L }
                ?: businessInfo["thread_id"]?.toLongOrNull()
                ?: feed.schema.threadIdFromSchema()
                ?: return null
        val author = businessInfo["user_id"]?.toLongOrNull()?.let(userMap::get)
        val title =
            feed.componentText(TITLE_COMPONENT)
                ?: businessInfo["title"]
                ?: return null
        val snippet = feed.componentText(ABSTRACT_COMPONENT) ?: businessInfo["abstract"]
        val images =
            feed.componentsList
                .asSequence()
                .filter { component -> component.component == PIC_COMPONENT }
                .flatMap { component -> component.feedPic.picsList.asSequence() }
                .mapNotNull { pic -> pic.toRecommendImage() }
                .distinctBy { it.url }
                .toList()
        return RecommendItem(
            id = threadId.toString(),
            title = title,
            forumName = null,
            forumAvatarUrl = null,
            snippet = snippet,
            authorName = resolveAuthorName(author),
            authorAvatarUrl = portraitToAvatarUrl(author?.portrait),
            images = images,
            replyCount = social?.commentNum ?: 0,
            agreeCount = social?.agree?.agreeNum?.toInt() ?: 0,
            shareCount = social?.shareNum?.toLong() ?: 0L,
            lastTimeTimestampSeconds =
                businessInfo["create_time"]
                    ?.toLongOrNull()
                    ?.takeIf { it > 0L },
            isTop = false,
        )
    }

    private fun resolveAuthorName(author: UserLite?): String? {
        author ?: return null
        return author.nameShow.ifBlank { author.name }.ifBlank { null }
    }

    private fun FrsFeedLite.componentText(componentName: String): String? =
        componentsList
            .asSequence()
            .firstOrNull { component -> component.component == componentName }
            ?.let { component ->
                when (componentName) {
                    TITLE_COMPONENT -> component.feedTitle
                    ABSTRACT_COMPONENT -> component.feedAbstract
                    else -> null
                }
            }
            ?.text()

    private fun FrsTextGroupLite.text(): String? =
        dataList
            .asSequence()
            .map { item -> item.textInfo.text.trim() }
            .filter { text -> text.isNotBlank() }
            .joinToString(separator = "")
            .ifBlank { null }

    private fun FrsPicLite.toRecommendImage(): RecommendImage? {
        val url =
            normalizeUrl(originPicUrl)
                ?: normalizeUrl(bigPicUrl)
                ?: normalizeUrl(smallPicUrl)
                ?: return null
        return RecommendImage(
            url = url,
            width = width.takeIf { it > 0 },
            height = height.takeIf { it > 0 },
        )
    }

    private fun String.threadIdFromSchema(): Long? {
        if (isBlank()) {
            return null
        }
        val match = THREAD_ID_PATTERN.find(this) ?: return null
        return match.groupValues[1].toLongOrNull()
    }

    private companion object {
        private const val FEED_LAYOUT = "feed"
        private const val TITLE_COMPONENT = "feed_title"
        private const val ABSTRACT_COMPONENT = "feed_abstract"
        private const val PIC_COMPONENT = "feed_pic"
        private const val SOCIAL_COMPONENT = "feed_social"
        private val THREAD_ID_PATTERN = Regex("""(?:%22(?:tid|threadId|thread_id)%22%3A|["?&](?:tid|threadId|thread_id)["=:%]+)(\d+)""")
    }

}
