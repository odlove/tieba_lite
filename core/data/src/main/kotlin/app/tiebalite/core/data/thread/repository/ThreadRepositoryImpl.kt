package app.tiebalite.core.data.thread.repository

import app.tiebalite.core.data.thread.mapper.ThreadPageMapper
import app.tiebalite.core.data.thread.remote.ThreadRemoteDataSource
import app.tiebalite.core.model.thread.ThreadPage

class ThreadRepositoryImpl(
    private val remoteDataSource: ThreadRemoteDataSource,
    private val mapper: ThreadPageMapper = ThreadPageMapper(),
) : ThreadRepository {
    override suspend fun loadThreadPage(
        threadId: Long,
        page: Int,
    ): Result<ThreadPage> =
        remoteDataSource.loadThreadPage(threadId = threadId, page = page).mapCatching(mapper::map)
}
