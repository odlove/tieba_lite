package app.tiebalite.core.data.thread.mapper

import app.tiebalite.core.network.proto.thread.ThreadPostLite
import app.tiebalite.core.network.proto.thread.ThreadUserLite

internal class ThreadAuthorResolver {
    fun buildUserMap(userList: List<ThreadUserLite>): Map<Long, ThreadUserLite> =
        userList
            .asSequence()
            .mapNotNull { user ->
                user.id.takeIf { it > 0L }?.let { id -> id to user }
            }.toMap()

    fun resolve(
        post: ThreadPostLite,
        userMap: Map<Long, ThreadUserLite>,
    ): ThreadUserLite? =
        userMap[post.authorId]

    fun resolve(
        authorId: Long,
        userMap: Map<Long, ThreadUserLite>,
    ): ThreadUserLite? =
        userMap[authorId]
}
