package app.tiebalite.core.data.thread.remote

import app.tiebalite.core.model.auth.AuthSession
import app.tiebalite.core.network.source.tbclient.thread.PbFloorNetworkSource
import app.tiebalite.core.network.source.tbclient.thread.PbFloorRaw
import app.tiebalite.core.network.source.tbclient.thread.PbPageNetworkSource
import app.tiebalite.core.network.source.tbclient.thread.PbPageRaw

class ThreadRemoteDataSource(
    private val pbPageNetworkSource: PbPageNetworkSource,
    private val pbFloorNetworkSource: PbFloorNetworkSource,
    private val sessionProvider: () -> AuthSession? = { null },
    private val tbsProvider: () -> String? = { null },
) {
    suspend fun loadThreadPage(
        threadId: Long,
        page: Int,
        postId: Long,
        lastPostId: Long?,
    ): Result<PbPageRaw> {
        val session = sessionProvider()
        return pbPageNetworkSource.fetchPage(
            threadId = threadId,
            page = page,
            postId = postId,
            lastPostId = lastPostId,
            bduss = session?.bduss,
            stoken = session?.stoken,
            tbs = tbsProvider() ?: session?.tbs,
        )
    }

    suspend fun loadThreadSubPostsPage(
        threadId: Long,
        postId: Long,
        page: Int,
        subPostId: Long,
        forumId: Long,
    ): Result<PbFloorRaw> {
        val session = sessionProvider()
        return pbFloorNetworkSource.fetchFloor(
            threadId = threadId,
            postId = postId,
            page = page,
            subPostId = subPostId,
            forumId = forumId,
            bduss = session?.bduss,
            stoken = session?.stoken,
            tbs = tbsProvider() ?: session?.tbs,
        )
    }
}
