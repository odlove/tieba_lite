package app.tiebalite.core.data.forum.mapper

import app.tiebalite.core.model.forum.ForumHeader
import app.tiebalite.core.model.forum.ForumPage
import app.tiebalite.core.model.recommend.RecommendImage
import app.tiebalite.core.model.recommend.RecommendItem
import app.tiebalite.core.network.proto.recommend.MediaLite
import app.tiebalite.core.network.proto.recommend.ThreadInfoLite
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
                data.threadListList.map { thread ->
                    mapThread(thread = thread, userMap = userMap)
                },
            currentPage = data.page.currentPage.takeIf { it > 0 } ?: fallbackCurrentPage,
            hasMore = data.page.hasMore == 1,
        )
    }

    private fun mapThread(
        thread: ThreadInfoLite,
        userMap: Map<Long, UserLite>,
    ): RecommendItem {
        val author = resolveAuthor(thread, userMap)
        val snippet =
            thread.abstractItemsList
                .asSequence()
                .map { it.text.trim() }
                .firstOrNull { it.isNotBlank() }
        val images =
            thread.mediaList
                .asSequence()
                .mapNotNull(::mapImage)
                .distinctBy { it.url }
                .toList()
        val threadId = thread.tid.takeIf { it != 0L } ?: thread.id
        return RecommendItem(
            id = threadId.toString(),
            title = thread.title.ifBlank { "(无标题)" },
            forumName = null,
            forumAvatarUrl = null,
            snippet = snippet,
            authorName = resolveAuthorName(author),
            authorAvatarUrl = portraitToAvatarUrl(author?.portrait),
            images = images,
            replyCount = thread.replyNum,
            agreeCount = thread.agreeNum,
            shareCount = thread.shareNum,
            lastTimeTimestampSeconds =
                thread.lastTimeInt
                    .takeIf { it > 0 }
                    ?.toLong()
                    ?: thread.createTime
                        .takeIf { it > 0 }
                        ?.toLong(),
            isTop = thread.isTop == 1,
        )
    }

    private fun resolveAuthor(
        thread: ThreadInfoLite,
        userMap: Map<Long, UserLite>,
    ): UserLite? {
        val author = thread.author
        return when {
            author.id > 0L -> author
            thread.authorId > 0L -> userMap[thread.authorId]
            author.name.isNotBlank() || author.nameShow.isNotBlank() || author.portrait.isNotBlank() -> author
            else -> null
        }
    }

    private fun resolveAuthorName(author: UserLite?): String? {
        author ?: return null
        return author.nameShow.ifBlank { author.name }.ifBlank { null }
    }

    private fun portraitToAvatarUrl(portrait: String?): String? {
        val value = portrait?.trim().orEmpty()
        if (value.isBlank()) {
            return null
        }
        return if (value.startsWith("http://") || value.startsWith("https://")) {
            value
        } else {
            "http://tb.himg.baidu.com/sys/portrait/item/$value"
        }
    }

    private fun mapImage(media: MediaLite): RecommendImage? {
        val url =
            normalizeUrl(media.originPic)
                ?: normalizeUrl(media.bigPic)
                ?: normalizeUrl(media.srcPic)
                ?: return null
        return RecommendImage(
            url = url,
            width = media.width.takeIf { it > 0 },
            height = media.height.takeIf { it > 0 },
        )
    }

    private fun normalizeUrl(raw: String?): String? {
        val value = raw?.trim().orEmpty()
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
}
