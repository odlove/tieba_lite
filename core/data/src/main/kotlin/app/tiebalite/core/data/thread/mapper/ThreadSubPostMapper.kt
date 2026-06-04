package app.tiebalite.core.data.thread.mapper

import app.tiebalite.core.data.common.mapper.portraitToAvatarUrl
import app.tiebalite.core.model.thread.ThreadSubPost
import app.tiebalite.core.network.proto.thread.AgreeLite
import app.tiebalite.core.network.proto.thread.ThreadPbContentLite
import app.tiebalite.core.network.proto.thread.ThreadSubPostListLite
import app.tiebalite.core.network.proto.thread.ThreadUserLite

internal class ThreadSubPostMapper(
    private val contentMapper: ThreadContentMapper = ThreadContentMapper(),
) {
    fun map(
        subPost: ThreadSubPostListLite,
        author: ThreadUserLite?,
    ): ThreadSubPost =
        mapFields(
            id = subPost.id,
            floor = subPost.floor,
            agree = subPost.agree,
            authorId = subPost.authorId,
            author = author,
            content = subPost.contentList,
            time = subPost.time,
        )

    private fun mapFields(
        id: Long,
        floor: Int,
        agree: AgreeLite,
        authorId: Long,
        author: ThreadUserLite?,
        content: List<ThreadPbContentLite>,
        time: Int,
    ): ThreadSubPost {
        return ThreadSubPost(
            id = id,
            floor = floor,
            agreeCount = agree.agreeNum,
            authorId =
                author?.id
                    ?.takeIf { id -> id > 0L }
                    ?: authorId,
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
            body = contentMapper.map(content),
            timestampSeconds =
                time
                    .takeIf { it > 0 }
                    ?.toLong(),
        )
    }
}
