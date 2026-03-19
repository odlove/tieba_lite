package app.tiebalite.core.data.forum.repository

import app.tiebalite.core.data.forum.mapper.ForumPageMapper
import app.tiebalite.core.data.forum.remote.ForumRemoteDataSource
import app.tiebalite.core.model.forum.ForumPage

class ForumRepositoryImpl(
    private val remoteDataSource: ForumRemoteDataSource,
    private val mapper: ForumPageMapper = ForumPageMapper(),
) : ForumRepository {
    override suspend fun loadForumPage(
        forumName: String,
        page: Int,
        loadType: Int,
        sortType: Int,
    ): Result<ForumPage> =
        remoteDataSource.loadForumPage(
            forumName = forumName,
            page = page,
            loadType = loadType,
            sortType = sortType,
        ).map { raw ->
            mapper.map(
                raw = raw,
                requestedForumName = forumName,
                fallbackCurrentPage = page,
            )
        }
}
