package app.tiebalite.core.data.forum.remote

import app.tiebalite.core.model.auth.AuthSession
import app.tiebalite.core.network.source.tbclient.forum.FrsPageNetworkSource
import app.tiebalite.core.network.source.tbclient.forum.FrsPageRaw

class ForumRemoteDataSource(
    private val frsPageNetworkSource: FrsPageNetworkSource,
    private val sessionProvider: () -> AuthSession? = { null },
) {
    suspend fun loadForumPage(
        forumName: String,
        page: Int,
        loadType: Int,
        sortType: Int = 0,
    ): Result<FrsPageRaw> {
        val session = sessionProvider()
        return frsPageNetworkSource.fetchPage(
            forumName = forumName,
            page = page,
            loadType = loadType,
            sortType = sortType,
            bduss = session?.bduss,
            stoken = session?.stoken,
            tbs = session?.tbs,
        )
    }
}
