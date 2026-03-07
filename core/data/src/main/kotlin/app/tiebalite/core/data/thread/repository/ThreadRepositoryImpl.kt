package app.tiebalite.core.data.thread.repository

import app.tiebalite.core.data.thread.mapper.ThreadPageMapper
import app.tiebalite.core.data.thread.mapper.ThreadSubPostsPageMapper
import app.tiebalite.core.data.thread.remote.ThreadRemoteDataSource
import app.tiebalite.core.model.thread.ThreadPage
import app.tiebalite.core.model.thread.ThreadSubPostsPage

class ThreadRepositoryImpl(
    private val remoteDataSource: ThreadRemoteDataSource,
    private val mapper: ThreadPageMapper = ThreadPageMapper(),
) : ThreadRepository {
    private val subPostsMapper = ThreadSubPostsPageMapper()

    override suspend fun loadThreadPage(
        threadId: Long,
        page: Int,
        postId: Long,
        lastPostId: Long?,
    ): Result<ThreadPage> =
        remoteDataSource.loadThreadPage(
            threadId = threadId,
            page = page,
            postId = postId,
            lastPostId = lastPostId,
        ).mapCatching(mapper::map)

    override suspend fun loadThreadSubPostsPage(
        threadId: Long,
        postId: Long,
        page: Int,
        subPostId: Long,
        forumId: Long,
    ): Result<ThreadSubPostsPage> =
        remoteDataSource.loadThreadSubPostsPage(
            threadId = threadId,
            postId = postId,
            page = page,
            subPostId = subPostId,
            forumId = forumId,
        ).mapCatching(subPostsMapper::map)
}
