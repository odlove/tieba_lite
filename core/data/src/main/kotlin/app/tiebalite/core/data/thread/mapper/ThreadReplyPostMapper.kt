package app.tiebalite.core.data.thread.mapper

import app.tiebalite.core.model.thread.ThreadPost
import app.tiebalite.core.network.proto.thread.ThreadPostLite
import app.tiebalite.core.network.proto.thread.ThreadUserLite

internal class ThreadReplyPostMapper(
    private val payloadMapper: ThreadPostPayloadMapper = ThreadPostPayloadMapper(),
) {
    fun map(
        post: ThreadPostLite,
        author: ThreadUserLite?,
    ): ThreadPost {
        val payload = payloadMapper.map(post = post, author = author)
        return ThreadPost(
            id = payload.id,
            floor = payload.floor,
            subPostCount = payload.subPostCount,
            authorId = payload.authorId,
            authorName = payload.authorName,
            authorLevel = payload.authorLevel,
            authorAvatarUrl = payload.authorAvatarUrl,
            ipLocation = payload.ipLocation,
            body = payload.body,
            timestampSeconds = payload.timestampSeconds,
        )
    }
}
