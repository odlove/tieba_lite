package app.tiebalite.core.data.thread.mapper

import app.tiebalite.core.model.thread.ThreadSubPost
import app.tiebalite.core.model.thread.ThreadPostBody
import app.tiebalite.core.network.proto.thread.ThreadInlineSubPostsLite
import app.tiebalite.core.network.proto.thread.ThreadPostLite
import app.tiebalite.core.network.proto.thread.ThreadSubPostLite
import app.tiebalite.core.network.proto.thread.ThreadUserLite

internal class ThreadPostPayloadMapper(
    private val contentMapper: ThreadContentMapper = ThreadContentMapper(),
    private val subPostMapper: ThreadSubPostMapper = ThreadSubPostMapper(),
) {
    fun map(
        post: ThreadPostLite,
        author: ThreadUserLite?,
        userMap: Map<Long, ThreadUserLite> = emptyMap(),
    ): ThreadPostPayload {
        return ThreadPostPayload(
            id = post.id,
            floor = post.floor,
            subPostCount = post.subPostNumber,
            subPosts = parseSubPosts(post = post, userMap = userMap),
            authorId =
                author?.id
                    ?.takeIf { id -> id > 0L }
                    ?: post.authorId,
            authorName =
                author
                    ?.nameShow
                    ?.ifBlank { author.name }
                    ?.ifBlank { null },
            authorLevel =
                author?.levelId
                    ?.takeIf { level -> level > 0 }
                    ?: 0,
            authorAvatarUrl = portraitToAvatarUrl(author?.portrait.orEmpty()),
            ipLocation = author?.ipAddress?.trim()?.takeIf { it.isNotBlank() },
            body = contentMapper.map(post.contentList),
            timestampSeconds =
                post.time
                    .takeIf { it > 0 }
                    ?.toLong(),
        )
    }

    private fun parseSubPosts(
        post: ThreadPostLite,
        userMap: Map<Long, ThreadUserLite>,
    ): List<ThreadSubPost> {
        if (post.subPostNumber <= 0 || post.subPostList.isEmpty) {
            return emptyList()
        }
        val container =
            runCatching {
                ThreadInlineSubPostsLite.parseFrom(post.subPostList)
            }.getOrNull() ?: return emptyList()
        return container.subPostListList.map { subPost ->
            subPostMapper.map(
                subPost = subPost,
                author = resolveSubPostAuthor(subPost = subPost, userMap = userMap),
            )
        }
    }

    private fun resolveSubPostAuthor(
        subPost: ThreadSubPostLite,
        userMap: Map<Long, ThreadUserLite>,
    ): ThreadUserLite? {
        val embeddedAuthor =
            subPost.author.takeUnless { author ->
                author.id <= 0L &&
                    author.nameShow.isBlank() &&
                    author.name.isBlank() &&
                    author.portrait.isBlank() &&
                    author.levelId <= 0 &&
                    author.ipAddress.isBlank()
            }
        return embeddedAuthor ?: userMap[subPost.authorId]
    }
}

internal data class ThreadPostPayload(
    val id: Long,
    val floor: Int,
    val subPostCount: Int,
    val subPosts: List<ThreadSubPost>,
    val authorId: Long,
    val authorName: String?,
    val authorLevel: Int,
    val authorAvatarUrl: String?,
    val ipLocation: String?,
    val body: ThreadPostBody,
    val timestampSeconds: Long?,
)
