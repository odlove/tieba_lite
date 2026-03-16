package app.tiebalite.core.data.myforums.repository

import app.tiebalite.core.data.myforums.mapper.MyForumsMapper
import app.tiebalite.core.data.myforums.remote.MyForumsRemoteDataSource
import app.tiebalite.core.model.myforums.MyForumItem

class MyForumsRepositoryImpl(
    private val remoteDataSource: MyForumsRemoteDataSource,
    private val mapper: MyForumsMapper = MyForumsMapper(),
) : MyForumsRepository {
    override suspend fun loadMyForums(): Result<List<MyForumItem>> =
        remoteDataSource.loadForumGuide().map { raw ->
            mapper.map(raw.response.data.likeForumList).distinctBy { it.forumId }
        }
}
