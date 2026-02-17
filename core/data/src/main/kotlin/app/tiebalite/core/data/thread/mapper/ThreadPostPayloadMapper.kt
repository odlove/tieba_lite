package app.tiebalite.core.data.thread.mapper

import app.tiebalite.core.model.thread.ThreadPostBody
import app.tiebalite.core.network.proto.thread.ThreadPostLite
import app.tiebalite.core.network.proto.thread.ThreadUserLite

internal class ThreadPostPayloadMapper(
    private val contentMapper: ThreadContentMapper = ThreadContentMapper(),
) {
    fun map(
        post: ThreadPostLite,
        author: ThreadUserLite?,
    ): ThreadPostPayload {
        return ThreadPostPayload(
            id = post.id,
            floor = post.floor,
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
}

internal data class ThreadPostPayload(
    val id: Long,
    val floor: Int,
    val authorId: Long,
    val authorName: String?,
    val authorLevel: Int,
    val authorAvatarUrl: String?,
    val ipLocation: String?,
    val body: ThreadPostBody,
    val timestampSeconds: Long?,
)
