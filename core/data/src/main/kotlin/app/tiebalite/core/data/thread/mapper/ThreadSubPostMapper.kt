package app.tiebalite.core.data.thread.mapper

import app.tiebalite.core.model.thread.ThreadSubPost
import app.tiebalite.core.network.proto.thread.ThreadSubPostLite
import app.tiebalite.core.network.proto.thread.ThreadUserLite

internal class ThreadSubPostMapper(
    private val contentMapper: ThreadContentMapper = ThreadContentMapper(),
) {
    fun map(
        subPost: ThreadSubPostLite,
        author: ThreadUserLite?,
    ): ThreadSubPost {
        return ThreadSubPost(
            id = subPost.id,
            floor = subPost.floor,
            authorId =
                author?.id
                    ?.takeIf { id -> id > 0L }
                    ?: subPost.authorId,
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
            body = contentMapper.map(subPost.contentList),
            timestampSeconds =
                subPost.time
                    .takeIf { it > 0 }
                    ?.toLong(),
        )
    }
}
