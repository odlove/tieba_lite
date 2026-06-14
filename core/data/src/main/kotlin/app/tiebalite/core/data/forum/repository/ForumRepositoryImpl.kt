package app.tiebalite.core.data.forum.repository

import app.tiebalite.core.data.forum.mapper.ForumPageMapper
import app.tiebalite.core.data.forum.remote.ForumRemoteDataSource
import app.tiebalite.core.model.forum.ForumPage

internal class ForumRepositoryImpl(
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

    override suspend fun followForum(
        forumId: Long,
        forumName: String,
    ): Result<Unit> =
        remoteDataSource.followForum(
            forumId = forumId,
            forumName = forumName,
        )

    override suspend fun unfollowForum(
        forumId: Long,
        forumName: String,
    ): Result<Unit> =
        remoteDataSource.unfollowForum(
            forumId = forumId,
            forumName = forumName,
        )
}
