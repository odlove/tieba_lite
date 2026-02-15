package app.tiebalite.core.data.thread.mapper

import app.tiebalite.core.model.thread.ThreadPage
import app.tiebalite.core.model.thread.ThreadPost
import app.tiebalite.core.network.proto.thread.ThreadPostLite
import app.tiebalite.core.network.proto.thread.ThreadUserLite
import app.tiebalite.core.network.source.tbclient.thread.PbPageRaw

class ThreadPageMapper {
    fun map(raw: PbPageRaw): ThreadPage {
        val data = raw.response.data
        val thread = data.thread
        val threadId = thread.tid.takeIf { it > 0L } ?: thread.id
        val userMap =
            data.userListList
                .associateBy { user ->
                    user.id
                }
        val posts = buildPostList(raw = raw).map { post ->
            mapPost(
                post = post,
                userMap = userMap,
            )
        }

        return ThreadPage(
            threadId = threadId,
            title = thread.title.ifBlank { "(无标题)" },
            forumName = data.forum.name.ifBlank { thread.fname }.ifBlank { null },
            forumAvatarUrl = normalizeUrl(data.forum.avatar),
            authorName = displayName(thread.author),
            authorAvatarUrl = portraitToAvatarUrl(thread.author.portrait),
            currentPage = data.page.currentPage.takeIf { it > 0 } ?: 1,
            hasMore = data.page.hasMore != 0,
            posts = posts,
        )
    }

    private fun buildPostList(raw: PbPageRaw): List<ThreadPostLite> {
        val list = raw.response.data.postListList.toMutableList()
        val firstFloorPost = raw.response.data.firstFloorPost
        if (firstFloorPost.id != 0L && list.none { post -> post.id == firstFloorPost.id }) {
            list += firstFloorPost
        }
        return list.sortedBy { post ->
            post.floor.takeIf { floor -> floor > 0 } ?: Int.MAX_VALUE
        }
    }

    private fun mapPost(
        post: ThreadPostLite,
        userMap: Map<Long, ThreadUserLite>,
    ): ThreadPost {
        val embeddedAuthor = post.author.takeIf(::hasUserPayload)
        val fallbackAuthor =
            userMap[post.authorId]
                ?.takeIf(::hasUserPayload)
        val author = embeddedAuthor ?: fallbackAuthor
        val imageUrls =
            post.contentList
                .asSequence()
                .flatMap { content ->
                    sequenceOf(
                        content.originSrc,
                        content.bigSrc,
                        content.src,
                    )
                }.mapNotNull(::normalizeUrl)
                .distinct()
                .toList()
        val contentText = extractContentText(post)
        return ThreadPost(
            id = post.id,
            floor = post.floor,
            authorId =
                author?.id
                    ?.takeIf { id -> id > 0L }
                    ?: post.authorId,
            authorName = author?.let(::displayName),
            authorLevel =
                author?.levelId
                    ?.takeIf { level -> level > 0 }
                    ?: 0,
            authorAvatarUrl = portraitToAvatarUrl(author?.portrait.orEmpty()),
            ipLocation = author?.ipAddress?.trim()?.takeIf { it.isNotBlank() } ?: author?.ip?.trim()?.takeIf { it.isNotBlank() },
            contentText =
                when {
                    contentText.isNotBlank() -> contentText
                    imageUrls.isNotEmpty() -> ""
                    else -> ""
                },
            imageUrls = imageUrls,
            timestampSeconds =
                post.time
                    .takeIf { it > 0 }
                    ?.toLong(),
        )
    }

    private fun extractContentText(post: ThreadPostLite): String =
        post.contentList
            .asSequence()
            .map { content ->
                content.text.trim()
            }.filter { text ->
                text.isNotBlank()
            }.joinToString("\n")

    private fun displayName(user: ThreadUserLite): String? =
        user.nameShow
            .ifBlank { user.name }
            .ifBlank { null }

    private fun hasUserPayload(user: ThreadUserLite): Boolean =
        user.id > 0L ||
            user.name.isNotBlank() ||
            user.nameShow.isNotBlank() ||
            user.portrait.isNotBlank()

    private fun portraitToAvatarUrl(portrait: String): String? {
        val value = portrait.trim()
        if (value.isBlank()) {
            return null
        }
        return if (value.startsWith("http://") || value.startsWith("https://")) {
            value
        } else {
            "http://tb.himg.baidu.com/sys/portrait/item/$value"
        }
    }

    private fun normalizeUrl(raw: String): String? {
        val value = raw.trim()
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
