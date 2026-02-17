package app.tiebalite.core.data.thread.mapper

import app.tiebalite.core.model.thread.ThreadFirstFloorPost
import app.tiebalite.core.network.proto.thread.ThreadPostLite
import app.tiebalite.core.network.proto.thread.ThreadUserLite

internal class ThreadFirstFloorPostMapper(
    private val payloadMapper: ThreadPostPayloadMapper = ThreadPostPayloadMapper(),
) {
    fun map(
        post: ThreadPostLite,
        author: ThreadUserLite?,
        fallbackThreadTitle: String,
    ): ThreadFirstFloorPost {
        val payload = payloadMapper.map(post = post, author = author)
        return ThreadFirstFloorPost(
            title = fallbackThreadTitle,
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
